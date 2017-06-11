package net.wizardsoflua.testenv.log4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import net.minecraftforge.common.MinecraftForge;
import net.wizardsoflua.testenv.event.ServerLog4jEvent;

/**
 * The {@link Log4j2ForgeEventBridge} forwards {@link LogEvent}s to the Forge event bus as
 * {@link ServerLog4jEvent}s.
 */
public class Log4j2ForgeEventBridge {

  private final String loggerName;
  private final Appender appender = new AbstractAppender("Log4jEventBridge", null,
      PatternLayout.createLayout("%msg", null, null, null, null)) {
    @Override
    public void append(LogEvent event) {
      MinecraftForge.EVENT_BUS.post(new ServerLog4jEvent(event));
    }
  };

  public Log4j2ForgeEventBridge(String loggerName) {
    this.loggerName = loggerName;
  }

  public void activate() {
    Logger coreLogger = (Logger) LogManager.getLogger(loggerName);
    appender.start();
    coreLogger.addAppender(appender);
  }

}
