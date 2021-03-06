package org.apache.log4j.pattern;
import junit.framework.TestCase;
public class FormattingInfoTest extends TestCase {
  public FormattingInfoTest(final String name) {
    super(name);
  }
  public void testGetDefault() {
    FormattingInfo field = FormattingInfo.getDefault();
    assertNotNull(field);
    assertEquals(0, field.getMinLength());
    assertEquals(Integer.MAX_VALUE, field.getMaxLength());
    assertEquals(false, field.isLeftAligned());
  }
  public void testConstructor() {
      FormattingInfo field = new FormattingInfo(true, 3, 6);
      assertNotNull(field);
      assertEquals(3, field.getMinLength());
      assertEquals(6, field.getMaxLength());
      assertEquals(true, field.isLeftAligned());
  }
  public void testTruncate() {
      StringBuffer buf = new StringBuffer("foobar");
      FormattingInfo field = new FormattingInfo(true, 0, 3);
      field.format(2, buf);
      assertEquals("fobar", buf.toString());
  }
    public void testPadLeft() {
        StringBuffer buf = new StringBuffer("foobar");
        FormattingInfo field = new FormattingInfo(false, 5, 10);
        field.format(2, buf);
        assertEquals("fo obar", buf.toString());
    }
    public void testPadRight() {
        StringBuffer buf = new StringBuffer("foobar");
        FormattingInfo field = new FormattingInfo(true, 5, 10);
        field.format(2, buf);
        assertEquals("foobar ", buf.toString());
    }
}
