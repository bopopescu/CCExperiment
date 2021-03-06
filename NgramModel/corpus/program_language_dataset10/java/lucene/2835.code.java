package org.apache.solr.schema;
import org.apache.solr.schema.DateField;
import org.apache.solr.util.DateMathParser;
import org.apache.lucene.document.Fieldable;
import java.util.Date;
import java.util.TimeZone;
import java.util.Locale;
import java.text.DateFormat;
import junit.framework.TestCase;
public class LegacyDateFieldTest extends TestCase {
  public static TimeZone UTC = TimeZone.getTimeZone("UTC");
  protected DateField f = null;
  protected DateMathParser p = null;
  public void setUp() throws Exception {
    super.setUp();
    p = new DateMathParser(UTC, Locale.US);
    f = new DateField();
    try {
      Class clazz = Class.forName("org.apache.solr.schema.LegacyDateField");
      f = (DateField) clazz.newInstance();
    } catch (ClassNotFoundException ignored) {
    }
  }
  public void assertToI(String expected, String input) {
    assertEquals("Input: " + input, expected, f.toInternal(input));
  }
  public void testToInternal() throws Exception {
    assertToI("1995-12-31T23:59:59.999", "1995-12-31T23:59:59.999Z");
    assertToI("1995-12-31T23:59:59.99",  "1995-12-31T23:59:59.99Z");
    assertToI("1995-12-31T23:59:59.9",   "1995-12-31T23:59:59.9Z");
    assertToI("1995-12-31T23:59:59",     "1995-12-31T23:59:59Z");
    assertToI("1995-12-31T23:59:59.9998", "1995-12-31T23:59:59.9998Z");
    assertToI("1995-12-31T23:59:59.9990", "1995-12-31T23:59:59.9990Z");
    assertToI("1995-12-31T23:59:59.990",  "1995-12-31T23:59:59.990Z");
    assertToI("1995-12-31T23:59:59.900",  "1995-12-31T23:59:59.900Z");
    assertToI("1995-12-31T23:59:59.90",   "1995-12-31T23:59:59.90Z");
    assertToI("1995-12-31T23:59:59.000",  "1995-12-31T23:59:59.000Z");
    assertToI("1995-12-31T23:59:59.00",   "1995-12-31T23:59:59.00Z");
    assertToI("1995-12-31T23:59:59.0",    "1995-12-31T23:59:59.0Z");
  }
  public void assertToI(String expected, long input) {
    assertEquals("Input: " + input, expected, f.toInternal(new Date(input)));
  }
  public void testToInternalObj() throws Exception {
    assertToI("1995-12-31T23:59:59.999", 820454399999l);
    assertToI("1995-12-31T23:59:59.990",  820454399990l);
    assertToI("1995-12-31T23:59:59.900",  820454399900l);
    assertToI("1995-12-31T23:59:59.000",  820454399000l);
  }
  public void assertItoR(String expected, String input) {
    assertEquals("Input: " + input, expected, f.indexedToReadable(input));
  }
  public void testIndexedToReadable() {
    assertItoR("1995-12-31T23:59:59.999Z", "1995-12-31T23:59:59.999");
    assertItoR("1995-12-31T23:59:59.99Z",  "1995-12-31T23:59:59.99");
    assertItoR("1995-12-31T23:59:59.9Z",   "1995-12-31T23:59:59.9");
    assertItoR("1995-12-31T23:59:59Z",     "1995-12-31T23:59:59");
  }
  public void testFormatter() {
    assertEquals("1970-01-01T00:00:00.005", f.formatDate(new Date(5)));
    assertEquals("1970-01-01T00:00:00.000", f.formatDate(new Date(0)));
    assertEquals("1970-01-01T00:00:00.370", f.formatDate(new Date(370)));
    assertEquals("1970-01-01T00:00:00.900", f.formatDate(new Date(900)));
  }
}
