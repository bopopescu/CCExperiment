package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGFEFuncBElement;
public class SVGOMFEFuncBElement
    extends    SVGOMComponentTransferFunctionElement
    implements SVGFEFuncBElement {
    protected SVGOMFEFuncBElement() {
    }
    public SVGOMFEFuncBElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_FE_FUNC_B_TAG;
    }
    protected Node newNode() {
        return new SVGOMFEFuncBElement();
    }
}
