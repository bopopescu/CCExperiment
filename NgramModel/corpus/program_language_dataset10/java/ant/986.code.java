package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
public class MkdirTest extends BuildFileTest {
    public MkdirTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/mkdir.xml");
    }
    public void test1() {
        expectBuildException("test1", "required argument missing");
    }
    public void test2() {
        expectBuildException("test2", "directory already exists as a file");
    }
    public void test3() {
        executeTarget("test3");
        java.io.File f = new java.io.File(getProjectDir(), "testdir.tmp");
        if (!f.exists() || !f.isDirectory()) {
            fail("mkdir failed");
        } else {
            f.delete();
        }
    }
}
