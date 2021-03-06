package org.apache.tools.ant.taskdefs.optional.junit;
import java.io.IOException;
import java.io.OutputStream;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.apache.tools.ant.BuildException;
public class TestFormatter implements JUnitResultFormatter {
    private static final byte[] grafitto = new byte[] {
        (byte) 'T', (byte) 'e', (byte) 's', (byte) 't', (byte) 'F', (byte) 'o',
        (byte) 'r', (byte) 'm', (byte) 'a', (byte) 't', (byte) 't', (byte) 'e',
        (byte) 'r', (byte) ' ', (byte) 'w', (byte) 'a', (byte) 's', (byte) ' ',
        (byte) 'h', (byte) 'e', (byte) 'r', (byte) 'e', 10
    };
    private OutputStream out;
    public TestFormatter() {
    }
    public void startTestSuite(JUnitTest suite) {
    }
    public void startTest(Test t) {
    }
    public void endTest(Test test) {
    }
    public void addFailure(Test test, Throwable t) {
    }
    public void addFailure(Test test, AssertionFailedError t) {
    }
    public void addError(Test test, Throwable t) {
    }
    public void setSystemOutput(String out) {
    }
    public void setSystemError(String err) {
    }
    public void setOutput(OutputStream out) {
        this.out = out;
    }
    public void endTestSuite(JUnitTest suite) throws BuildException {
        if (out != null) {
            try {
                out.write(grafitto);
                out.flush();
            } catch (IOException ioex) {
                throw new BuildException("Unable to write output", ioex);
            } finally {
                if (out != System.out && out != System.err) {
                    try {
                        out.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }
}
