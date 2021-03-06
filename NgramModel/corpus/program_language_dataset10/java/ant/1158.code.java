package org.apache.tools.ant.util;
import junit.framework.TestCase;
public class UnicodeUtilTest extends TestCase {
    public void testChineseWord() {
        String word = "\u81ea\u7531";
        assertEquals("u81ea", UnicodeUtil.EscapeUnicode(word.charAt(0)).toString());
        assertEquals("u7531", UnicodeUtil.EscapeUnicode(word.charAt(1)).toString());
    }
}
