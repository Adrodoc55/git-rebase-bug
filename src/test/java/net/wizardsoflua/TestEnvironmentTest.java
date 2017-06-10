package net.wizardsoflua;

import org.junit.Test;
import org.junit.runner.RunWith;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.wizardsoflua.testenv.MinecraftJUnitRunner;
import net.wizardsoflua.testenv.WolTestBase;
import net.wizardsoflua.testenv.net.ChatAction;
import net.wizardsoflua.testenv.net.ClickAction;

@RunWith(MinecraftJUnitRunner.class)
public class TestEnvironmentTest extends WolTestBase {

  // /test net.wizardsoflua.TestEnvironmentTest test_can_receive_ServerChatEvent
  @Test
  public void test_can_receive_ServerChatEvent() {
    // Given:
    String message = "hello";

    // When:
    mc().post(newServerChatEvent(mc().player().getDelegate(), message));

    // Then:
    ServerChatEvent act = mc().waitFor(ServerChatEvent.class);
    assertThat(act.getMessage()).isEqualTo(message);
  }

  // /test net.wizardsoflua.TestEnvironmentTest test_can_receive_RightClickBlock_Event
  @Test
  public void test_can_receive_RightClickBlock_Event() {
    // Given:
    BlockPos pos = BlockPos.ORIGIN;

    // When:
    mc().post(newRightClickBlockEvent(mc().player().getDelegate(), pos));

    // Then:
    RightClickBlock act = mc().waitFor(RightClickBlock.class);
    assertThat(act.getPos()).isEqualTo(pos);
  }

  // /test net.wizardsoflua.TestEnvironmentTest test_can_receive_console_output
  @Test
  public void test_can_receive_console_output() {
    // Given:
    String message = "hello";

    // When:
    mc().player().getDelegate().sendMessage(new TextComponentString(message));

    // Then:
    Iterable<String> act = mc().getChatOutputOf(mc().player().getDelegate());
    assertThat(act).containsOnly(message);
  }

  // /test net.wizardsoflua.TestEnvironmentTest test_can_move_player_around
  @Test
  public void test_can_move_player_around() {
    // Given:
    BlockPos pos = new BlockPos(1, 4, 1);

    // When:
    mc().player().getDelegate().setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());

    // Then
    BlockPos act = mc().player().getDelegate().getPosition();
    assertThat(act).isEqualTo(pos);
  }

  // /test net.wizardsoflua.TestEnvironmentTest test_player_can_post_chat_message
  @Test
  public void test_player_can_post_chat_message() {
    // Given:
    String message = "hello";

    // When:
    mc().player().perform(new ChatAction(message));

    // Then:
    ServerChatEvent act = mc().waitFor(ServerChatEvent.class);
    assertThat(act.getMessage()).isEqualTo(message);
  }

  // /test net.wizardsoflua.TestEnvironmentTest test_player_can_post_several_chat_messages
  @Test
  public void test_player_can_post_several_chat_messages() {
    // Given:
    String message1 = "hello";
    String message2 = "dude";

    // When:
    mc().player().perform(new ChatAction(message1));
    mc().player().perform(new ChatAction(message2));

    // Then:
    ServerChatEvent act1 = mc().waitFor(ServerChatEvent.class);
    assertThat(act1.getMessage()).isEqualTo(message1);
    ServerChatEvent act2 = mc().waitFor(ServerChatEvent.class);
    assertThat(act2.getMessage()).isEqualTo(message2);
  }

  // /test net.wizardsoflua.TestEnvironmentTest test_player_can_click_on_blockpos
  @Test
  public void test_player_can_click_on_blockpos() {
    // Given:
    BlockPos playerPos = new BlockPos(0, 4, 0);
    mc().player().getDelegate().setPositionAndUpdate(playerPos.getX(), playerPos.getY(), playerPos.getZ());
    BlockPos pos = new BlockPos(0, 3, 0);
    EnumFacing facing = EnumFacing.UP;

    // When:
    mc().player().perform(new ClickAction(pos, facing));

    // Then:
    LeftClickBlock act = mc().waitFor(LeftClickBlock.class);
    assertThat(act.getPos()).isEqualTo(pos);
    assertThat(act.getFace()).isEqualTo(facing);
  }

}
