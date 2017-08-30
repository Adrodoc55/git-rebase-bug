package net.wizardsoflua.tests;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;
import org.junit.runner.RunWith;

import net.wizardsoflua.testenv.MinecraftJUnitRunner;
import net.wizardsoflua.testenv.WolTestBase;
import net.wizardsoflua.testenv.event.ServerLog4jEvent;
import net.wizardsoflua.testenv.event.TestPlayerReceivedChatEvent;
import net.wizardsoflua.testenv.net.ChatAction;

@RunWith(MinecraftJUnitRunner.class)
public class RuntimeTest extends WolTestBase {

  // /test net.wizardsoflua.tests.RuntimeTest test_getRealDateTime_executed_by_player
  @Test
  public void test_getRealDateTime_executed_by_player() throws Exception {
    // Given:
    LocalDateTime now = LocalDateTime.now();
    String expected = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    mc().freezeClock(now);

    // When:
    mc().player().perform(new ChatAction("/lua print(Runtime.getRealDateTime())"));

    // Then:
    TestPlayerReceivedChatEvent act = mc().waitFor(TestPlayerReceivedChatEvent.class);
    assertThat(act.getMessage()).isEqualTo(expected);
  }

  // /test net.wizardsoflua.tests.RuntimeTest test_getRealDateTime_executed_by_server
  @Test
  public void test_getRealDateTime_executed_by_server() throws Exception {
    // Given:
    LocalDateTime now = LocalDateTime.now();
    String expected = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    mc().freezeClock(now);

    // When:
    mc().executeCommand("/lua print(Runtime.getRealDateTime())");

    // Then:
    ServerLog4jEvent act = mc().waitFor(ServerLog4jEvent.class);
    assertThat(act.getMessage()).isEqualTo(expected);
  }

  /// test net.wizardsoflua.tests.RuntimeTest test_getRealtime_executed_by_player
  @Test
  public void test_getRealtime_executed_by_player() throws Exception {
    // Given:
    long now = System.currentTimeMillis();
    String expected = String.valueOf(now);
    mc().freezeClock(now);

    // When:
    mc().player().perform(new ChatAction("/lua print(Runtime.getRealtime())"));

    // Then:
    TestPlayerReceivedChatEvent act = mc().waitFor(TestPlayerReceivedChatEvent.class);
    assertThat(act.getMessage()).isEqualTo(expected);
  }

  // /test net.wizardsoflua.tests.RuntimeTest test_getRealtime_executed_by_server
  @Test
  public void test_getRealtime_executed_by_server() throws Exception {
    // Given:
    long now = System.currentTimeMillis();
    String expected = String.valueOf(now);
    mc().freezeClock(now);

    // When:
    mc().executeCommand("/lua print(Runtime.getRealtime())");

    // Then:
    ServerLog4jEvent act = mc().waitFor(ServerLog4jEvent.class);
    assertThat(act.getMessage()).isEqualTo(expected);
  }

  // /test net.wizardsoflua.tests.RuntimeTest test_sleep
  @Test
  public void test_sleep() throws Exception {
    // Given:
    long sleepTime = 10;
    // When:
    mc().executeCommand(
        "/lua print(Runtime.getGametime()); Runtime.sleep(%s); print(Runtime.getGametime())",
        sleepTime);

    // Then:
    ServerLog4jEvent message1 = mc().waitFor(ServerLog4jEvent.class);
    ServerLog4jEvent message2 = mc().waitFor(ServerLog4jEvent.class);
    long actual = Long.parseLong(message2.getMessage()) - Long.parseLong(message1.getMessage());
    assertThat(actual).isEqualTo(sleepTime);
  }

  // /test net.wizardsoflua.tests.RuntimeTest test_spell_will_be_broken_when_autosleep_is_off
  @Test
  public void test_spell_will_be_broken_when_autosleep_is_off() throws Exception {
    // Given:
    int repetitions = 2000;
    mc().setLuaTicksLimit(10000);
    // When:
    mc().player().perform(new ChatAction(
        "/lua Runtime.setAutoSleep(false); for i=1,%s do print(i); end", repetitions));

    // Then:
    for (int i = 0; i < 1998; ++i) {
      TestPlayerReceivedChatEvent act = mc().waitFor(TestPlayerReceivedChatEvent.class);
      assertThat(act.getMessage()).isEqualTo(String.valueOf(i + 1));
    }
    TestPlayerReceivedChatEvent act = mc().waitFor(TestPlayerReceivedChatEvent.class);
    assertThat(act.getMessage()).contains("Spell has been broken automatically");
  }

}
