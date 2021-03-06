package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGFontFaceFormatElement;
public class SVGOMFontFaceFormatElement
    extends    SVGOMElement
    implements SVGFontFaceFormatElement {
    protected SVGOMFontFaceFormatElement() {
    }
    public SVGOMFontFaceFormatElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_FONT_FACE_FORMAT_TAG;
    }
    protected Node newNode() {
        return new SVGOMFontFaceFormatElement();
    }
}
