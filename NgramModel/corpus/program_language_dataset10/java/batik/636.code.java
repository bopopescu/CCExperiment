package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGTitleElement;
public class SVGOMTitleElement
    extends    SVGDescriptiveElement
    implements SVGTitleElement {
    protected SVGOMTitleElement() {
    }
    public SVGOMTitleElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_TITLE_TAG;
    }
    protected Node newNode() {
        return new SVGOMTitleElement();
    }
}
