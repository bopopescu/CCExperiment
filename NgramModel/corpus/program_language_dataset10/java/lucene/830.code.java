package org.apache.lucene.ant;
import java.io.PrintStream;
import java.io.PrintWriter;
public class DocumentHandlerException extends Exception {
    private Throwable cause;
    public DocumentHandlerException() {
        super();
    }
    public DocumentHandlerException(String message) {
        super(message);
    }
    public DocumentHandlerException(Throwable cause) {
        super(cause.toString());
        this.cause = cause;
    }
    public Throwable getException() {
        return cause;
    }
    @Override
    public void printStackTrace() {
        printStackTrace(System.err);
    }
    @Override
    public void printStackTrace(PrintStream ps) {
        synchronized (ps) {
            super.printStackTrace(ps);
            if (cause != null) {
                ps.println("--- Nested Exception ---");
                cause.printStackTrace(ps);
            }
        }
    }
    @Override
    public void printStackTrace(PrintWriter pw) {
        synchronized (pw) {
            super.printStackTrace(pw);
            if (cause != null) {
                pw.println("--- Nested Exception ---");
                cause.printStackTrace(pw);
            }
        }
    }
}
