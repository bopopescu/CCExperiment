package org.apache.tools.ant.taskdefs.optional.ejb;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
public class BorlandGenerateClient extends Task {
    static final String JAVA_MODE = "java";
    static final String FORK_MODE = "fork";
    boolean debug = false;
    File ejbjarfile = null;
    File clientjarfile = null;
    Path classpath;
    String mode = FORK_MODE;
    int version = BorlandDeploymentTool.BAS;
    public void setVersion(int version) {
        this.version = version;
    }
    public void setMode(String s) {
        mode = s;
    }
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    public void setEjbjar(File ejbfile) {
        ejbjarfile = ejbfile;
    }
    public void setClientjar(File clientjar) {
        clientjarfile = clientjar;
    }
    public void setClasspath(Path classpath) {
        if (this.classpath == null) {
            this.classpath = classpath;
        } else {
            this.classpath.append(classpath);
        }
    }
    public Path createClasspath() {
        if (this.classpath == null) {
            this.classpath = new Path(getProject());
        }
        return this.classpath.createPath();
    }
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }
    public void execute() throws BuildException {
        if (ejbjarfile == null || ejbjarfile.isDirectory()) {
            throw new BuildException("invalid ejb jar file.");
        }
        if (clientjarfile == null || clientjarfile.isDirectory()) {
            log("invalid or missing client jar file.", Project.MSG_VERBOSE);
            String ejbjarname = ejbjarfile.getAbsolutePath();
            String clientname = ejbjarname.substring(0, ejbjarname.lastIndexOf("."));
            clientname = clientname + "client.jar";
            clientjarfile = new File(clientname);
        }
        if (mode == null) {
            log("mode is null default mode  is java");
            setMode(JAVA_MODE);
        }
        if (!(version == BorlandDeploymentTool.BES
            || version == BorlandDeploymentTool.BAS)) {
            throw new BuildException("version " + version
                                      + " is not supported");
        }
        log("client jar file is " + clientjarfile);
        if (mode.equalsIgnoreCase(FORK_MODE)) {
            executeFork();
        } else {
            executeJava();
        } 
    }
    protected void executeJava() throws BuildException {
        try {
            if (version == BorlandDeploymentTool.BES)  {
                throw new BuildException("java mode is supported only for "
                    + "previous version <=" + BorlandDeploymentTool.BAS);
            }
            log("mode : java");
            Java execTask = null;
            execTask = new Java(this);
            execTask.setDir(new File("."));
            execTask.setClassname("com.inprise.server.commandline.EJBUtilities");
            execTask.setClasspath(classpath.concatSystemClasspath());
            execTask.setFork(true);
            execTask.createArg().setValue("generateclient");
            if (debug) {
                execTask.createArg().setValue("-trace");
            }
            execTask.createArg().setValue("-short");
            execTask.createArg().setValue("-jarfile");
            execTask.createArg().setValue(ejbjarfile.getAbsolutePath());
            execTask.createArg().setValue("-single");
            execTask.createArg().setValue("-clientjarfile");
            execTask.createArg().setValue(clientjarfile.getAbsolutePath());
            log("Calling EJBUtilities", Project.MSG_VERBOSE);
            execTask.execute();
        } catch (Exception e) {
            String msg = "Exception while calling generateclient Details: " + e.toString();
            throw new BuildException(msg, e);
        }
    }
    protected  void executeFork() throws BuildException {
        if (version == BorlandDeploymentTool.BAS) {
            executeForkV4();
        }
        if (version == BorlandDeploymentTool.BES) {
            executeForkV5();
        }
    }
    protected  void executeForkV4() throws BuildException {
        try {
            log("mode : fork " + BorlandDeploymentTool.BAS, Project.MSG_DEBUG);
            ExecTask execTask = new ExecTask(this);
            execTask.setDir(new File("."));
            execTask.setExecutable("iastool");
            execTask.createArg().setValue("generateclient");
            if (debug) {
                execTask.createArg().setValue("-trace");
            }
            execTask.createArg().setValue("-short");
            execTask.createArg().setValue("-jarfile");
            execTask.createArg().setValue(ejbjarfile.getAbsolutePath());
            execTask.createArg().setValue("-single");
            execTask.createArg().setValue("-clientjarfile");
            execTask.createArg().setValue(clientjarfile.getAbsolutePath());
            log("Calling iastool", Project.MSG_VERBOSE);
            execTask.execute();
        } catch (Exception e) {
            String msg = "Exception while calling generateclient Details: "
                + e.toString();
            throw new BuildException(msg, e);
        }
    }
    protected  void executeForkV5() throws BuildException {
        try {
            log("mode : fork " + BorlandDeploymentTool.BES, Project.MSG_DEBUG);
            ExecTask execTask = new ExecTask(this);
            execTask.setDir(new File("."));
            execTask.setExecutable("iastool");
            if (debug) {
                execTask.createArg().setValue("-debug");
            }
            execTask.createArg().setValue("-genclient");
            execTask.createArg().setValue("-jars");
            execTask.createArg().setValue(ejbjarfile.getAbsolutePath());
            execTask.createArg().setValue("-target");
            execTask.createArg().setValue(clientjarfile.getAbsolutePath());
            execTask.createArg().setValue("-cp");
            execTask.createArg().setValue(classpath.toString());
            log("Calling iastool", Project.MSG_VERBOSE);
            execTask.execute();
        } catch (Exception e) {
            String msg = "Exception while calling generateclient Details: "
                + e.toString();
            throw new BuildException(msg, e);
        }
    }
}