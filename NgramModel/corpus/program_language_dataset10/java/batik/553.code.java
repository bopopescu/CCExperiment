package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimateMotionElement;
public class SVGOMAnimateMotionElement
    extends    SVGOMAnimationElement
    implements SVGAnimateMotionElement {
    protected static final AttributeInitializer attributeInitializer;
    static {
        attributeInitializer = new AttributeInitializer(1);
        attributeInitializer.addAttribute(null,
                                          null,
                                          SVG_CALC_MODE_ATTRIBUTE,
                                          SVG_PACED_VALUE);
    }
    protected SVGOMAnimateMotionElement() {
    }
    public SVGOMAnimateMotionElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
    }
    public String getLocalName() {
        return SVG_ANIMATE_MOTION_TAG;
    }
    protected AttributeInitializer getAttributeInitializer() {
        return attributeInitializer;
    }
    protected Node newNode() {
        return new SVGOMAnimateMotionElement();
    }
}
