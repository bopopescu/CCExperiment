package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.taskdefs.condition.Os;
public class DirnameTest extends BuildFileTest {
    public DirnameTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/dirname.xml");
    }
    public void test1() {
        expectBuildException("test1", "property attribute required");
    }
    public void test2() {
        expectBuildException("test2", "file attribute required");
    }
    public void test3() {
        expectBuildException("test3", "property attribute required");
    }
    public void test4() {
        if (Os.isFamily("netware") || Os.isFamily("dos")) {
            return;
        }
        executeTarget("test4");
        String filesep = System.getProperty("file.separator");
        String expected = filesep + "usr" + filesep + "local";
        String checkprop = project.getProperty("local.dir");
        if (!checkprop.equals(expected)) {
            fail("dirname failed");
        }
    }
    public void test5() {
        executeTarget("test5");
        String expected = project.getProperty("basedir");
        String checkprop = project.getProperty("base.dir");
        if (!checkprop.equals(expected)) {
            fail("dirname failed");
        }
    }
}
