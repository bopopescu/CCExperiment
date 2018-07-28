package org.apache.log4j;
import org.apache.log4j.spi.LoggingEvent;
public class SimpleLayout extends Layout {
  StringBuffer sbuf = new StringBuffer(128);
  public SimpleLayout() {
  }
  public
  void activateOptions() {
  }
  public
  String format(LoggingEvent event) {
    sbuf.setLength(0);
    sbuf.append(event.getLevel().toString());
    sbuf.append(" - ");
    sbuf.append(event.getRenderedMessage());
    sbuf.append(LINE_SEP);
    return sbuf.toString();
  }
  public
  boolean ignoresThrowable() {
    return true;
  }
}