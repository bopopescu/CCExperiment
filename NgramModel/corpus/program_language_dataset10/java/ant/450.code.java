package org.apache.tools.ant.taskdefs.optional.j2ee;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
public class ServerDeploy extends Task {
    private String action;
    private File source;
    private Vector vendorTools = new Vector();
    public void addGeneric(GenericHotDeploymentTool tool) {
        tool.setTask(this);
        vendorTools.addElement(tool);
    }
    public void addWeblogic(WebLogicHotDeploymentTool tool) {
        tool.setTask(this);
        vendorTools.addElement(tool);
    }
    public void addJonas(JonasHotDeploymentTool tool) {
        tool.setTask(this);
        vendorTools.addElement(tool);
    }
    public void execute() throws BuildException {
        for (Enumeration e = vendorTools.elements();
             e.hasMoreElements();) {
            HotDeploymentTool tool = (HotDeploymentTool) e.nextElement();
            tool.validateAttributes();
            tool.deploy();
        }
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public File getSource() {
        return source;
    }
    public void setSource(File source) {
        this.source = source;
    }
}