package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.dom.util.XMLSupport;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGGlyphRefElement;
public class SVGOMGlyphRefElement
    extends    SVGStylableElement
    implements SVGGlyphRefElement {
    protected static final AttributeInitializer attributeInitializer;
    static {
        attributeInitializer = new AttributeInitializer(4);
        attributeInitializer.addAttribute(XMLSupport.XMLNS_NAMESPACE_URI,
                                          null, "xmlns:xlink",
                                          XLinkSupport.XLINK_NAMESPACE_URI);
        attributeInitializer.addAttribute(XLinkSupport.XLINK_NAMESPACE_URI,
                                          "xlink", "type", "simple");
        attributeInitializer.addAttribute(XLinkSupport.XLINK_NAMESPACE_URI,
                                          "xlink", "show", "other");
        attributeInitializer.addAttribute(XLinkSupport.XLINK_NAMESPACE_URI,
                                          "xlink", "actuate", "onLoad");
    }
    protected SVGOMAnimatedString href;
    protected SVGOMGlyphRefElement() {
    }
    public SVGOMGlyphRefElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        href =
            createLiveAnimatedString(XLINK_NAMESPACE_URI, XLINK_HREF_ATTRIBUTE);
    }
    public String getLocalName() {
        return SVG_GLYPH_REF_TAG;
    }
    public SVGAnimatedString getHref() {
        return href;
    }
    public String getGlyphRef() {
        return getAttributeNS(null, SVG_GLYPH_REF_ATTRIBUTE);
    }
    public void setGlyphRef(String glyphRef) throws DOMException {
        setAttributeNS(null, SVG_GLYPH_REF_ATTRIBUTE, glyphRef);
    }
    public String getFormat() {
        return getAttributeNS(null, SVG_FORMAT_ATTRIBUTE);
    }
    public void setFormat(String format) throws DOMException {
        setAttributeNS(null, SVG_FORMAT_ATTRIBUTE, format);
    }
    public float getX() {
        return Float.parseFloat(getAttributeNS(null, SVG_X_ATTRIBUTE));
    }
    public void setX(float x) throws DOMException {
        setAttributeNS(null, SVG_X_ATTRIBUTE, String.valueOf(x));
    }
    public float getY() {
        return Float.parseFloat(getAttributeNS(null, SVG_Y_ATTRIBUTE));
    }
    public void setY(float y) throws DOMException {
        setAttributeNS(null, SVG_Y_ATTRIBUTE, String.valueOf(y));
    }
    public float getDx() {
        return Float.parseFloat(getAttributeNS(null, SVG_DX_ATTRIBUTE));
    }
    public void setDx(float dx) throws DOMException {
        setAttributeNS(null, SVG_DX_ATTRIBUTE, String.valueOf(dx));
    }
    public float getDy() {
        return Float.parseFloat(getAttributeNS(null, SVG_DY_ATTRIBUTE));
    }
    public void setDy(float dy) throws DOMException {
        setAttributeNS(null, SVG_DY_ATTRIBUTE, String.valueOf(dy));
    }
    protected AttributeInitializer getAttributeInitializer() {
        return attributeInitializer;
    }
    protected Node newNode() {
        return new SVGOMGlyphRefElement();
    }
}
