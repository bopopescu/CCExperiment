package org.apache.tools.ant.taskdefs.condition;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.types.Reference;
public class IsReference extends ProjectComponent implements Condition {
    private Reference ref;
    private String type;
    public void setRefid(Reference r) {
        ref = r;
    }
    public void setType(String type) {
        this.type = type;
    }
    public boolean eval() throws BuildException {
        if (ref == null) {
            throw new BuildException("No reference specified for isreference "
                                     + "condition");
        }
        String key = ref.getRefId();
        if (!getProject().hasReference(key)) {
            return false;
        } else if (type == null) {
            return true;
        } else {
            Object o = getProject().getReference(key);
            Class typeClass =
                (Class) getProject().getDataTypeDefinitions().get(type);
            if (typeClass == null) {
                typeClass =
                    (Class) getProject().getTaskDefinitions().get(type);
            }
            if (typeClass == null) {
                return false;
            }
            return typeClass.isAssignableFrom(o.getClass());
        }
    }
}
