package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Commandline;
import junit.framework.TestCase;
public class ExecuteJavaTest extends TestCase {
    private final static int TIME_OUT = 5000;
    private final static int CLOCK_ERROR=200;
    private final static int TIME_OUT_TEST=TIME_OUT-CLOCK_ERROR;
    private ExecuteJava ej;
    private Project project;
    private Path cp;
    public ExecuteJavaTest(String name) {
        super(name);
    }
    protected void setUp(){
        ej = new ExecuteJava();
        ej.setTimeout(new Long(TIME_OUT));
        project = new Project();
        project.setBasedir(".");
        project.setProperty(MagicNames.ANT_HOME, System.getProperty(MagicNames.ANT_HOME));
        cp = new Path(project, getTestClassPath());
        ej.setClasspath(cp);
    }
    private Commandline getCommandline(int timetorun) throws Exception {
        Commandline cmd = new Commandline();
        cmd.setExecutable(TimeProcess.class.getName());
        cmd.createArgument().setValue(String.valueOf(timetorun));
        return cmd;
    }
    public void testNoTimeOut() throws Exception {
        Commandline cmd = getCommandline(TIME_OUT/2);
        ej.setJavaCommand(cmd);
        ej.execute(project);
        assertTrue("process should not have been killed", !ej.killedProcess());
    }
    public void testTimeOut() throws Exception {
        Commandline cmd = getCommandline(TIME_OUT*2);
        ej.setJavaCommand(cmd);
        long now = System.currentTimeMillis();
        ej.execute(project);
        long elapsed = System.currentTimeMillis() - now;
        assertTrue("process should have been killed", ej.killedProcess());
        assertTrue("elapse time of "+elapsed
                   +" ms is less than timeout value of "+TIME_OUT_TEST+" ms",
                   elapsed >= TIME_OUT_TEST);
        assertTrue("elapse time of "+elapsed
                   +" ms is greater than run value of "+(TIME_OUT*2)+" ms",
                   elapsed < TIME_OUT*2);
    }
    public void testNoTimeOutForked() throws Exception {
        Commandline cmd = getCommandline(TIME_OUT/2);
        ej.setJavaCommand(cmd);
        ej.fork(cp);
        assertTrue("process should not have been killed", !ej.killedProcess());
    }
    public void testTimeOutForked() throws Exception {
        Commandline cmd = getCommandline(TIME_OUT*2);
        ej.setJavaCommand(cmd);
        long now = System.currentTimeMillis();
        ej.fork(cp);
        long elapsed = System.currentTimeMillis() - now;
        assertTrue("process should have been killed", ej.killedProcess());
        assertTrue("elapse time of "+elapsed
                   +" ms is less than timeout value of "+TIME_OUT_TEST+" ms",
                   elapsed >= TIME_OUT_TEST);
        assertTrue("elapse time of "+elapsed
                   +" ms is greater than run value of "+(TIME_OUT*2)+" ms",
                   elapsed < TIME_OUT*2);
    }
    private static String getTestClassPath(){
        String classpath = System.getProperty("build.tests");
        if (classpath == null) {
            System.err.println("WARNING: 'build.tests' property is not available !");
            classpath = System.getProperty("java.class.path");
        }
        return classpath;
    }
}
