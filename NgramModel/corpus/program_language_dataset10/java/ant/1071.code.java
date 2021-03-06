package org.apache.tools.ant.taskdefs.optional.junit;
import org.apache.tools.ant.BuildFileTest;
public class JUnitTestListenerTest extends BuildFileTest {
    private static final String PASS_TEST_TARGET = "captureToSummary";
    private static final String PASS_TEST = "testNoCrash";
    public JUnitTestListenerTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/optional/junit.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void testFullLogOutput() {
        getProject().setProperty("enableEvents", "true");
        executeTarget(PASS_TEST_TARGET);
        assertTrue("expecting full log to have BuildListener events", 
                   hasBuildListenerEvents(getFullLog()));
    }
    public void testNoLogOutput() {
        getProject().setProperty("enableEvents", "true");
        executeTarget(PASS_TEST_TARGET);
        assertFalse("expecting log to not have BuildListener events", 
                    hasBuildListenerEvents(getLog()));
    }
    public void testTestCountFired() {
        getProject().setProperty("enableEvents", "true");
        executeTarget(PASS_TEST_TARGET);
	assertTrue("expecting test count message",
		   hasEventMessage(JUnitTask.TESTLISTENER_PREFIX + 
				   "tests to run: "));
    }
    public void testStartTestFired() {
        getProject().setProperty("enableEvents", "true");
        executeTarget(PASS_TEST_TARGET);
	assertTrue("expecting test started message",
		   hasEventMessage(JUnitTask.TESTLISTENER_PREFIX + 
				   "startTest(" + PASS_TEST + ")"));
    }
    public void testEndTestFired() {
        getProject().setProperty("enableEvents", "true");
        executeTarget(PASS_TEST_TARGET);
	assertTrue("expecting test ended message",
		   hasEventMessage(JUnitTask.TESTLISTENER_PREFIX + 
				   "endTest(" + PASS_TEST + ")"));
    }
    public void testNoFullLogOutputByDefault() {
        executeTarget(PASS_TEST_TARGET);
        assertFalse("expecting full log to not have BuildListener events", 
                    hasBuildListenerEvents(getFullLog()));
    }
    public void testFullLogOutputMagicProperty() {
        getProject().setProperty(JUnitTask.ENABLE_TESTLISTENER_EVENTS, "true");
        executeTarget(PASS_TEST_TARGET);
        assertTrue("expecting full log to have BuildListener events", 
                   hasBuildListenerEvents(getFullLog()));
    }
    public void testNoFullLogOutputMagicPropertyWins() {
        getProject().setProperty(JUnitTask.ENABLE_TESTLISTENER_EVENTS, "false");
        getProject().setProperty("enableEvents", "true");
        executeTarget(PASS_TEST_TARGET);
        assertFalse("expecting full log to not have BuildListener events", 
                    hasBuildListenerEvents(getFullLog()));
    }
    private boolean hasBuildListenerEvents(String log) {
        return log.indexOf(JUnitTask.TESTLISTENER_PREFIX) >= 0;
    }
    private boolean hasEventMessage(String eventPrefix) {
	return getFullLog().indexOf(eventPrefix) >= 0;
    }
}
