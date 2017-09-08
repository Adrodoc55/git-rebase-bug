package net.wizardsoflua.lua.classes.spell;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.impl.NonsuspendableFunctionException;
import net.sandius.rembulan.lib.StringLib;
import net.sandius.rembulan.runtime.AbstractFunctionAnyArg;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;
import net.wizardsoflua.block.WolBlock;
import net.wizardsoflua.lua.Converters;
import net.wizardsoflua.lua.classes.block.BlockClass;
import net.wizardsoflua.lua.classes.entity.EntityClass;
import net.wizardsoflua.lua.module.types.Terms;
import net.wizardsoflua.spell.SpellEntity;

public class SpellClass {

  public static final String METATABLE_NAME = "Spell";

  private final Converters converters;
  private final Table metatable;

  public SpellClass(Converters converters) {
    this.converters = converters;
    // TODO do declaration outside this class
    this.metatable = converters.getTypes().declare(METATABLE_NAME, EntityClass.METATABLE_NAME);
    metatable.rawset("execute", new ExecuteFunction());
  }

  public Table toLua(SpellEntity delegate) {
    return new Proxy(converters, metatable, delegate);
  }

  public static class Proxy extends EntityClass.Proxy {

    private final SpellEntity delegate;

    public Proxy(Converters converters, Table metatable, SpellEntity delegate) {
      super(converters, metatable, delegate);
      this.delegate = delegate;
      addReadOnly("owner", this::getOwner);
      add("block", this::getBlock, this::setBlock);
      add("visible", this::isVisible, this::setVisible);
    }

    public Table getOwner() {
      return getConverters().entityToLua(delegate.getOwner());
    }

    public Table getBlock() {
      BlockPos pos = new BlockPos(delegate.getPositionVector());
      IBlockState blockState = delegate.getEntityWorld().getBlockState(pos);
      TileEntity tileEntity = delegate.getEntityWorld().getTileEntity(pos);
      WolBlock block = new WolBlock(blockState, tileEntity);
      return getConverters().blockToLua(block);
    }

    public void setBlock(Object luaObj) {
      getConverters().getTypes().checkAssignable(BlockClass.METATABLE_NAME, luaObj,
          Terms.MANDATORY);
      WolBlock wolBlock = getConverters().blockToJava(luaObj);
      World world = delegate.getEntityWorld();
      BlockPos pos = new BlockPos(delegate.getPositionVector());
      wolBlock.setBlock(world, pos);
    }

    public void setVisible(Object luaObj) {
      boolean value =
          checkNotNull(getConverters().booleanToJava(luaObj), "Expected boolean but got nil!");
      delegate.setVisible(value);
    }

    public boolean isVisible() {
      return delegate.isVisible();
    }

    public int execute(String command) {
      World world = delegate.getEntityWorld();
      return world.getMinecraftServer().getCommandManager().executeCommand(delegate, command);
    }
  }

  private class ExecuteFunction extends AbstractFunctionAnyArg {
    @Override
    public void invoke(ExecutionContext context, Object[] args) throws ResolvedControlThrowable {
      Object arg0 = args[0];
      converters.getTypes().checkAssignable(METATABLE_NAME, arg0, Terms.MANDATORY);
      Proxy wrapper = (Proxy) arg0;


      LuaFunction formatFunc = StringLib.format();
      Object[] argArray = new Object[args.length - 1];
      System.arraycopy(args, 1, argArray, 0, args.length - 1);
      formatFunc.invoke(context, argArray);
      String command = String.valueOf(context.getReturnBuffer().get(0));

      int result = wrapper.execute(command);
      context.getReturnBuffer().setTo(result);
    }

    @Override
    public void resume(ExecutionContext context, Object suspendedState)
        throws ResolvedControlThrowable {
      throw new NonsuspendableFunctionException();
    }
  }
}