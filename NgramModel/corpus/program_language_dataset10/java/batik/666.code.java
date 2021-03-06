package org.apache.batik.dom.svg12;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGOMTextContentElement;
import org.apache.batik.util.SVG12Constants;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGTextContentElement;
public class SVGOMFlowDivElement 
    extends    SVGOMTextContentElement
    implements SVGTextContentElement {
    protected SVGOMFlowDivElement() {
    }
    public SVGOMFlowDivElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG12Constants.SVG_FLOW_DIV_TAG;
    }
    protected Node newNode() {
        return new SVGOMFlowDivElement();
    }
}
