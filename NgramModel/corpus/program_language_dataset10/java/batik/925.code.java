package org.apache.batik.extension.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.extension.GraphicsExtensionElement;
import org.w3c.dom.Node;
public class BatikRegularPolygonElement
    extends    GraphicsExtensionElement 
    implements BatikExtConstants {
    protected BatikRegularPolygonElement() {
    }
    public BatikRegularPolygonElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return BATIK_EXT_REGULAR_POLYGON_TAG;
    }
    public String getNamespaceURI() {
        return BATIK_EXT_NAMESPACE_URI;
    }
    protected Node newNode() {
        return new BatikRegularPolygonElement();
    }
}
