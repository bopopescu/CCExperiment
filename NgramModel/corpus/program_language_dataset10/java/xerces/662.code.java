package org.apache.xerces.xni;
public class XNIException 
    extends RuntimeException {
    static final long serialVersionUID = 9019819772686063775L;
    private Exception fException = this;
    public XNIException(String message) {
        super(message);
    } 
    public XNIException(Exception exception) {
        super(exception.getMessage());
        fException = exception;
    } 
    public XNIException(String message, Exception exception) {
        super(message);
        fException = exception;
    } 
    public Exception getException() {
        return fException != this ? fException : null;
    } 
    public synchronized Throwable initCause(Throwable throwable) {
        if (fException != this) {
            throw new IllegalStateException();
        }
        if (throwable == this) {
            throw new IllegalArgumentException();
        }
        fException = (Exception) throwable;
        return this;
    } 
    public Throwable getCause() {
        return getException();
    } 
} 
