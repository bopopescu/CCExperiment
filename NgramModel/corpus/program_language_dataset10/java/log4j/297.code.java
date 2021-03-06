package org.apache.log4j;
import org.apache.log4j.helpers.DateLayoutTest;
import org.apache.log4j.spi.LoggingEvent;
public class TTCCLayoutTest extends DateLayoutTest {
  public TTCCLayoutTest(final String testName) {
    super(testName, "text/plain", true, null, null);
  }
  protected Layout createLayout() {
    return new TTCCLayout();
  }
  public void testFormat() {
    NDC.clear();
    NDC.push("NDC goes here");
    Logger logger = Logger.getLogger("org.apache.log4j.LayoutTest");
    LoggingEvent event =
      new LoggingEvent(
        "org.apache.log4j.Logger", logger, Level.INFO, "Hello, World", null);
    TTCCLayout layout = (TTCCLayout) createLayout();
    String result = layout.format(event);
    NDC.pop();
    StringBuffer buf = new StringBuffer(100);
    layout.dateFormat(buf, event);
    buf.append('[');
    buf.append(event.getThreadName());
    buf.append("] ");
    buf.append(event.getLevel().toString());
    buf.append(' ');
    buf.append(event.getLoggerName());
    buf.append(' ');
    buf.append("NDC goes here");
    buf.append(" - ");
    buf.append(event.getMessage());
    buf.append(System.getProperty("line.separator"));
    assertEquals(buf.toString(), result);
  }
  public void testGetSetThreadPrinting() {
    TTCCLayout layout = new TTCCLayout();
    assertEquals(true, layout.getThreadPrinting());
    layout.setThreadPrinting(false);
    assertEquals(false, layout.getThreadPrinting());
    layout.setThreadPrinting(true);
    assertEquals(true, layout.getThreadPrinting());
  }
  public void testGetSetCategoryPrefixing() {
    TTCCLayout layout = new TTCCLayout();
    assertEquals(true, layout.getCategoryPrefixing());
    layout.setCategoryPrefixing(false);
    assertEquals(false, layout.getCategoryPrefixing());
    layout.setCategoryPrefixing(true);
    assertEquals(true, layout.getCategoryPrefixing());
  }
  public void testGetSetContextPrinting() {
    TTCCLayout layout = new TTCCLayout();
    assertEquals(true, layout.getContextPrinting());
    layout.setContextPrinting(false);
    assertEquals(false, layout.getContextPrinting());
    layout.setContextPrinting(true);
    assertEquals(true, layout.getContextPrinting());
  }
}
