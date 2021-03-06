package org.apache.tools.ant.property;
import java.text.ParsePosition;
import java.util.Collection;
import java.util.Iterator;
import org.apache.tools.ant.Project;
public class ParseProperties implements ParseNextProperty {
    private final Project project;
    private final GetProperty getProperty;
    private final Collection expanders;
    public ParseProperties(Project project, Collection expanders, GetProperty getProperty) {
        this.project = project;
        this.expanders = expanders;
        this.getProperty = getProperty;
    }
    public Project getProject() {
        return project;
    }
    public Object parseProperties(String value) {
        if (value == null || "".equals(value)) {
            return value;
        }
        final int len = value.length();
        ParsePosition pos = new ParsePosition(0);
        Object o = parseNextProperty(value, pos);
        if (o != null && pos.getIndex() >= len) {
            return o;
        }
        StringBuffer sb = new StringBuffer(len * 2);
        if (o == null) {
            sb.append(value.charAt(pos.getIndex()));
            pos.setIndex(pos.getIndex() + 1);
        } else {
            sb.append(o);
        }
        while (pos.getIndex() < len) {
            o = parseNextProperty(value, pos);
            if (o == null) {
                sb.append(value.charAt(pos.getIndex()));
                pos.setIndex(pos.getIndex() + 1);
            } else {
                sb.append(o);
            }
        }
        return sb.toString();
    }
    public boolean containsProperties(String value) {
        if (value == null) {
            return false;
        }
        final int len = value.length();
        for (ParsePosition pos = new ParsePosition(0); pos.getIndex() < len;) {
            if (parsePropertyName(value, pos) != null) {
                return true;
            }
            pos.setIndex(pos.getIndex() + 1);
        }
        return false;
    }
    public Object parseNextProperty(String value, ParsePosition pos) {
        final int start = pos.getIndex();
        if (start > value.length()) {
            return null;
        }
        String propertyName = parsePropertyName(value, pos);
        if (propertyName != null) {
            Object result = getProperty(propertyName);
            if (result != null) {
                return result;
            }
            if (project != null) {
                project.log(
                    "Property \"" + propertyName
                    + "\" has not been set", Project.MSG_VERBOSE);
            }
            return value.substring(start, pos.getIndex());
        }
        return null;
    }
    private String parsePropertyName(String value, ParsePosition pos) {
        for (Iterator iter = expanders.iterator(); iter.hasNext();) {
            String propertyName = ((PropertyExpander) iter.next())
                .parsePropertyName(value, pos, this);
            if (propertyName == null) {
                continue;
            }
            return propertyName;
        }
        return null;
    }
    private Object getProperty(String propertyName) {
        return getProperty.getProperty(propertyName);
    }
}
