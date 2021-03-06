package org.apache.tools.ant;
public class ExitStatusException extends BuildException {
    private static final long serialVersionUID = 7760846806886585968L;
    private int status;
    public ExitStatusException(int status) {
        super();
        this.status = status;
    }
    public ExitStatusException(String msg, int status) {
        super(msg);
        this.status = status;
    }
    public ExitStatusException(String message, int status, Location location) {
        super(message, location);
        this.status = status;
    }
    public int getStatus() {
        return status;
    }
}
