package net.wizardsoflua.lua.classes.event;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.wizardsoflua.lua.classes.DeclareLuaClass;
import net.wizardsoflua.lua.classes.ProxyingLuaClass;

@DeclareLuaClass(name = RightClickBlockEventClass.NAME, superClass = PlayerInteractEventClass.class)
public class RightClickBlockEventClass extends
    ProxyingLuaClass<PlayerInteractEvent.RightClickBlock, RightClickBlockEventClass.Proxy<PlayerInteractEvent.RightClickBlock>> {
  public static final String NAME = "RightClickBlockEvent";

  @Override
  public Proxy<PlayerInteractEvent.RightClickBlock> toLua(
      PlayerInteractEvent.RightClickBlock javaObj) {
    return new Proxy<>(this, javaObj);
  }

  public static class Proxy<D extends PlayerInteractEvent.RightClickBlock>
      extends PlayerInteractEventClass.Proxy<D> {
    public Proxy(ProxyingLuaClass<?, ?> luaClass, D delegate) {
      super(luaClass, delegate);
      addImmutableNullable("hitVec", getConverters().toLuaNullable(delegate.getHitVec()));
    }
  }
}
