package org.apache.log4j;
import junit.framework.TestCase;
import org.apache.log4j.spi.LoggingEvent;
public class LayoutTest extends TestCase {
  private final String contentType;
  private final boolean ignoresThrowable;
  private final String header;
  private final String footer;
  public LayoutTest(final String testName) {
    super(testName);
    contentType = "text/plain";
    ignoresThrowable = true;
    header = null;
    footer = null;
  }
  protected LayoutTest(
    final String testName, final String expectedContentType,
    final boolean expectedIgnoresThrowable, final String expectedHeader,
    final String expectedFooter) {
    super(testName);
    contentType = expectedContentType;
    ignoresThrowable = expectedIgnoresThrowable;
    header = expectedHeader;
    footer = expectedFooter;
  }
  public void testLineSep() {
    assertEquals(System.getProperty("line.separator"), Layout.LINE_SEP);
  }
  public void testLineSepLen() {
    assertEquals(Layout.LINE_SEP.length(), Layout.LINE_SEP_LEN);
  }
  protected Layout createLayout() {
    return new MockLayout();
  }
  public void testGetContentType() {
    assertEquals(contentType, createLayout().getContentType());
  }
  public void testIgnoresThrowable() {
    assertEquals(ignoresThrowable, createLayout().ignoresThrowable());
  }
  public void testGetHeader() {
    assertEquals(header, createLayout().getHeader());
  }
  public void testGetFooter() {
    assertEquals(footer, createLayout().getFooter());
  }
  public void testFormat() throws Exception {
    Logger logger = Logger.getLogger("org.apache.log4j.LayoutTest");
    LoggingEvent event =
      new LoggingEvent(
        "org.apache.log4j.Logger", logger, Level.INFO, "Hello, World", null);
    String result = createLayout().format(event);
    assertEquals("Mock", result);
  }
  private static final class MockLayout extends Layout {
    public String format(final LoggingEvent event) {
      return "Mock";
    }
    public void activateOptions() {
    }
    public boolean ignoresThrowable() {
      return true;
    }
  }
}
