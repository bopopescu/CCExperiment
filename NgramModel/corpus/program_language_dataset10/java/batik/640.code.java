package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGTSpanElement;
public class SVGOMTSpanElement
    extends    SVGOMTextPositioningElement
    implements SVGTSpanElement {
    protected SVGOMTSpanElement() {
    }
    public SVGOMTSpanElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_TSPAN_TAG;
    }
    protected Node newNode() {
        return new SVGOMTSpanElement();
    }
}
