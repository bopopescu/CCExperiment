package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TypeAdapter;
public class AugmentReference extends Task implements TypeAdapter {
    private String id;
    public void checkProxyClass(Class proxyClass) {
    }
    public synchronized Object getProxy() {
        if (getProject() == null) {
            throw new IllegalStateException(getTaskName() + "Project owner unset");
        }
        hijackId();
        if (getProject().hasReference(id)) {
            Object result = getProject().getReference(id);
            log("project reference " + id + "=" + String.valueOf(result), Project.MSG_DEBUG);
            return result;
        }
        throw new IllegalStateException("Unknown reference \"" + id + "\"");
    }
    public void setProxy(Object o) {
        throw new UnsupportedOperationException();
    }
    private synchronized void hijackId() {
        if (id == null) {
            RuntimeConfigurable wrapper = getWrapper();
            id = wrapper.getId();
            if (id == null) {
                throw new IllegalStateException(getTaskName() + " attribute 'id' unset");
            }
            wrapper.setAttribute("id", null);
            wrapper.removeAttribute("id");
            wrapper.setElementTag("augmented reference \"" + id + "\"");
        }
    }
}
