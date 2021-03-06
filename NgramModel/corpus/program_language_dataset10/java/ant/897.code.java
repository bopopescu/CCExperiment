package org.apache.tools.ant;
import org.apache.tools.ant.BuildFileTest;
public class CaseTest extends BuildFileTest {
    public CaseTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/core/case.xml");
    }
    public void testCaseSensitivity() {
        executeTarget("case-sensitivity");
    }
    public void testTaskCase() {
        expectBuildExceptionContaining("taskcase",
            "Task names are case sensitive",
            "Problem: failed to create task or type ecHO");
    }
}
