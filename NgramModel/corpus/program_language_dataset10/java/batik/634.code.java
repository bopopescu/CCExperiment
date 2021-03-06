package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGTextPathElement;
public class SVGOMTextPathElement
    extends    SVGOMTextContentElement
    implements SVGTextPathElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMTextContentElement.xmlTraitInformation);
        t.put(null, SVG_METHOD_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_IDENT));
        t.put(null, SVG_SPACING_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_IDENT));
        t.put(null, SVG_START_OFFSET_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH));
        t.put(XLINK_NAMESPACE_URI, XLINK_HREF_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_URI));
        xmlTraitInformation = t;
    }
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
    protected static final String[] METHOD_VALUES = {
        "",
        SVG_ALIGN_VALUE,
        SVG_STRETCH_VALUE
    };
    protected static final String[] SPACING_VALUES = {
        "",
        SVG_AUTO_VALUE,
        SVG_EXACT_VALUE
    };
    protected SVGOMAnimatedEnumeration method;
    protected SVGOMAnimatedEnumeration spacing;
    protected SVGOMAnimatedLength startOffset;
    protected SVGOMAnimatedString href;
    protected SVGOMTextPathElement() {
    }
    public SVGOMTextPathElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        method =
            createLiveAnimatedEnumeration
                (null, SVG_METHOD_ATTRIBUTE, METHOD_VALUES, (short) 1);
        spacing =
            createLiveAnimatedEnumeration
                (null, SVG_SPACING_ATTRIBUTE, SPACING_VALUES, (short) 2);
        startOffset =
            createLiveAnimatedLength
                (null, SVG_START_OFFSET_ATTRIBUTE,
                 SVG_TEXT_PATH_START_OFFSET_DEFAULT_VALUE,
                 SVGOMAnimatedLength.OTHER_LENGTH, false);
        href =
            createLiveAnimatedString(XLINK_NAMESPACE_URI, XLINK_HREF_ATTRIBUTE);
    }
    public String getLocalName() {
        return SVG_TEXT_PATH_TAG;
    }
    public SVGAnimatedLength getStartOffset() {
        return startOffset;
    }
    public SVGAnimatedEnumeration getMethod() {
        return method;
    }
    public SVGAnimatedEnumeration getSpacing() {
        return spacing;
    }
    public SVGAnimatedString getHref() {
        return href;
    }
    protected AttributeInitializer getAttributeInitializer() {
        return attributeInitializer;
    }
    protected Node newNode() {
        return new SVGOMTextPathElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
