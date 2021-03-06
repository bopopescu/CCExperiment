package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
public class UpToDateTest extends BuildFileTest {
    public UpToDateTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/uptodate.xml");
    }
    public void tearDown() {
        executeTarget("tearDown");
    }
    public void testFilesetUpToDate() {
        expectPropertySet("testFilesetUpToDate", "foo");
    }
    public void testFilesetOutOfDate() {
        expectPropertyUnset("testFilesetOutOfDate", "foo");
    }
    public void testRCUpToDate() {
        expectPropertySet("testRCUpToDate", "foo");
    }
    public void testRCOutOfDate() {
        expectPropertyUnset("testRCOutOfDate", "foo");
    }
}
