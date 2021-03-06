package org.apache.tools.ant.property;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.MagicNames;
public class LocalProperties
    extends InheritableThreadLocal
    implements PropertyHelper.PropertyEvaluator,
    PropertyHelper.PropertySetter {
    public static synchronized LocalProperties get(Project project) {
        LocalProperties l = (LocalProperties) project.getReference(
            MagicNames.REFID_LOCAL_PROPERTIES);
        if (l == null) {
            l = new LocalProperties();
            project.addReference(MagicNames.REFID_LOCAL_PROPERTIES, l);
            PropertyHelper.getPropertyHelper(project).add(l);
        }
        return l;
    }
    private LocalProperties() {
    }
    protected synchronized Object initialValue() {
        return new LocalPropertyStack();
    }
    private LocalPropertyStack current() {
        return (LocalPropertyStack) get();
    }
    public void addLocal(String property) {
        current().addLocal(property);
    }
    public void enterScope() {
        current().enterScope();
    }
    public void exitScope() {
        current().exitScope();
    }
    public void copy() {
        set(current().copy());
    }
    public Object evaluate(String property, PropertyHelper helper) {
        return current().evaluate(property, helper);
    }
    public boolean setNew(
        String property, Object value, PropertyHelper propertyHelper) {
        return current().setNew(property, value, propertyHelper);
    }
    public boolean set(
        String property, Object value, PropertyHelper propertyHelper) {
        return current().set(property, value, propertyHelper);
    }
}
