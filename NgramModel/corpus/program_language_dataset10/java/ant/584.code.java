package org.apache.tools.ant.taskdefs.rmic;
import java.lang.reflect.Method;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Commandline;
public class WLRmic extends DefaultRmicAdapter {
    public static final String WLRMIC_CLASSNAME = "weblogic.rmic";
    public static final String COMPILER_NAME = "weblogic";
    public static final String ERROR_NO_WLRMIC_ON_CLASSPATH =
        "Cannot use WebLogic rmic, as it is not "
        + "available. Add it to Ant's classpath with the -lib option";
    public static final String ERROR_WLRMIC_FAILED = "Error starting WebLogic rmic: ";
    public static final String WL_RMI_STUB_SUFFIX = "_WLStub";
    public static final String WL_RMI_SKEL_SUFFIX = "_WLSkel";
    public static final String UNSUPPORTED_STUB_OPTION = "Unsupported stub option: ";
    public boolean execute() throws BuildException {
        getRmic().log("Using WebLogic rmic", Project.MSG_VERBOSE);
        Commandline cmd = setupRmicCommand(new String[] {"-noexit"});
        AntClassLoader loader = null;
        try {
            Class c = null;
            if (getRmic().getClasspath() == null) {
                c = Class.forName(WLRMIC_CLASSNAME);
            } else {
                loader
                    = getRmic().getProject().createClassLoader(getRmic().getClasspath());
                c = Class.forName(WLRMIC_CLASSNAME, true, loader);
            }
            Method doRmic = c.getMethod("main",
                                        new Class [] {String[].class});
            doRmic.invoke(null, new Object[] {cmd.getArguments()});
            return true;
        } catch (ClassNotFoundException ex) {
            throw new BuildException(ERROR_NO_WLRMIC_ON_CLASSPATH, getRmic().getLocation());
        } catch (Exception ex) {
            if (ex instanceof BuildException) {
                throw (BuildException) ex;
            } else {
                throw new BuildException(ERROR_WLRMIC_FAILED, ex,
                                         getRmic().getLocation());
            }
        } finally {
            if (loader != null) {
                loader.cleanup();
            }
        }
    }
    public String getStubClassSuffix() {
        return WL_RMI_STUB_SUFFIX;
    }
    public String getSkelClassSuffix() {
        return WL_RMI_SKEL_SUFFIX;
    }
    protected String[] preprocessCompilerArgs(String[] compilerArgs) {
        return filterJvmCompilerArgs(compilerArgs);
    }
    protected String addStubVersionOptions() {
        String stubVersion = getRmic().getStubVersion();
        if (null != stubVersion) {
            getRmic().log(UNSUPPORTED_STUB_OPTION + stubVersion,
                          Project.MSG_WARN);
        }
        return null;
    }
}