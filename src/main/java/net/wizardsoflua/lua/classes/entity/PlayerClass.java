package net.wizardsoflua.lua.classes.entity;

import java.util.HashSet;
import java.util.Set;

import com.google.common.cache.Cache;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.Table;
import net.wizardsoflua.lua.Converters;
import net.wizardsoflua.lua.classes.DeclareLuaClass;
import net.wizardsoflua.lua.classes.ProxyCachingLuaClass;

@DeclareLuaClass(name = PlayerClass.METATABLE_NAME, superclassname = EntityClass.METATABLE_NAME)
public class PlayerClass
    extends ProxyCachingLuaClass<EntityPlayerMP, PlayerClass.Proxy<EntityPlayerMP>> {
  public static final String METATABLE_NAME = "Player";

  public PlayerClass() {
    add("putNbt", new UnsupportedFunction("putNbt", METATABLE_NAME));
  }

  @Override
  public Proxy<EntityPlayerMP> toLua(EntityPlayerMP delegate) {
    return new Proxy<>(getConverters(), getMetaTable(), delegate);
  }

  public void replaceDelegate(EntityPlayerMP newPlayer) {
    Cache<EntityPlayerMP, Proxy<EntityPlayerMP>> cache = getCache();

    Set<EntityPlayer> found = new HashSet<>();
    for (EntityPlayer key : cache.asMap().keySet()) {
      if (key.getUniqueID().equals(newPlayer.getUniqueID())) {
        found.add(key);
      }
    }

    for (EntityPlayer entityPlayer : found) {
      Proxy<EntityPlayerMP> oldValue = cache.asMap().remove(entityPlayer);
      cache.put(newPlayer, oldValue);
      oldValue.setDelegate(newPlayer);
    }
  }

  public class Proxy<D extends EntityPlayerMP> extends EntityClass.EntityLivingBaseProxy<D> {
    public Proxy(Converters converters, Table metatable, D delegate) {
      super(converters, metatable, delegate);
      // Overwrite name, since player names can't be changed
      addReadOnly("name", this::getName);
      add("team", this::getTeam, this::setTeam);
      add("gamemode", this::getGamemode, this::setGamemode);
    }

    public Object getGamemode() {
      GameType result = delegate.interactionManager.getGameType();
      return getConverters().toLua(result);
    }

    public void setGamemode(Object modeObj) {
      GameType mode = getConverters().toJava(GameType.class, modeObj);
      delegate.setGameType(mode);
    }

    @Override
    public float getRotationYaw() {
      float v = delegate.rotationYaw;
      return MathHelper.wrapDegrees(v);
    }

    @Override
    public void setRotationYaw(Object luaObj) {
      super.setRotationYaw(luaObj);
      if (delegate instanceof EntityPlayerMP) {
        ((EntityPlayerMP) delegate).connection.setPlayerLocation(delegate.posX, delegate.posY,
            delegate.posZ, delegate.rotationYaw, delegate.rotationPitch);
      }
    }

    @Override
    public void setRotationPitch(Object luaObj) {
      super.setRotationPitch(luaObj);
      if (delegate instanceof EntityPlayerMP) {
        ((EntityPlayerMP) delegate).connection.setPlayerLocation(delegate.posX, delegate.posY,
            delegate.posZ, delegate.rotationYaw, delegate.rotationPitch);
      }
    }

    @Override
    public void setRotationYawAndPitch(float yaw, float pitch) {
      super.setRotationYawAndPitch(yaw, pitch);
      if (delegate instanceof EntityPlayerMP) {
        ((EntityPlayerMP) delegate).connection.setPlayerLocation(delegate.posX, delegate.posY,
            delegate.posZ, delegate.rotationYaw, delegate.rotationPitch);
      }
    }

    public ByteString getTeam() {
      Team team = delegate.getTeam();
      if (team == null) {
        return null;
      }
      return ByteString.of(team.getRegisteredName());
    }

    public void setTeam(Object luaObj) {
      String teamName = getConverters().toJavaNullable(String.class, luaObj);
      if (teamName == null) {
        delegate.getWorldScoreboard().removePlayerFromTeams(delegate.getName());
      } else {
        boolean success =
            delegate.getWorldScoreboard().addPlayerToTeam(delegate.getName(), teamName);
        if (!success) {
          throw new IllegalArgumentException(String
              .format("Couldn't add player %s to unknown team %s!", delegate.getName(), teamName));
        }
      }
    }
  }


}
