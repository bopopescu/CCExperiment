package org.apache.tools.ant.types;
import junit.framework.TestCase;
import org.apache.tools.ant.BuildException;
public class EnumeratedAttributeTest extends TestCase {
    private static String[] expected = {"a", "b", "c"};
    public EnumeratedAttributeTest(String name) {
        super(name);
    }
    public void testContains() {
        EnumeratedAttribute t1 = new TestNormal();
        for (int i=0; i<expected.length; i++) {
            assertTrue(expected[i]+" is in TestNormal",
                   t1.containsValue(expected[i]));
            assertTrue(expected[i].toUpperCase()+" is in TestNormal",
                   !t1.containsValue(expected[i].toUpperCase()));
        }
        assertTrue("TestNormal doesn\'t have \"d\" attribute",
               !t1.containsValue("d"));
        assertTrue("TestNull doesn\'t have \"d\" attribute and doesn\'t die",
               !(new TestNull()).containsValue("d"));
    }
    public void testFactory() {
		Factory ea = (Factory)EnumeratedAttribute.getInstance(Factory.class, "one");
		assertEquals("Factory did not set the right value.", ea.getValue(), "one");
		try {
	    	EnumeratedAttribute.getInstance(Factory.class, "illegal");
	    	fail("Factory should fail when trying to set an illegal value.");
		} catch (BuildException be) {
		}
	}
	public void testExceptions() {
        EnumeratedAttribute t1 = new TestNormal();
        for (int i=0; i<expected.length; i++) {
            try {
                t1.setValue(expected[i]);
            } catch (BuildException be) {
                fail("unexpected exception for value "+expected[i]);
            }
        }
        try {
            t1.setValue("d");
            fail("expected exception for value \"d\"");
        } catch (BuildException be) {
        }
        try {
            (new TestNull()).setValue("d");
            fail("expected exception for value \"d\" in TestNull");
        } catch (BuildException be) {
        } catch (Throwable other) {
            fail("unexpected death of TestNull: "+other.getMessage());
        }
    }
    public static class TestNormal extends EnumeratedAttribute {
        public String[] getValues() {
            return expected;
        }
    }
    public static class TestNull extends EnumeratedAttribute {
        public String[] getValues() {
            return null;
        }
    }
    public static class Factory extends EnumeratedAttribute {
    	public String[] getValues() {
    		return new String[] { "one", "two", "three" };
    	}
    }
}
