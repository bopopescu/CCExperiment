package org.apache.log4j.helpers;
import org.apache.log4j.Layout;
import org.apache.log4j.LayoutTest;
import org.apache.log4j.spi.LoggingEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Calendar;
public class DateLayoutTest extends LayoutTest {
  public DateLayoutTest(final String testName) {
    super(testName);
  }
  protected DateLayoutTest(
    final String testName, final String expectedContentType,
    final boolean expectedIgnoresThrowable, final String expectedHeader,
    final String expectedFooter) {
    super(
      testName, expectedContentType, expectedIgnoresThrowable, expectedHeader,
      expectedFooter);
  }
  protected Layout createLayout() {
    return new MockLayout();
  }
  public void testNullDateFormat() {
    assertEquals("NULL", DateLayout.NULL_DATE_FORMAT);
  }
  public void testRelativeTimeDateFormat() {
    assertEquals("RELATIVE", DateLayout.RELATIVE_TIME_DATE_FORMAT);
  }
  public void testDateFormatOption() {
    assertEquals("DateFormat", DateLayout.DATE_FORMAT_OPTION);
  }
  public void testTimeZoneOption() {
    assertEquals("TimeZone", DateLayout.TIMEZONE_OPTION);
  }
  public void testGetOptionStrings() {
    String[] options = ((DateLayout) createLayout()).getOptionStrings();
    assertEquals(2, options.length);
  }
  public void testSetOptionDateFormat() {
    DateLayout layout = (DateLayout) createLayout();
    layout.setOption("dAtefOrmat", "foobar");
    assertEquals("FOOBAR", layout.getDateFormat());
  }
  public void testSetOptionTimeZone() {
    DateLayout layout = (DateLayout) createLayout();
    layout.setOption("tImezOne", "+05:00");
    assertEquals("+05:00", layout.getTimeZone());
  }
  public void testSetDateFormat() {
    DateLayout layout = (DateLayout) createLayout();
    layout.setDateFormat("ABSOLUTE");
    assertEquals("ABSOLUTE", layout.getDateFormat());
  }
  public void testSetTimeZone() {
    DateLayout layout = (DateLayout) createLayout();
    layout.setTimeZone("+05:00");
    assertEquals("+05:00", layout.getTimeZone());
  }
  public void testSetDateFormatNull() {
    DateLayout layout = (DateLayout) createLayout();
    layout.setDateFormat((String) null, null);
  }
  public void testSetDateFormatNullString() {
    DateLayout layout = (DateLayout) createLayout();
    layout.setDateFormat("NuLL", null);
  }
  public void testSetDateFormatRelative() {
    DateLayout layout = (DateLayout) createLayout();
    layout.setDateFormat("rElatIve", TimeZone.getDefault());
  }
  public void testSetDateFormatAbsolute() {
    DateLayout layout = (DateLayout) createLayout();
    layout.setDateFormat("aBsolUte", TimeZone.getDefault());
  }
  public void testSetDateFormatDateTime() {
    DateLayout layout = (DateLayout) createLayout();
    layout.setDateFormat("dAte", TimeZone.getDefault());
  }
  public void testSetDateFormatISO8601() {
    DateLayout layout = (DateLayout) createLayout();
    layout.setDateFormat("iSo8601", TimeZone.getDefault());
  }
  public void testSetDateFormatSimple() {
    DateLayout layout = (DateLayout) createLayout();
    layout.setDateFormat("HH:mm:ss", TimeZone.getDefault());
  }
  public void testActivateOptions() {
    DateLayout layout = (DateLayout) createLayout();
    layout.setDateFormat("HH:mm:ss");
    layout.setTimeZone("+05:00");
    layout.activateOptions();
  }
  public void testSetDateFormatWithFormat() {
    DateFormat format = new SimpleDateFormat("HH:mm");
    DateLayout layout = (DateLayout) createLayout();
    layout.setDateFormat(format, TimeZone.getDefault());
  }
  public void testISO8601Format() {
      DateFormat format = new ISO8601DateFormat();
      Calendar calendar = Calendar.getInstance();
      calendar.clear();
      calendar.set(1970, 0, 1, 0, 0, 0);
      String actual = format.format(calendar.getTime());
      assertEquals("1970-01-01 00:00:00,000", actual);
  }
  public void testDateTimeFormat() {
      DateFormat format = new DateTimeDateFormat();
      Calendar calendar = Calendar.getInstance();
      calendar.clear();
      calendar.set(1970, 0, 1, 0, 0, 0);
      String actual = format.format(calendar.getTime());
      SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm:ss,SSS");
      String expected = df.format(calendar.getTime());
      assertEquals(expected, actual);
  }
  private static final class MockLayout extends DateLayout {
    public MockLayout() {
      assertNotNull(pos);
      assertNotNull(date);
      assertNull(dateFormat);
    }
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
