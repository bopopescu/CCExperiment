package org.apache.tools.ant.util;
import java.io.ByteArrayOutputStream;
import org.apache.tools.ant.Project;
public class PropertyOutputStream extends ByteArrayOutputStream {
    private Project project;
    private String property;
    private boolean trim;
    public PropertyOutputStream(Project p, String s) {
        this(p, s, true);
    }
    public PropertyOutputStream(Project p, String s, boolean b) {
        project = p;
        property = s;
        trim = b;
    }
    public void close() {
        if (project != null && property != null) {
            String s = new String(toByteArray());
            project.setNewProperty(property, trim ? s.trim() : s);
        }
    }
}