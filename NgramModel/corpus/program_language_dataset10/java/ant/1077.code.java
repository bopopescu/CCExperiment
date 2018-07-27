package org.apache.tools.ant.taskdefs.optional.junit;
import junit.framework.Test;
import junit.framework.TestCase;
public class SuiteMethodTest {
    public static Test suite() {
        return new Nested("testMethod");
    }
    public static class Nested extends TestCase {
        public Nested(String name) {
            super(name);
        }
        public void testMethod() {
            assertTrue(true);
        }
    }
}
