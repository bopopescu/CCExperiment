package org.apache.tools.ant.taskdefs.optional.clearcase;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.Commandline;
public class CCUnCheckout extends ClearCase {
    private boolean mKeep = false;
    public void execute() throws BuildException {
        Commandline commandLine = new Commandline();
        Project aProj = getProject();
        int result = 0;
        if (getViewPath() == null) {
            setViewPath(aProj.getBaseDir().getPath());
        }
        commandLine.setExecutable(getClearToolCommand());
        commandLine.createArgument().setValue(COMMAND_UNCHECKOUT);
        checkOptions(commandLine);
        if (!getFailOnErr()) {
            getProject().log("Ignoring any errors that occur for: "
                    + getViewPathBasename(), Project.MSG_VERBOSE);
        }
        result = run(commandLine);
        if (Execute.isFailure(result) && getFailOnErr()) {
            String msg = "Failed executing: " + commandLine.toString();
            throw new BuildException(msg, getLocation());
        }
    }
    private void checkOptions(Commandline cmd) {
        if (getKeepCopy()) {
            cmd.createArgument().setValue(FLAG_KEEPCOPY);
        } else {
            cmd.createArgument().setValue(FLAG_RM);
        }
        cmd.createArgument().setValue(getViewPath());
    }
    public void setKeepCopy(boolean keep) {
        mKeep = keep;
    }
    public boolean getKeepCopy() {
        return mKeep;
    }
    public static final String FLAG_KEEPCOPY = "-keep";
    public static final String FLAG_RM = "-rm";
}