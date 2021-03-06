package org.apache.tools.ant.taskdefs;
import java.util.Vector;
import org.apache.tools.ant.BuildFileTest;
public class CallTargetTest extends BuildFileTest {
    public CallTargetTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/calltarget.xml");
    }
    public void testInheritRefFileSet() {
        expectLogContaining("testinheritreffileset", "calltarget.xml");
    }
    public void testInheritFilterset() {
        project.executeTarget("testinheritreffilterset");
    }
    public void testMultiCall() {
        Vector v = new Vector();
        v.add("call-multi");
        v.add("call-multi");
        project.executeTargets(v);
        assertLogContaining("multi is SETmulti is SET");
    }
    public void testBlankTarget() {
        expectBuildException("blank-target", "target name must not be empty");
    }
    public void testMultipleTargets() {
        expectLog("multiple-targets", "tadadctbdbtc");
    }
    public void testMultipleTargets2() {
        expectLog("multiple-targets-2", "dadctb");
    }
    public void tearDown() {
        project.executeTarget("cleanup");
    }
}
