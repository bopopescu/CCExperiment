package org.apache.tools.ant.taskdefs.optional.junit;
import java.io.OutputStream;
import junit.framework.TestListener;
import org.apache.tools.ant.BuildException;
public interface JUnitResultFormatter
    extends TestListener, JUnitTaskMirror.JUnitResultFormatterMirror {
    void startTestSuite(JUnitTest suite) throws BuildException;
    void endTestSuite(JUnitTest suite) throws BuildException;
    void setOutput(OutputStream out);
    void setSystemOutput(String out);
    void setSystemError(String err);
}