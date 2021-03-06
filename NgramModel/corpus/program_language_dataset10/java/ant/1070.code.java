package org.apache.tools.ant.taskdefs.optional.junit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.JavaEnvUtils;
public class JUnitTaskTest extends BuildFileTest {
    public JUnitTaskTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/optional/junit.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void testCrash() {
       expectPropertySet("crash", "crashed");
    }
    public void testNoCrash() {
       expectPropertyUnset("nocrash", "crashed");
    }
    public void testTimeout() {
       expectPropertySet("timeout", "timeout");
    }
    public void testNoTimeout() {
       expectPropertyUnset("notimeout", "timeout");
    }
    public void testNonForkedCapture() throws IOException {
        executeTarget("capture");
        assertNoPrint(getLog(), "log");
        assertNoPrint(getFullLog(), "debug log");
    }
    public void testForkedCapture() throws IOException {
        getProject().setProperty("fork", "true");
        testNonForkedCapture();
        assertNoPrint(getOutput(), "output");
        assertNoPrint(getError(), "error output");
        assertOutput();
    }
    public void testBatchTestForkOnceToDir() {
        assertResultFilesExist("testBatchTestForkOnceToDir", ".xml");
    }
    public void testBatchTestForkOnceExtension() {
        assertResultFilesExist("testBatchTestForkOnceExtension", ".foo");
    }
    public void testFailureRecorder() {
        if (JavaEnvUtils.isAtLeastJavaVersion(JavaEnvUtils.JAVA_1_5)) {
            try {
                Class.forName("junit.framework.JUnit4TestAdapter");
                System.err.println("skipping tests since it fails when"
                                   + " using JUnit 4");
                return;
            } catch (ClassNotFoundException e) {
            }
        }
        try {
            File testDir = new File(getProjectDir(), "out");
            File collectorFile = new File(getProjectDir(),
                                          "out/FailedTests.java");
            assertFalse("Test directory '" + testDir.getAbsolutePath()
                        + "' must not exist before the test preparation.", 
                        testDir.exists());
            assertFalse("The collector file '"
                        + collectorFile.getAbsolutePath()
                        + "'must not exist before the test preparation.", 
                        collectorFile.exists());
            executeTarget("failureRecorder.prepare");
            assertTrue("Test directory '" + testDir.getAbsolutePath()
                       + "' was not created.", testDir.exists());
            assertTrue("There should be one class.",
                       (new File(testDir, "A.class")).exists());
            assertFalse("The collector file '"
                        + collectorFile.getAbsolutePath() 
                        + "' should not exist before the 1st run.",
                        collectorFile.exists());
            executeTarget("failureRecorder.runtest");
            assertTrue("The collector file '" + collectorFile.getAbsolutePath() 
                       + "' should exist after the 1st run.",
                       collectorFile.exists());
            assertOutputContaining("1st run: should run A.test01", "A.test01");
            assertOutputContaining("1st run: should run B.test05", "B.test05");
            assertOutputContaining("1st run: should run B.test06", "B.test06");
            assertOutputContaining("1st run: should run C.test07", "C.test07");
            assertOutputContaining("1st run: should run C.test08", "C.test08");
            assertOutputContaining("1st run: should run C.test09", "C.test09");
            assertOutputContaining("1st run: should run A.test02", "A.test02");
            assertOutputContaining("1st run: should run A.test03", "A.test03");
            assertOutputContaining("1st run: should run B.test04", "B.test04");
            assertOutputContaining("1st run: should run D.test10", "D.test10");
            executeTarget("failureRecorder.runtest");
            assertTrue("The collector file '" + collectorFile.getAbsolutePath() 
                       + "' should exist after the 2nd run.",
                       collectorFile.exists());
            assertOutputNotContaining("2nd run: should not run A.test01",
                                      "A.test01");
            assertOutputNotContaining("2nd run: should not run A.test05",
                                      "B.test05");
            assertOutputNotContaining("2nd run: should not run B.test06",
                                      "B.test06");
            assertOutputNotContaining("2nd run: should not run C.test07",
                                      "C.test07");
            assertOutputNotContaining("2nd run: should not run C.test08",
                                      "C.test08");
            assertOutputNotContaining("2nd run: should not run C.test09",
                                      "C.test09");
            assertOutputContaining("2nd run: should run A.test02", "A.test02");
            assertOutputContaining("2nd run: should run A.test03", "A.test03");
            assertOutputContaining("2nd run: should run B.test04", "B.test04");
            assertOutputContaining("2nd run: should run D.test10", "D.test10");
            executeTarget("failureRecorder.fixing");
            executeTarget("failureRecorder.runtest");
            assertTrue("The collector file '" + collectorFile.getAbsolutePath() 
                       + "' should exist after the 3rd run.",
                       collectorFile.exists());
            assertOutputContaining("3rd run: should run A.test02", "A.test02");
            assertOutputContaining("3rd run: should run A.test03", "A.test03");
            assertOutputContaining("3rd run: should run B.test04", "B.test04");
            assertOutputContaining("3rd run: should run D.test10", "D.test10");
            executeTarget("failureRecorder.runtest");
            assertTrue("The collector file '" + collectorFile.getAbsolutePath() 
                       + "' should exist after the 4th run.",
                       collectorFile.exists());
            assertOutputContaining("4th run: should run B.test04", "B.test04");
            assertOutputContaining("4th run: should run D.test10", "D.test10");
        } catch (BuildException be) {
            be.printStackTrace();
            System.err.println("nested build's log: " + getLog());
            System.err.println("nested build's System.out: " + getOutput());
            System.err.println("nested build's System.err: " + getError());
            fail("Ant execution failed: " + be.getMessage());
        }
    }
    public void testBatchTestForkOnceCustomFormatter() {
        assertResultFilesExist("testBatchTestForkOnceCustomFormatter", "foo");
    }
    public void testMultilineAssertsNoFork() {
        expectLogNotContaining("testMultilineAssertsNoFork", "messed up)");
        assertLogNotContaining("crashed)");
    }
    public void testMultilineAssertsFork() {
        expectLogNotContaining("testMultilineAssertsFork", "messed up)");
        assertLogNotContaining("crashed)");
    }
    private void assertResultFilesExist(String target, String extension) {
        executeTarget(target);
        assertResultFileExists("JUnitClassLoader", extension);
        assertResultFileExists("JUnitTestRunner", extension);
        assertResultFileExists("JUnitVersionHelper", extension);
    }
    private void assertResultFileExists(String classNameFragment, String ext) {
        assertTrue("result for " + classNameFragment + "Test" + ext + " exists",
                   getProject().resolveFile("out/TEST-org.apache.tools.ant."
                                            + "taskdefs.optional.junit."
                                            + classNameFragment + "Test" + ext)
                   .exists());
    }
    private void assertNoPrint(String result, String where) {
        assertTrue(where + " '" + result + "' must not contain print statement",
                   result.indexOf("print to System.") == -1);
    }
    private void assertOutput() throws IOException {
        FileReader inner = new FileReader(getProject()
                                          .resolveFile("testlog.txt"));
        BufferedReader reader = new BufferedReader(inner);
        try {
            String line = reader.readLine();
            assertEquals("Testsuite: org.apache.tools.ant.taskdefs.optional.junit.Printer",
                         line);
            line = reader.readLine();
            assertNotNull(line);
            assertTrue(line.startsWith("Tests run: 1, Failures: 0, Errors: 0, Time elapsed:"));
            line = reader.readLine();
            assertEquals("------------- Standard Output ---------------",
                         line);
            assertPrint(reader.readLine(), "static", "out");
            assertPrint(reader.readLine(), "constructor", "out");
            assertPrint(reader.readLine(), "method", "out");
            line = reader.readLine();
            assertEquals("------------- ---------------- ---------------",
                         line);
            line = reader.readLine();
            assertEquals("------------- Standard Error -----------------",
                         line);
            assertPrint(reader.readLine(), "static", "err");
            assertPrint(reader.readLine(), "constructor", "err");
            assertPrint(reader.readLine(), "method", "err");
            line = reader.readLine();
            assertEquals("------------- ---------------- ---------------",
                         line);
            line = reader.readLine();
            assertEquals("", line);
            line = reader.readLine();
            assertNotNull(line);
            assertTrue(line.startsWith("Testcase: testNoCrash took "));
        } finally {
            inner.close();
        }
    }
    private void assertPrint(String line, String from, String to) {
        String search = from + " print to System." + to;
        assertEquals(search, line);
    }
}