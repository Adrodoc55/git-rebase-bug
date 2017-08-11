package net.wizardsoflua.testenv;

import org.junit.After;
import org.junit.Before;

import com.google.common.collect.Iterables;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.wizardsoflua.testenv.event.TestPlayerPreparedForTestEvent;
import net.wizardsoflua.testenv.net.PrepareForTestAction;

public class WolTestBase extends TestDataFactory {
  private static int testIdCount = 0;
  private WolTestEnvironment testEnv = WolTestEnvironment.instance;
  private MinecraftBackdoor mcBackdoor = new MinecraftBackdoor(testEnv, MinecraftForge.EVENT_BUS);;

  @Before
  public void beforeTest() {
    testEnv.getEventRecorder().setEnabled(true);
    mc().resetClock();
    mc().breakAllSpells();
    int testId = testIdCount++;
    mc().player().perform(new PrepareForTestAction(testId));
    TestPlayerPreparedForTestEvent evt = mc().waitFor(TestPlayerPreparedForTestEvent.class);
    assertThat(evt.getId()).isEqualTo(testId);
    testEnv.getEventRecorder().clear();
  }

  @After
  public void afterTest() {
    testEnv.getEventRecorder().setEnabled(false);
    testEnv.getEventRecorder().clear();
    mc().breakAllSpells();
    mc().resetClock();
  }

  protected MinecraftBackdoor mc() {
    return mcBackdoor;
  }

  protected Iterable<String> messagesOf(Iterable<ServerChatEvent> events) {
    return Iterables.transform(events, ServerChatEvent::getMessage);
  }

  protected Iterable<BlockPos> positionsOf(Iterable<RightClickBlock> events) {
    return Iterables.transform(events, RightClickBlock::getPos);
  }

  protected String format(BlockPos pos) {
    return formatPos((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
  }

  protected String formatPos(int x, int y, int z) {
    return "{" + x + ", " + y + ", " + z + "}";
  }
  
  protected String formatPos(double x, double y, double z) {
    return "{" + x + ", " + y + ", " + z + "}";
  }

}
