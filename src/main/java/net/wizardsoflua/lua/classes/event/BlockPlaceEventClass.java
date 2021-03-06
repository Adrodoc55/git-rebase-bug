package net.wizardsoflua.lua.classes.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.world.BlockEvent;
import net.wizardsoflua.block.ImmutableWolBlock;
import net.wizardsoflua.lua.classes.DeclareLuaClass;
import net.wizardsoflua.lua.classes.ProxyingLuaClass;

@DeclareLuaClass(name = BlockPlaceEventClass.NAME, superClass = BlockEventClass.class)
public class BlockPlaceEventClass extends
    ProxyingLuaClass<BlockEvent.PlaceEvent, BlockPlaceEventClass.Proxy<BlockEvent.PlaceEvent>> {
  public static final String NAME = "BlockPlaceEvent";

  @Override
  public Proxy<BlockEvent.PlaceEvent> toLua(BlockEvent.PlaceEvent javaObj) {
    return new Proxy<>(this, javaObj);
  }

  public static class Proxy<D extends BlockEvent.PlaceEvent> extends BlockEventClass.Proxy<D> {
    public Proxy(ProxyingLuaClass<?, ?> luaClass, D delegate) {
      super(luaClass, delegate);
      addReadOnly("hand", this::getHand);
      addReadOnly("placedAgainst", this::getPlacedAgainst);
      addReadOnly("player", this::getPlayer);
    }

    @Override
    protected Object getBlock() {
      IBlockState blockState = delegate.getState();
      NBTTagCompound nbt = delegate.getBlockSnapshot().getNbt();
      return getConverters().toLua(new ImmutableWolBlock(blockState, nbt));
    }

    protected Object getHand() {
      return getConverters().toLua(delegate.getHand());
    }

    protected Object getPlacedAgainst() {
      IBlockState blockState = delegate.getPlacedAgainst();
      NBTTagCompound nbt = null;
      return getConverters().toLua(new ImmutableWolBlock(blockState, nbt));
    }

    protected Object getPlayer() {
      return getConverters().toLua(delegate.getPlayer());
    }
  }
}
