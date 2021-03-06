package org.apache.batik.test;
public class TestException extends Exception {
    protected String errorCode;
    protected Object[] errorParams;
    protected Exception sourceError;
    public TestException(String errorCode,
                         Object[] errorParams,
                         Exception e){
        this.errorCode = errorCode;
        this.errorParams = errorParams;
        this.sourceError = e;
    }
    public String getErrorCode(){
        return errorCode;
    }
    public Object[] getErrorParams(){
        return errorParams;
    }
    public Exception getSourceError(){
        return sourceError;
    }
    public String getMessage(){
        return Messages.formatMessage(errorCode,
                                      errorParams);
    }
}
