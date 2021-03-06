package org.apache.xml.serializer;
import java.io.IOException;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSSerializerFilter;
public interface DOM3Serializer {
    public void serializeDOM3(Node node) throws IOException;
    public void setErrorHandler(DOMErrorHandler handler);
    public DOMErrorHandler getErrorHandler();
    public void setNodeFilter(LSSerializerFilter filter);
    public LSSerializerFilter getNodeFilter();
    public void setNewLine(char[] newLine);
}
