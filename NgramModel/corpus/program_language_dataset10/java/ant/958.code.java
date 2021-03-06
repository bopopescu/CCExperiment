package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
public class DeltreeTest extends BuildFileTest {
    public DeltreeTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/deltree.xml");
    }
    public void test1() {
        expectBuildException("test1", "required argument not specified");
    }
    public void test2() {
        executeTarget("test2");
    }
}
