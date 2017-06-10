package net.wizardsoflua.testenv.player;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.player.EntityPlayerMP;
import net.wizardsoflua.testenv.WolTestEnvironment;
import net.wizardsoflua.testenv.net.ClientAction;

public class PlayerBackdoor {
  private WolTestEnvironment testEnv;

  public PlayerBackdoor(WolTestEnvironment testEnv) {
    this.testEnv = testEnv;
  }

  public EntityPlayerMP getDelegate() {
    EntityPlayerMP testPlayer = testEnv.getTestPlayer();
    checkNotNull(testPlayer, "testPlayer==null!");
    return testPlayer;
  }

  public void perform(ClientAction action) {
    testEnv.getServer().addScheduledTask(new Runnable() {
      @Override
      public void run() {
        testEnv.getPacketPipeline().sendTo(action, getDelegate());
      }
    });
  }

}