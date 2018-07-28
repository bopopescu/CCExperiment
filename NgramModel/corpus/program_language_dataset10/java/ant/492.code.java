package org.apache.tools.ant.taskdefs.optional.junit;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.apache.tools.ant.BuildException;
public class SummaryJUnitResultFormatter
    implements JUnitResultFormatter, JUnitTaskMirror.SummaryJUnitResultFormatterMirror {
    private static final double ONE_SECOND = 1000.0;
    private NumberFormat nf = NumberFormat.getInstance();
    private OutputStream out;
    private boolean withOutAndErr = false;
    private String systemOutput = null;
    private String systemError = null;
    public SummaryJUnitResultFormatter() {
    }
    public void startTestSuite(JUnitTest suite) {
        String newLine = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer("Running ");
        sb.append(suite.getName());
        sb.append(newLine);
        try {
            out.write(sb.toString().getBytes());
            out.flush();
        } catch (IOException ioex) {
            throw new BuildException("Unable to write summary output", ioex);
        }
    }
    public void startTest(Test t) {
    }
    public void endTest(Test test) {
    }
    public void addFailure(Test test, Throwable t) {
    }
    public void addFailure(Test test, AssertionFailedError t) {
        addFailure(test, (Throwable) t);
    }
    public void addError(Test test, Throwable t) {
    }
    public void setOutput(OutputStream out) {
        this.out = out;
    }
    public void setSystemOutput(String out) {
        systemOutput = out;
    }
    public void setSystemError(String err) {
        systemError = err;
    }
    public void setWithOutAndErr(boolean value) {
        withOutAndErr = value;
    }
    public void endTestSuite(JUnitTest suite) throws BuildException {
        String newLine = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer("Tests run: ");
        sb.append(suite.runCount());
        sb.append(", Failures: ");
        sb.append(suite.failureCount());
        sb.append(", Errors: ");
        sb.append(suite.errorCount());
        sb.append(", Time elapsed: ");
        sb.append(nf.format(suite.getRunTime() / ONE_SECOND));
        sb.append(" sec");
        sb.append(newLine);
        if (withOutAndErr) {
            if (systemOutput != null && systemOutput.length() > 0) {
                sb.append("Output:").append(newLine).append(systemOutput)
                    .append(newLine);
            }
            if (systemError != null && systemError.length() > 0) {
                sb.append("Error: ").append(newLine).append(systemError)
                    .append(newLine);
            }
        }
        try {
            out.write(sb.toString().getBytes());
            out.flush();
        } catch (IOException ioex) {
            throw new BuildException("Unable to write summary output", ioex);
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