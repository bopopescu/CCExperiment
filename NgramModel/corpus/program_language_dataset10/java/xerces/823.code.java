package org.apache.xerces.util;
import java.util.Map;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;
public class InternalTaglet implements Taglet {
    private static final String NAME = "xerces.internal";
    private static final String HEADER = "INTERNAL:";
    public boolean inConstructor() {
        return false;
    }
    public boolean inField() {
        return false;
    }
    public boolean inMethod() {
        return true;
    }
    public boolean inOverview() {
        return true;
    }
    public boolean inPackage() {
        return false;
    }
    public boolean inType() {
        return true;
    }
    public boolean isInlineTag() {
        return false;
    }
    public String getName() {
        return NAME;
    }
    public String toString(Tag arg0) {
        return "<DT><H1 style=\"font-size:110%\">" + HEADER + "</H1><DD>"
        + "Usage of this class is not supported. It may be altered or removed at any time.<br/>"
        + "<I>" + arg0.text() + "</I></DD>\n";
    }
    public String toString(Tag[] tags) {
        if (tags.length == 0) {
            return null;
        }
        String result = "\n<DT><H1 style=\"font-size:110%\">" + HEADER + "</H1><DD>";
        result += "Usage of this class is not supported. It may be altered or removed at any time.";
        result += "<I>";
        for (int i = 0; i < tags.length; i++) {
            result += "<br/>";
            result += tags[i].text();
        }
        return result + "</I></DD>\n";
    }
    public static void register(Map tagletMap) {
        InternalTaglet tag = new InternalTaglet();
        Taglet t = (Taglet) tagletMap.get(tag.getName());
        if (t != null) {
            tagletMap.remove(tag.getName());
        }
        tagletMap.put(tag.getName(), tag);
    }
}
