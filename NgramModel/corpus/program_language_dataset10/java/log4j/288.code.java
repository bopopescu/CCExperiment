package org.apache.log4j;
import org.apache.log4j.spi.LoggingEvent;
public class PatternLayoutTest extends LayoutTest {
  public PatternLayoutTest(final String testName) {
    super(testName, "text/plain", true, null, null);
  }
  protected Layout createLayout() {
    return new PatternLayout("[%t] %p %c - %m%n");
  }
  public void testFormat() {
    Logger logger = Logger.getLogger("org.apache.log4j.LayoutTest");
    LoggingEvent event =
      new LoggingEvent(
        "org.apache.log4j.Logger", logger, Level.INFO, "Hello, World", null);
    PatternLayout layout = (PatternLayout) createLayout();
    String result = layout.format(event);
    StringBuffer buf = new StringBuffer(100);
    buf.append('[');
    buf.append(event.getThreadName());
    buf.append("] ");
    buf.append(event.getLevel().toString());
    buf.append(' ');
    buf.append(event.getLoggerName());
    buf.append(" - ");
    buf.append(event.getMessage());
    buf.append(System.getProperty("line.separator"));
    assertEquals(buf.toString(), result);
  }
  public void testGetPatternFormat() {
    PatternLayout layout = (PatternLayout) createLayout();
    assertEquals("[%t] %p %c - %m%n", layout.getConversionPattern());
  }
  public void testDefaultConversionPattern() {
    assertEquals("%m%n", PatternLayout.DEFAULT_CONVERSION_PATTERN);
  }
  public void testTTCCConversionPattern() {
    assertEquals(
      "%r [%t] %p %c %x - %m%n", PatternLayout.TTCC_CONVERSION_PATTERN);
  }
  public void testFormatResize() {
    Logger logger = Logger.getLogger("org.apache.log4j.xml.PatternLayoutTest");
    NDC.clear();
    char[] msg = new char[2000];
    for (int i = 0; i < msg.length; i++) {
      msg[i] = 'A';
    }
    LoggingEvent event1 =
      new LoggingEvent(
        "org.apache.log4j.Logger", logger, Level.DEBUG, new String(msg), null);
    PatternLayout layout = (PatternLayout) createLayout();
    String result = layout.format(event1);
    LoggingEvent event2 =
      new LoggingEvent(
        "org.apache.log4j.Logger", logger, Level.WARN, "Hello, World", null);
    result = layout.format(event2);
    assertEquals("[", result.substring(0, 1));
  }
  public static final class DerivedPatternLayout extends PatternLayout {
    public DerivedPatternLayout() {
    }
    public int getBufSize() {
      return BUF_SIZE;
    }
    public int getMaxCapacity() {
      return MAX_CAPACITY;
    }
  }
}
