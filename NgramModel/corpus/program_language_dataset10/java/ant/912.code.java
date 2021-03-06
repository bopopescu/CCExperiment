package org.apache.tools.ant;
import org.apache.tools.ant.BuildFileTest;
public class ImmutableTest extends BuildFileTest {
    public ImmutableTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/core/immutable.xml");
    }
    public void test1() {
        executeTarget("test1");
        assertEquals("override", project.getProperty("test"));
    }
    public void test2() {
        executeTarget("test2");
        assertNotNull(project.getProperty("DSTAMP"));
        assertNotNull(project.getProperty("start.DSTAMP"));
    }
    public void test3() {
        executeTarget("test3");
        assertEquals("original", project.getProperty("DSTAMP"));
    }
    public void test4() {
        executeTarget("test4");
        assertEquals("original", project.getProperty("test"));
    }
    public void test5() {
        executeTarget("test5");
        assertEquals("original", project.getProperty("test"));
    }
    public void test6() {
        executeTarget("test6");
        assertEquals("original", project.getProperty("test1"));
        assertEquals("original", project.getProperty("test2"));
    }
    public void test7() {
        executeTarget("test7");
        assertEquals("original", project.getProperty("test"));
    }
}
