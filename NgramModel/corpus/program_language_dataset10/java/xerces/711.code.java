package org.apache.xerces.xs;
import org.w3c.dom.ls.LSInput;
public interface XSImplementation {
    public StringList getRecognizedVersions();
    public XSLoader createXSLoader(StringList versions)
                                   throws XSException;
    public StringList createStringList(String[] values);
    public LSInputList createLSInputList(LSInput[] values);
}
