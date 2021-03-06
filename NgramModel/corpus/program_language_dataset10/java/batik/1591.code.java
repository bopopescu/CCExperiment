package org.apache.batik.test;
import java.io.PrintWriter;
import java.io.StringWriter;
public class TestErrorConditionException extends Exception {
    protected String errorCode;
    protected TestErrorConditionException(){
    }
    public TestErrorConditionException(String errorCode){
        this.errorCode = errorCode;
    }
    public TestReport getTestReport(Test test){
        DefaultTestReport report = new DefaultTestReport(test);
        if(errorCode != null){
            report.setErrorCode(errorCode);
        } else {
            report.setErrorCode(TestReport.ERROR_TEST_FAILED);
        }
        report.setPassed(false);
        addStackTraceDescription(report);
        return report;
    }
    public void addStackTraceDescription(TestReport report){
        StringWriter trace = new StringWriter();
        printStackTrace(new PrintWriter(trace));
        report.addDescriptionEntry
          (TestReport.ENTRY_KEY_ERROR_CONDITION_STACK_TRACE,
           trace.toString());
    }
}
