package org.apache.batik.dom.anim;
import org.apache.batik.anim.values.AnimatableValue;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGLength;
public interface AnimationTarget {
    short PERCENTAGE_FONT_SIZE       = 0;
    short PERCENTAGE_VIEWPORT_WIDTH  = 1;
    short PERCENTAGE_VIEWPORT_HEIGHT = 2;
    short PERCENTAGE_VIEWPORT_SIZE   = 3;
    Element getElement();
    void updatePropertyValue(String pn, AnimatableValue val);
    void updateAttributeValue(String ns, String ln, AnimatableValue val);
    void updateOtherValue(String type, AnimatableValue val);
    AnimatableValue getUnderlyingValue(String ns, String ln);
    short getPercentageInterpretation(String ns, String an, boolean isCSS);
    boolean useLinearRGBColorInterpolation();
    float svgToUserSpace(float v, short type, short pcInterp);
    void addTargetListener(String ns, String an, boolean isCSS,
                           AnimationTargetListener l);
    void removeTargetListener(String ns, String an, boolean isCSS,
                              AnimationTargetListener l);
}
