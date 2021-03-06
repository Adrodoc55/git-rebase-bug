package net.wizardsoflua;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.UsernameCache;

public class GameProfiles {

  private final MinecraftServer server;

  public GameProfiles(MinecraftServer server) {
    this.server = checkNotNull(server, "server==null!");
  }

  public @Nullable GameProfile getGameProfile(String nameOrUuid) {
    checkNotNull(nameOrUuid, "nameOrUuid==null!");
    try {
      return getGameProfileById(UUID.fromString(nameOrUuid));
    } catch (IllegalArgumentException e) {
      return getGameProfileByName(nameOrUuid);
    }
  }

  private GameProfile getGameProfileByName(String playerName) {
    // TODO optimize performance
    Map<UUID, String> map = UsernameCache.getMap();
    for (Map.Entry<UUID, String> entry : map.entrySet()) {
      if (entry.getValue().equals(playerName)) {
        return server.getPlayerProfileCache().getProfileByUUID(entry.getKey());
      }
    }
    return null;
  }

  private GameProfile getGameProfileById(UUID uuid) {
    return server.getPlayerProfileCache().getProfileByUUID(uuid);
  }

}
