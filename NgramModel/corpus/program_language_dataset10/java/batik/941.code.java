package org.apache.batik.extension.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.extension.GraphicsExtensionElement;
import org.w3c.dom.Node;
public class FlowTextElement
    extends    GraphicsExtensionElement 
    implements BatikExtConstants {
    protected FlowTextElement() {
    }
    public FlowTextElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return BATIK_EXT_FLOW_TEXT_TAG;
    }
    public String getNamespaceURI() {
        return BATIK_12_NAMESPACE_URI;
    }
    protected Node newNode() {
        return new FlowTextElement();
    }
}
