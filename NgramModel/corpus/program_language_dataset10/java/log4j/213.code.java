package org.apache.log4j.pattern;
import org.apache.log4j.spi.LoggingEvent;
public final class NDCPatternConverter extends LoggingEventPatternConverter {
  private static final NDCPatternConverter INSTANCE =
    new NDCPatternConverter();
  private NDCPatternConverter() {
    super("NDC", "ndc");
  }
  public static NDCPatternConverter newInstance(
    final String[] options) {
    return INSTANCE;
  }
  public void format(final LoggingEvent event, final StringBuffer toAppendTo) {
    toAppendTo.append(event.getNDC());
  }
}
