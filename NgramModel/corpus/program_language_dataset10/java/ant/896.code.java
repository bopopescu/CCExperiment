package org.apache.tools.ant;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import junit.framework.TestCase;
public abstract class BuildFileTest extends TestCase {
    protected Project project;
    private StringBuffer logBuffer;
    private StringBuffer fullLogBuffer;
    private StringBuffer outBuffer;
    private StringBuffer errBuffer;
    private BuildException buildException;
    public BuildFileTest() {
        super();
    }
    public BuildFileTest(String name) {
        super(name);
    }
    protected void tearDown() throws Exception {
        if (project == null) {
            return;
        }
        final String tearDown = "tearDown";
        if (project.getTargets().containsKey(tearDown)) {
            project.executeTarget(tearDown);
        }
    }
    public void expectBuildException(String target, String cause) {
        expectSpecificBuildException(target, cause, null);
    }
    public void expectLog(String target, String log) {
        executeTarget(target);
        String realLog = getLog();
        assertEquals(log, realLog);
    }
    public void assertLogContaining(String substring) {
        String realLog = getLog();
        assertTrue("expecting log to contain \"" + substring + "\" log was \""
                   + realLog + "\"",
                   realLog.indexOf(substring) >= 0);
    }
    public void assertLogNotContaining(String substring) {
        String realLog = getLog();
        assertFalse("didn't expect log to contain \"" + substring + "\" log was \""
                    + realLog + "\"",
                    realLog.indexOf(substring) >= 0);
    }
    public void assertOutputContaining(String substring) {
        assertOutputContaining(null, substring);
    }
    public void assertOutputContaining(String message, String substring) {
        String realOutput = getOutput();
        String realMessage = (message != null) 
            ? message 
            : "expecting output to contain \"" + substring + "\" output was \"" + realOutput + "\"";
        assertTrue(realMessage, realOutput.indexOf(substring) >= 0);
    }
    public void assertOutputNotContaining(String message, String substring) {
        String realOutput = getOutput();
        String realMessage = (message != null) 
            ? message 
            : "expecting output to not contain \"" + substring + "\" output was \"" + realOutput + "\"";
        assertFalse(realMessage, realOutput.indexOf(substring) >= 0);
    }
    public void expectLogContaining(String target, String log) {
        executeTarget(target);
        assertLogContaining(log);
    }
    public void expectLogNotContaining(String target, String log) {
        executeTarget(target);
        assertLogNotContaining(log);
    }
    public String getLog() {
        return logBuffer.toString();
    }
    public void expectDebuglog(String target, String log) {
        executeTarget(target);
        String realLog = getFullLog();
        assertEquals(log, realLog);
    }
    public void assertDebuglogContaining(String substring) {
        String realLog = getFullLog();
        assertTrue("expecting debug log to contain \"" + substring 
                   + "\" log was \""
                   + realLog + "\"",
                   realLog.indexOf(substring) >= 0);
    }
    public String getFullLog() {
        return fullLogBuffer.toString();
    }
    public void expectOutput(String target, String output) {
        executeTarget(target);
        String realOutput = getOutput();
        assertEquals(output, realOutput.trim());
    }
    public void expectOutputAndError(String target, String output, String error) {
        executeTarget(target);
        String realOutput = getOutput();
        assertEquals(output, realOutput);
        String realError = getError();
        assertEquals(error, realError);
    }
    public String getOutput() {
        return cleanBuffer(outBuffer);
    }
    public String getError() {
        return cleanBuffer(errBuffer);
    }
    public BuildException getBuildException() {
        return buildException;
    }
    private String cleanBuffer(StringBuffer buffer) {
        StringBuffer cleanedBuffer = new StringBuffer();
        for (int i = 0; i < buffer.length(); i++) {
            char ch = buffer.charAt(i);
            if (ch != '\r') {
                cleanedBuffer.append(ch);
            }
        }
        return cleanedBuffer.toString();
    }
    public void configureProject(String filename) throws BuildException {
        configureProject(filename, Project.MSG_DEBUG);
    }
    public void configureProject(String filename, int logLevel)
        throws BuildException {
        logBuffer = new StringBuffer();
        fullLogBuffer = new StringBuffer();
        project = new Project();
        project.init();
        File antFile = new File(System.getProperty("root"), filename);
        project.setUserProperty("ant.file" , antFile.getAbsolutePath());
        project.addBuildListener(new AntTestListener(logLevel));
        ProjectHelper.configureProject(project, antFile);
    }
    public void executeTarget(String targetName) {
        PrintStream sysOut = System.out;
        PrintStream sysErr = System.err;
        try {
            sysOut.flush();
            sysErr.flush();
            outBuffer = new StringBuffer();
            PrintStream out = new PrintStream(new AntOutputStream(outBuffer));
            System.setOut(out);
            errBuffer = new StringBuffer();
            PrintStream err = new PrintStream(new AntOutputStream(errBuffer));
            System.setErr(err);
            logBuffer = new StringBuffer();
            fullLogBuffer = new StringBuffer();
            buildException = null;
            project.executeTarget(targetName);
        } finally {
            System.setOut(sysOut);
            System.setErr(sysErr);
        }
    }
    public Project getProject() {
        return project;
    }
    public File getProjectDir() {
        return project.getBaseDir();
    }
    public void expectSpecificBuildException(String target, String cause, String msg) {
        try {
            executeTarget(target);
        } catch (org.apache.tools.ant.BuildException ex) {
            buildException = ex;
            if ((null != msg) && (!ex.getMessage().equals(msg))) {
                fail("Should throw BuildException because '" + cause
                        + "' with message '" + msg
                        + "' (actual message '" + ex.getMessage() + "' instead)");
            }
            return;
        }
        fail("Should throw BuildException because: " + cause);
    }
    public void expectBuildExceptionContaining(String target, String cause, String contains) {
        try {
            executeTarget(target);
        } catch (org.apache.tools.ant.BuildException ex) {
            buildException = ex;
            if ((null != contains) && (ex.getMessage().indexOf(contains) == -1)) {
                fail("Should throw BuildException because '" + cause + "' with message containing '" + contains + "' (actual message '" + ex.getMessage() + "' instead)");
            }
            return;
        }
        fail("Should throw BuildException because: " + cause);
    }
    public void expectPropertySet(String target, String property, String value) {
        executeTarget(target);
        assertPropertyEquals(property, value);
    }
    public void assertPropertyEquals(String property, String value) {
        String result = project.getProperty(property);
        assertEquals("property " + property,value,result);
    }
    public  void assertPropertySet(String property) {
        assertPropertyEquals(property, "true");
    }
    public void assertPropertyUnset(String property) {
        String result = project.getProperty(property);
        if (result != null) {
            fail("Expected property " + property
                    + " to be unset, but it is set to the value: " + result);
        }
    }
    public  void expectPropertySet(String target, String property) {
        expectPropertySet(target, property, "true");
    }
    public  void expectPropertyUnset(String target, String property) {
        expectPropertySet(target, property, null);
    }
    public  URL getResource(String resource){
        URL url = getClass().getResource(resource);
        assertNotNull("Could not find resource :" + resource, url);
        return url;
    }
    protected static class AntOutputStream extends java.io.OutputStream {
        private StringBuffer buffer;
        public AntOutputStream( StringBuffer buffer ) {
            this.buffer = buffer;
        }
        public void write(int b) {
            buffer.append((char)b);
        }
    }
    private class AntTestListener implements BuildListener {
        private int logLevel;
        public AntTestListener(int logLevel) {
            this.logLevel = logLevel;
        }
        public void buildStarted(BuildEvent event) {
        }
        public void buildFinished(BuildEvent event) {
        }
        public void targetStarted(BuildEvent event) {
        }
        public void targetFinished(BuildEvent event) {
        }
        public void taskStarted(BuildEvent event) {
        }
        public void taskFinished(BuildEvent event) {
        }
        public void messageLogged(BuildEvent event) {
            if (event.getPriority() > logLevel) {
                return;
            }
            if (event.getPriority() == Project.MSG_INFO ||
                event.getPriority() == Project.MSG_WARN ||
                event.getPriority() == Project.MSG_ERR) {
                logBuffer.append(event.getMessage());
            }
            fullLogBuffer.append(event.getMessage());
        }
    }
}
