package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedBoolean;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGStringList;
public abstract class SVGOMTextContentElement
    extends    SVGStylableElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGStylableElement.xmlTraitInformation);
        t.put(null, SVG_TEXT_LENGTH_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_SIZE));
        t.put(null, SVG_LENGTH_ADJUST_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_IDENT));
        t.put(null, SVG_EXTERNAL_RESOURCES_REQUIRED_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_BOOLEAN));
        xmlTraitInformation = t;
    }
    protected static final String[] LENGTH_ADJUST_VALUES = {
        "",
        SVG_SPACING_ATTRIBUTE,
        SVG_SPACING_AND_GLYPHS_VALUE
    };
    protected SVGOMAnimatedBoolean externalResourcesRequired;
    protected AbstractSVGAnimatedLength textLength;
    protected SVGOMAnimatedEnumeration lengthAdjust;
    protected SVGOMTextContentElement() {
    }
    protected SVGOMTextContentElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        externalResourcesRequired =
            createLiveAnimatedBoolean
                (null, SVG_EXTERNAL_RESOURCES_REQUIRED_ATTRIBUTE, false);
        lengthAdjust =
            createLiveAnimatedEnumeration
                (null, SVG_LENGTH_ADJUST_ATTRIBUTE, LENGTH_ADJUST_VALUES,
                 (short) 1);
        textLength = new AbstractSVGAnimatedLength
            (this, null, SVG_TEXT_LENGTH_ATTRIBUTE,
             SVGOMAnimatedLength.HORIZONTAL_LENGTH, true) {
                boolean usedDefault;
                protected String getDefaultValue() {
                    usedDefault = true;
                    return String.valueOf( getComputedTextLength() );
                }
                public SVGLength getBaseVal() {
                    if (baseVal == null) {
                        baseVal = new SVGTextLength(direction);
                    }
                    return baseVal;
                }
                class SVGTextLength extends BaseSVGLength {
                    public SVGTextLength(short direction) {
                        super(direction);
                    }
                    protected void revalidate() {
                        usedDefault = false;
                        super.revalidate();
                        if (usedDefault) valid = false;
                    }
                }
            };
        liveAttributeValues.put(null, SVG_TEXT_LENGTH_ATTRIBUTE, textLength);
        textLength.addAnimatedAttributeListener
            (((SVGOMDocument) ownerDocument).getAnimatedAttributeListener());
    }
    public SVGAnimatedLength getTextLength() {
        return textLength;
    }
    public SVGAnimatedEnumeration getLengthAdjust() {
        return lengthAdjust;
    }
    public int getNumberOfChars() {
        return SVGTextContentSupport.getNumberOfChars(this);
    }
    public float getComputedTextLength() {
        return SVGTextContentSupport.getComputedTextLength(this);
    }
    public float getSubStringLength(int charnum, int nchars)
        throws DOMException {
        return SVGTextContentSupport.getSubStringLength(this, charnum, nchars);
    }
    public SVGPoint getStartPositionOfChar(int charnum) throws DOMException {
        return SVGTextContentSupport.getStartPositionOfChar(this, charnum);
    }
    public SVGPoint getEndPositionOfChar(int charnum) throws DOMException {
        return SVGTextContentSupport.getEndPositionOfChar(this, charnum);
    }
    public SVGRect getExtentOfChar(int charnum) throws DOMException {
        return SVGTextContentSupport.getExtentOfChar(this, charnum);
    }
    public float getRotationOfChar(int charnum) throws DOMException {
        return SVGTextContentSupport.getRotationOfChar(this, charnum);
    }
    public int getCharNumAtPosition(SVGPoint point) {
        return SVGTextContentSupport.getCharNumAtPosition
            (this, point.getX(), point.getY());
    }
    public void selectSubString(int charnum, int nchars)
        throws DOMException {
        SVGTextContentSupport.selectSubString(this, charnum, nchars);
    }
    public SVGAnimatedBoolean getExternalResourcesRequired() {
        return externalResourcesRequired;
    }
    public String getXMLlang() {
        return XMLSupport.getXMLLang(this);
    }
    public void setXMLlang(String lang) {
        setAttributeNS(XML_NAMESPACE_URI, XML_LANG_QNAME, lang);
    }
    public String getXMLspace() {
        return XMLSupport.getXMLSpace(this);
    }
    public void setXMLspace(String space) {
        setAttributeNS(XML_NAMESPACE_URI, XML_SPACE_QNAME, space);
    }
    public SVGStringList getRequiredFeatures() {
        return SVGTestsSupport.getRequiredFeatures(this);
    }
    public SVGStringList getRequiredExtensions() {
        return SVGTestsSupport.getRequiredExtensions(this);
    }
    public SVGStringList getSystemLanguage() {
        return SVGTestsSupport.getSystemLanguage(this);
    }
    public boolean hasExtension(String extension) {
        return SVGTestsSupport.hasExtension(this, extension);
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
