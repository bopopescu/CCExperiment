package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGSetElement;
public class SVGOMSetElement
    extends    SVGOMAnimationElement
    implements SVGSetElement {
    protected SVGOMSetElement() {
    }
    public SVGOMSetElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_SET_TAG;
    }
    protected Node newNode() {
        return new SVGOMSetElement();
    }
}
