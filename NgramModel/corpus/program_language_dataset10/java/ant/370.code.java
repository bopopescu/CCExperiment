package org.apache.tools.ant.taskdefs.optional.ccm;
public class CCMCheckinDefault extends CCMCheck {
    public CCMCheckinDefault() {
        super();
        setCcmAction(COMMAND_CHECKIN);
        setTask(DEFAULT_TASK);
    }
    public static final String DEFAULT_TASK = "default";
}