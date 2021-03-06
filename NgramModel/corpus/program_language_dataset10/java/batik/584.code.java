package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGFEMergeElement;
public class SVGOMFEMergeElement
    extends    SVGOMFilterPrimitiveStandardAttributes
    implements SVGFEMergeElement {
    protected SVGOMFEMergeElement() {
    }
    public SVGOMFEMergeElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_FE_MERGE_TAG;
    }
    protected Node newNode() {
        return new SVGOMFEMergeElement();
    }
}
