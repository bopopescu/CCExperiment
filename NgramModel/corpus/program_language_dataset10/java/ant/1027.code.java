package org.apache.tools.ant.taskdefs.condition;
import junit.framework.TestCase;
public class EqualsTest extends TestCase {
    public EqualsTest(String name) {
        super(name);
    }
    public void testTrim() {
        Equals eq = new Equals();
        eq.setArg1("a");
        eq.setArg2(" a");
        assertTrue(!eq.eval());
        eq.setTrim(true);
        assertTrue(eq.eval());
        eq.setArg2("a\t");
        assertTrue(eq.eval());
    }
    public void testCaseSensitive() {
        Equals eq = new Equals();
        eq.setArg1("a");
        eq.setArg2("A");
        assertTrue(!eq.eval());
        eq.setCasesensitive(false);
        assertTrue(eq.eval());
    }
}
