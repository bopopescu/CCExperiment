package org.apache.tools.ant.taskdefs;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Task;
public class PropertyHelperTask extends Task {
    public final class DelegateElement {
        private String refid;
        private DelegateElement() {
        }
        public String getRefid() {
            return refid;
        }
        public void setRefid(String refid) {
            this.refid = refid;
        }
        private PropertyHelper.Delegate resolve() {
            if (refid == null) {
                throw new BuildException("refid required for generic delegate");
            }
            return (PropertyHelper.Delegate) getProject().getReference(refid);
        }
    }
    private PropertyHelper propertyHelper;
    private List delegates;
    public synchronized void addConfigured(PropertyHelper propertyHelper) {
        if (this.propertyHelper != null) {
            throw new BuildException("Only one PropertyHelper can be installed");
        }
        this.propertyHelper = propertyHelper;
    }
    public synchronized void addConfigured(PropertyHelper.Delegate delegate) {
        getAddDelegateList().add(delegate);
    }
    public DelegateElement createDelegate() {
        DelegateElement result = new DelegateElement();
        getAddDelegateList().add(result);
        return result;
    }
    public void execute() throws BuildException {
        if (getProject() == null) {
            throw new BuildException("Project instance not set");
        }
        if (propertyHelper == null && delegates == null) {
            throw new BuildException("Either a new PropertyHelper"
                    + " or one or more PropertyHelper delegates are required");
        }
        PropertyHelper ph = propertyHelper;
        if (ph == null) {
            ph = PropertyHelper.getPropertyHelper(getProject());
        } else {
            ph = propertyHelper;
        }
        synchronized (ph) {
            if (delegates != null) {
                for (Iterator iter = delegates.iterator(); iter.hasNext();) {
                    Object o = iter.next();
                    PropertyHelper.Delegate delegate = o instanceof DelegateElement
                            ? ((DelegateElement) o).resolve() : (PropertyHelper.Delegate) o;
                    log("Adding PropertyHelper delegate " + delegate, Project.MSG_DEBUG);
                    ph.add(delegate);
                }
            }
        }
        if (propertyHelper != null) {
            log("Installing PropertyHelper " + propertyHelper, Project.MSG_DEBUG);
            getProject().addReference(MagicNames.REFID_PROPERTY_HELPER, propertyHelper);
        }
    }
    private synchronized List getAddDelegateList() {
        if (delegates == null) {
            delegates = new ArrayList();
        }
        return delegates;
    }
}
