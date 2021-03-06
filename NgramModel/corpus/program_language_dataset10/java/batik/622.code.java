package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGRadialGradientElement;
public class SVGOMRadialGradientElement
    extends    SVGOMGradientElement
    implements SVGRadialGradientElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMGradientElement.xmlTraitInformation);
        t.put(null, SVG_CX_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_WIDTH));
        t.put(null, SVG_CY_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_HEIGHT));
        t.put(null, SVG_FX_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_WIDTH));
        t.put(null, SVG_FY_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_HEIGHT));
        t.put(null, SVG_R_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_LENGTH, PERCENTAGE_VIEWPORT_SIZE));
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedLength cx;
    protected SVGOMAnimatedLength cy;
    protected AbstractSVGAnimatedLength fx;
    protected AbstractSVGAnimatedLength fy;
    protected SVGOMAnimatedLength r;
    protected SVGOMRadialGradientElement() {
    }
    public SVGOMRadialGradientElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        cx = createLiveAnimatedLength
            (null, SVG_CX_ATTRIBUTE, SVG_RADIAL_GRADIENT_CX_DEFAULT_VALUE,
             SVGOMAnimatedLength.HORIZONTAL_LENGTH, false);
        cy = createLiveAnimatedLength
            (null, SVG_CY_ATTRIBUTE, SVG_RADIAL_GRADIENT_CY_DEFAULT_VALUE,
             SVGOMAnimatedLength.VERTICAL_LENGTH, false);
        r = createLiveAnimatedLength
            (null, SVG_R_ATTRIBUTE, SVG_RADIAL_GRADIENT_R_DEFAULT_VALUE,
             SVGOMAnimatedLength.OTHER_LENGTH, false);
        fx = new AbstractSVGAnimatedLength
            (this, null, SVG_FX_ATTRIBUTE,
             SVGOMAnimatedLength.HORIZONTAL_LENGTH, false) {
                protected String getDefaultValue() {
                    Attr attr = getAttributeNodeNS(null, SVG_CX_ATTRIBUTE);
                    if (attr == null) {
                        return SVG_RADIAL_GRADIENT_CX_DEFAULT_VALUE;
                    }
                    return attr.getValue();
                }
            };
        fy = new AbstractSVGAnimatedLength
            (this, null, SVG_FY_ATTRIBUTE,
             SVGOMAnimatedLength.VERTICAL_LENGTH, false) {
                protected String getDefaultValue() {
                    Attr attr = getAttributeNodeNS(null, SVG_CY_ATTRIBUTE);
                    if (attr == null) {
                        return SVG_RADIAL_GRADIENT_CY_DEFAULT_VALUE;
                    }
                    return attr.getValue();
                }
            };
        liveAttributeValues.put(null, SVG_FX_ATTRIBUTE, fx);
        liveAttributeValues.put(null, SVG_FY_ATTRIBUTE, fy);
        AnimatedAttributeListener l =
            ((SVGOMDocument) ownerDocument).getAnimatedAttributeListener();
        fx.addAnimatedAttributeListener(l);
        fy.addAnimatedAttributeListener(l);
    }
    public String getLocalName() {
        return SVG_RADIAL_GRADIENT_TAG;
    }
    public SVGAnimatedLength getCx() {
        return cx;
    }
    public SVGAnimatedLength getCy() {
        return cy;
    }
    public SVGAnimatedLength getR() {
        return r;
    }
    public SVGAnimatedLength getFx() {
        return fx;
    }
    public SVGAnimatedLength getFy() {
        return fy;
    }
    protected Node newNode() {
        return new SVGOMRadialGradientElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
