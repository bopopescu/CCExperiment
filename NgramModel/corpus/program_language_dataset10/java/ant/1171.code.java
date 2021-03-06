package org.apache.tools.ant.util.regexp;
public abstract class RegexpTest extends RegexpMatcherTest {
    private static final String test = "abcdefg-abcdefg";
    private static final String pattern = "ab([^d]*)d([^f]*)f";
    public RegexpTest(String name) {
        super(name);
    }
    public final RegexpMatcher getImplementation() {
        return getRegexpImplementation();
    }
    public abstract Regexp getRegexpImplementation();
    public void testSubstitution() {
        Regexp reg = (Regexp) getReg();
        reg.setPattern(pattern);
        assertTrue(reg.matches(test));
        assertEquals("abedcfg-abcdefg", reg.substitute(test, "ab\\2d\\1f",
                                                       Regexp.MATCH_DEFAULT));
    }
    public void testReplaceFirstSubstitution() {
        Regexp reg = (Regexp) getReg();
        reg.setPattern(pattern);
        assertTrue(reg.matches(test));
        assertEquals("abedcfg-abcdefg", reg.substitute(test, "ab\\2d\\1f",
                                                       Regexp.REPLACE_FIRST));
    }
    public void testReplaceAllSubstitution() {
        Regexp reg = (Regexp) getReg();
        reg.setPattern(pattern);
        assertTrue(reg.matches(test));
        assertEquals("abedcfg-abedcfg", reg.substitute(test, "ab\\2d\\1f",
                                                       Regexp.REPLACE_ALL));
    }
}
