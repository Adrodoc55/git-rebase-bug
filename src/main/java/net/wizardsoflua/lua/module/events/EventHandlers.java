package net.wizardsoflua.lua.module.events;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.wizardsoflua.event.CustomLuaEvent;
import net.wizardsoflua.lua.classes.LuaClassLoader;
import net.wizardsoflua.lua.classes.eventqueue.EventQueue;
import net.wizardsoflua.lua.data.Data;

public class EventHandlers {

  public interface Context {
    long getCurrentTime();
  }

  private final Multimap<String, EventQueue> queues = HashMultimap.create();
  private final EventQueue.Context eventQueueContext = new EventQueue.Context() {
    @Override
    public void disconnect(EventQueue eventQueue) {
      EventHandlers.this.disconnect(eventQueue);
    }

    @Override
    public long getCurrentTime() {
      return context.getCurrentTime();
    }
  };
  private final LuaClassLoader classLoader;
  private final Context context;

  public EventHandlers(LuaClassLoader classLoader, Context context) {
    this.classLoader = requireNonNull(classLoader, "classLoader == null!");
    this.context = requireNonNull(context, "context == null!");
  }

  public EventQueue connect(Iterable<String> eventNames) {
    EventQueue result = new EventQueue(eventNames, eventQueueContext);
    for (String name : eventNames) {
      queues.put(name, result);
    }
    return result;
  }

  public void disconnect(EventQueue queue) {
    for (String name : queue.getNames()) {
      queues.remove(name, queue);
    }
  }

  public boolean shouldPause() {
    if (queues.isEmpty()) {
      // no queues -> nothing to wait for, so keep running
      return false;
    }
    long now = context.getCurrentTime();

    for (EventQueue queue : queues.values()) {
      long waitUntil = queue.getWaitUntil();
      if (now <= waitUntil) {
        // we are still waiting for a message
        if (!queue.isEmpty()) {
          // but we have received one -> wake up
          return false;
        }
        // but we havn't yet received one -> pause
        return true;
      }
    }
    return false;
  }

  public void fire(String eventName, Object dataLuaObj) {
    Data data = Data.createData(dataLuaObj, classLoader);
    MinecraftForge.EVENT_BUS.post(new CustomLuaEvent(eventName, data));
  }

  public void onEvent(String eventName, Event event) {
    for (EventQueue eventQueue : queues.get(eventName)) {
      eventQueue.add(event);
    }
  }

}
