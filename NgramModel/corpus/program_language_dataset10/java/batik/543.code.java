package org.apache.batik.dom.svg;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.anim.AnimationTarget;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedAngle;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGAngle;
import org.w3c.dom.svg.SVGMarkerElement;
public class SVGOMAnimatedMarkerOrientValue extends AbstractSVGAnimatedValue {
    protected boolean valid;
    protected AnimatedAngle animatedAngle = new AnimatedAngle();
    protected AnimatedEnumeration animatedEnumeration =
        new AnimatedEnumeration();
    protected BaseSVGAngle baseAngleVal;
    protected short baseEnumerationVal;
    protected AnimSVGAngle animAngleVal;
    protected short animEnumerationVal;
    protected boolean changing;
    public SVGOMAnimatedMarkerOrientValue(AbstractElement elt,
                                          String ns,
                                          String ln) {
        super(elt, ns, ln);
    }
   protected void updateAnimatedValue(AnimatableValue val) {
        throw new UnsupportedOperationException
            ("Animation of marker orient value is not implemented");
    }
    public AnimatableValue getUnderlyingValue(AnimationTarget target) {
        throw new UnsupportedOperationException
            ("Animation of marker orient value is not implemented");
    }
    public void attrAdded(Attr node, String newv) {
        if (!changing) {
            valid = false;
        }
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }
    public void attrModified(Attr node, String oldv, String newv) {
        if (!changing) {
            valid = false;
        }
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }
    public void attrRemoved(Attr node, String oldv) {
        if (!changing) {
            valid = false;
        }
        fireBaseAttributeListeners();
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }
    public void setAnimatedValueToAngle(short unitType, float value) {
        hasAnimVal = true;
        animAngleVal.setAnimatedValue(unitType, value);
        animEnumerationVal = SVGMarkerElement.SVG_MARKER_ORIENT_ANGLE;
        fireAnimatedAttributeListeners();
    }
    public void setAnimatedValueToAuto() {
        hasAnimVal = true;
        animAngleVal.setAnimatedValue(SVGAngle.SVG_ANGLETYPE_UNSPECIFIED, 0);
        animEnumerationVal = SVGMarkerElement.SVG_MARKER_ORIENT_AUTO;
        fireAnimatedAttributeListeners();
    }
    public void resetAnimatedValue() {
        hasAnimVal = false;
        fireAnimatedAttributeListeners();
    }
    public SVGAnimatedAngle getAnimatedAngle() {
        return animatedAngle;
    }
    public SVGAnimatedEnumeration getAnimatedEnumeration() {
        return animatedEnumeration;
    }
    protected class BaseSVGAngle extends SVGOMAngle {
        public void invalidate() {
            valid = false;
        }
        protected void reset() {
            try {
                changing = true;
                valid = true;
                String value;
                if (baseEnumerationVal ==
                        SVGMarkerElement.SVG_MARKER_ORIENT_ANGLE) {
                    value = getValueAsString();
                } else if (baseEnumerationVal ==
                        SVGMarkerElement.SVG_MARKER_ORIENT_AUTO) {
                    value = SVGConstants.SVG_AUTO_VALUE;
                } else {
                    return;
                }
                element.setAttributeNS(namespaceURI, localName, value);
            } finally {
                changing = false;
            }
        }
        protected void revalidate() {
            if (!valid) {
                Attr attr = element.getAttributeNodeNS(namespaceURI, localName);
                if (attr == null) {
                    unitType = SVGAngle.SVG_ANGLETYPE_UNSPECIFIED;
                    value = 0;
                } else {
                    parse(attr.getValue());
                }
                valid = true;
            }
        }
        protected void parse(String s) {
            if (s.equals(SVGConstants.SVG_AUTO_VALUE)) {
                unitType = SVGAngle.SVG_ANGLETYPE_UNSPECIFIED;
                value = 0;
                baseEnumerationVal = SVGMarkerElement.SVG_MARKER_ORIENT_AUTO;
            } else {
                super.parse(s);
                if (unitType == SVGAngle.SVG_ANGLETYPE_UNKNOWN) {
                    baseEnumerationVal = SVGMarkerElement.SVG_MARKER_ORIENT_UNKNOWN;
                } else {
                    baseEnumerationVal = SVGMarkerElement.SVG_MARKER_ORIENT_ANGLE;
                }
            }
        }
    }
    protected class AnimSVGAngle extends SVGOMAngle {
        public short getUnitType() {
            if (hasAnimVal) {
                return super.getUnitType();
            }
            return animatedAngle.getBaseVal().getUnitType();
        }
        public float getValue() {
            if (hasAnimVal) {
                return super.getValue();
            }
            return animatedAngle.getBaseVal().getValue();
        }
        public float getValueInSpecifiedUnits() {
            if (hasAnimVal) {
                return super.getValueInSpecifiedUnits();
            }
            return animatedAngle.getBaseVal().getValueInSpecifiedUnits();
        }
        public String getValueAsString() {
            if (hasAnimVal) {
                return super.getValueAsString();
            }
            return animatedAngle.getBaseVal().getValueAsString();
        }
        public void setValue(float value) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "readonly.angle",
                 null);
        }
        public void setValueInSpecifiedUnits(float value) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "readonly.angle",
                 null);
        }
        public void setValueAsString(String value) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "readonly.angle",
                 null);
        }
        public void newValueSpecifiedUnits(short unit, float value) {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "readonly.angle",
                 null);
        }
        public void convertToSpecifiedUnits(short unit) {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "readonly.angle",
                 null);
        }
        protected void setAnimatedValue(int type, float val) {
            super.newValueSpecifiedUnits((short) type, val);
        }
    }
    protected class AnimatedAngle implements SVGAnimatedAngle {
        public SVGAngle getBaseVal() {
            if (baseAngleVal == null) {
                baseAngleVal = new BaseSVGAngle();
            }
            return baseAngleVal;
        }
        public SVGAngle getAnimVal() {
            if (animAngleVal == null) {
                animAngleVal = new AnimSVGAngle();
            }
            return animAngleVal;
        }
    }
    protected class AnimatedEnumeration implements SVGAnimatedEnumeration {
        public short getBaseVal() {
            if (baseAngleVal == null) {
                baseAngleVal = new BaseSVGAngle();
            }
            baseAngleVal.revalidate();
            return baseEnumerationVal;
        }
        public void setBaseVal(short baseVal) throws DOMException {
            if (baseVal == SVGMarkerElement.SVG_MARKER_ORIENT_AUTO) {
                baseEnumerationVal = baseVal;
                if (baseAngleVal == null) {
                    baseAngleVal = new BaseSVGAngle();
                }
                baseAngleVal.unitType = SVGAngle.SVG_ANGLETYPE_UNSPECIFIED;
                baseAngleVal.value = 0;
                baseAngleVal.reset();
            } else if (baseVal == SVGMarkerElement.SVG_MARKER_ORIENT_ANGLE) {
                baseEnumerationVal = baseVal;
                if (baseAngleVal == null) {
                    baseAngleVal = new BaseSVGAngle();
                }
                baseAngleVal.reset();
            }
        }
        public short getAnimVal() {
            if (hasAnimVal) {
                return animEnumerationVal;
            }
            if (baseAngleVal == null) {
                baseAngleVal = new BaseSVGAngle();
            }
            baseAngleVal.revalidate();
            return baseEnumerationVal;
        }
    }
}
