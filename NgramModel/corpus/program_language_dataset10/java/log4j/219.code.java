package org.apache.log4j.pattern;
import org.apache.log4j.spi.LoggingEvent;
public class ThreadPatternConverter extends LoggingEventPatternConverter {
  private static final ThreadPatternConverter INSTANCE =
    new ThreadPatternConverter();
  private ThreadPatternConverter() {
    super("Thread", "thread");
  }
  public static ThreadPatternConverter newInstance(
    final String[] options) {
    return INSTANCE;
  }
  public void format(final LoggingEvent event, final StringBuffer toAppendTo) {
    toAppendTo.append(event.getThreadName());
  }
}
