package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
public class GetTest extends BuildFileTest {
    public GetTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/get.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void test1() {
        expectBuildException("test1", "required argument missing");
    }
    public void test2() {
        expectBuildException("test2", "required argument missing");
    }
    public void test3() {
        expectBuildException("test3", "required argument missing");
    }
    public void test4() {
        expectBuildException("test4", "src invalid");
    }
    public void test5() {
        expectBuildException("test5", "dest invalid (or no http-server on local machine)");
    }
    public void test6() {
        executeTarget("test6");
    }
    public void testUseTimestamp() {
        executeTarget("testUseTimestamp");
    }
    public void testUseTomorrow() {
        executeTarget("testUseTomorrow");
    }
}
