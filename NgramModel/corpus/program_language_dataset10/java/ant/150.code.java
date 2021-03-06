package org.apache.tools.ant.property;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.tools.ant.PropertyHelper;
public class LocalPropertyStack {
    private LinkedList stack = new LinkedList();
    public void addLocal(String property) {
        if (!stack.isEmpty()) {
            ((Map) stack.getFirst()).put(property, NullReturn.NULL);
        }
    }
    public void enterScope() {
        stack.addFirst(new HashMap());
    }
    public void exitScope() {
        ((HashMap) stack.removeFirst()).clear();
    }
    public LocalPropertyStack copy() {
        LocalPropertyStack ret = new LocalPropertyStack();
        ret.stack.addAll(stack);
        return ret;
    }
    public Object evaluate(String property, PropertyHelper helper) {
        for (Iterator i = stack.iterator(); i.hasNext();) {
            Map map = (Map) i.next();
            Object ret = map.get(property);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }
    public boolean setNew(
        String property, Object value, PropertyHelper propertyHelper) {
        Map map = getMapForProperty(property);
        if (map == null) {
            return false;
        }
        Object currValue = map.get(property);
        if (currValue == NullReturn.NULL) {
            map.put(property, value);
        }
        return true;
    }
    public boolean set(String property, Object value, PropertyHelper propertyHelper) {
        Map map = getMapForProperty(property);
        if (map == null) {
            return false;
        }
        map.put(property, value);
        return true;
    }
    private Map getMapForProperty(String property) {
        for (Iterator i = stack.iterator(); i.hasNext();) {
            Map map = (Map) i.next();
            if (map.get(property) != null) {
                return map;
            }
        }
        return null;
    }
}
