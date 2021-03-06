package org.apache.batik.xml;
public class XMLException extends RuntimeException {
    protected Exception exception;
    public XMLException (String message) {
        super(message);
        exception = null;
    }
    public XMLException (Exception e) {
        exception = e;
    }
    public XMLException (String message, Exception e) {
        super(message);
        exception = e;
    }
    public String getMessage () {
        String message = super.getMessage();
        if (message == null && exception != null) {
            return exception.getMessage();
        } else {
            return message;
        }
    }
    public Exception getException () {
        return exception;
    }
    public void printStackTrace() { 
        if (exception == null) {
            super.printStackTrace();
        } else {
            synchronized (System.err) {
                System.err.println(this);
                super.printStackTrace();
            }
        }
    }
    public void printStackTrace(java.io.PrintStream s) { 
        if (exception == null) {
            super.printStackTrace(s);
        } else {
            synchronized (s) {
                s.println(this);
                super.printStackTrace();
            }
        }
    }
    public void printStackTrace(java.io.PrintWriter s) { 
        if (exception == null) {
            super.printStackTrace(s);
        } else {
            synchronized (s) {
                s.println(this);
                super.printStackTrace(s);
            }
        }
    }
}
