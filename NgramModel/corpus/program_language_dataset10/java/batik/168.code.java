package org.apache.batik.bridge;
import java.util.ArrayList;
import java.util.List;
import org.apache.batik.anim.AbstractAnimation;
import org.apache.batik.anim.AnimationEngine;
import org.apache.batik.dom.anim.AnimationTarget;
import org.apache.batik.anim.MotionAnimation;
import org.apache.batik.anim.values.AnimatableMotionPointValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.dom.svg.SVGAnimatedPathDataSupport;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.AngleHandler;
import org.apache.batik.parser.AngleParser;
import org.apache.batik.parser.LengthArrayProducer;
import org.apache.batik.parser.LengthPairListParser;
import org.apache.batik.parser.PathParser;
import org.apache.batik.parser.ParseException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAngle;
public class SVGAnimateMotionElementBridge extends SVGAnimateElementBridge {
    public String getLocalName() {
        return SVG_ANIMATE_MOTION_TAG;
    }
    public Bridge getInstance() {
        return new SVGAnimateMotionElementBridge();
    }
    protected AbstractAnimation createAnimation(AnimationTarget target) {
        animationType = AnimationEngine.ANIM_TYPE_OTHER;
        attributeLocalName = "motion";
        AnimatableValue from = parseLengthPair(SVG_FROM_ATTRIBUTE);
        AnimatableValue to = parseLengthPair(SVG_TO_ATTRIBUTE);
        AnimatableValue by = parseLengthPair(SVG_BY_ATTRIBUTE);
        boolean rotateAuto = false, rotateAutoReverse = false;
        float rotateAngle = 0;
        short rotateAngleUnit = SVGAngle.SVG_ANGLETYPE_UNKNOWN;
        String rotateString = element.getAttributeNS(null,
                                                     SVG_ROTATE_ATTRIBUTE);
        if (rotateString.length() != 0) {
            if (rotateString.equals("auto")) {
                rotateAuto = true;
            } else if (rotateString.equals("auto-reverse")) {
                rotateAuto = true;
                rotateAutoReverse = true;
            } else {
                class Handler implements AngleHandler {
                    float theAngle;
                    short theUnit = SVGAngle.SVG_ANGLETYPE_UNSPECIFIED;
                    public void startAngle() throws ParseException {
                    }
                    public void angleValue(float v) throws ParseException {
                        theAngle = v;
                    }
                    public void deg() throws ParseException {
                        theUnit = SVGAngle.SVG_ANGLETYPE_DEG;
                    }
                    public void grad() throws ParseException {
                        theUnit = SVGAngle.SVG_ANGLETYPE_GRAD;
                    }
                    public void rad() throws ParseException {
                        theUnit = SVGAngle.SVG_ANGLETYPE_RAD;
                    }
                    public void endAngle() throws ParseException {
                    }
                }
                AngleParser ap = new AngleParser();
                Handler h = new Handler();
                ap.setAngleHandler(h);
                try {
                    ap.parse(rotateString);
                } catch (ParseException pEx ) {
                    throw new BridgeException
                        (ctx, element,
                         pEx, ErrorConstants.ERR_ATTRIBUTE_VALUE_MALFORMED,
                         new Object[] { SVG_ROTATE_ATTRIBUTE, rotateString });
                }
                rotateAngle = h.theAngle;
                rotateAngleUnit = h.theUnit;
            }
        }
        return new MotionAnimation(timedElement,
                                   this,
                                   parseCalcMode(),
                                   parseKeyTimes(),
                                   parseKeySplines(),
                                   parseAdditive(),
                                   parseAccumulate(),
                                   parseValues(),
                                   from,
                                   to,
                                   by,
                                   parsePath(),
                                   parseKeyPoints(),
                                   rotateAuto,
                                   rotateAutoReverse,
                                   rotateAngle,
                                   rotateAngleUnit);
    }
    protected ExtendedGeneralPath parsePath() {
        Node n = element.getFirstChild();
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE
                    && SVG_NAMESPACE_URI.equals(n.getNamespaceURI())
                    && SVG_MPATH_TAG.equals(n.getLocalName())) {
                String uri = XLinkSupport.getXLinkHref((Element) n);
                Element path = ctx.getReferencedElement(element, uri);
                if (!SVG_NAMESPACE_URI.equals(path.getNamespaceURI())
                        || !SVG_PATH_TAG.equals(path.getLocalName())) {
                    throw new BridgeException
                        (ctx, element, ErrorConstants.ERR_URI_BAD_TARGET,
                         new Object[] { uri });
                }
                SVGOMPathElement pathElt = (SVGOMPathElement) path;
                AWTPathProducer app = new AWTPathProducer();
                SVGAnimatedPathDataSupport.handlePathSegList
                    (pathElt.getPathSegList(), app);
                return (ExtendedGeneralPath) app.getShape();
            }
            n = n.getNextSibling();
        }
        String pathString = element.getAttributeNS(null, SVG_PATH_ATTRIBUTE);
        if (pathString.length() == 0) {
            return null;
        }
        try {
            AWTPathProducer app = new AWTPathProducer();
            PathParser pp = new PathParser();
            pp.setPathHandler(app);
            pp.parse(pathString);
            return (ExtendedGeneralPath) app.getShape();
        } catch (ParseException pEx ) {
            throw new BridgeException
                (ctx, element, pEx, ErrorConstants.ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] { SVG_PATH_ATTRIBUTE, pathString });
        }
    }
    protected float[] parseKeyPoints() {
        String keyPointsString =
            element.getAttributeNS(null, SVG_KEY_POINTS_ATTRIBUTE);
        int len = keyPointsString.length();
        if (len == 0) {
            return null;
        }
        List keyPoints = new ArrayList(7);
        int i = 0, start = 0, end;
        char c;
outer:  while (i < len) {
            while (keyPointsString.charAt(i) == ' ') {
                i++;
                if (i == len) {
                    break outer;
                }
            }
            start = i++;
            if (i != len) {
                c = keyPointsString.charAt(i);
                while (c != ' ' && c != ';' && c != ',') {
                    i++;
                    if (i == len) {
                        break;
                    }
                    c = keyPointsString.charAt(i);
                }
            }
            end = i++;
            try {
                float keyPointCoord =
                    Float.parseFloat(keyPointsString.substring(start, end));
                keyPoints.add(new Float(keyPointCoord));
            } catch (NumberFormatException nfEx ) {
                throw new BridgeException
                    (ctx, element, nfEx, ErrorConstants.ERR_ATTRIBUTE_VALUE_MALFORMED,
                     new Object[] { SVG_KEY_POINTS_ATTRIBUTE, keyPointsString });
            }
        }
        len = keyPoints.size();
        float[] ret = new float[len];
        for (int j = 0; j < len; j++) {
            ret[j] = ((Float) keyPoints.get(j)).floatValue();
        }
        return ret;
    }
    protected int getDefaultCalcMode() {
        return MotionAnimation.CALC_MODE_PACED;
    }
    protected AnimatableValue[] parseValues() {
        String valuesString = element.getAttributeNS(null,
                                                     SVG_VALUES_ATTRIBUTE);
        int len = valuesString.length();
        if (len == 0) {
            return null;
        }
        return parseValues(valuesString);
    }
    protected AnimatableValue[] parseValues(String s) {
        try {
            LengthPairListParser lplp = new LengthPairListParser();
            LengthArrayProducer lap = new LengthArrayProducer();
            lplp.setLengthListHandler(lap);
            lplp.parse(s);
            short[] types = lap.getLengthTypeArray();
            float[] values = lap.getLengthValueArray();
            AnimatableValue[] ret = new AnimatableValue[types.length / 2];
            for (int i = 0; i < types.length; i += 2) {
                float x = animationTarget.svgToUserSpace
                    (values[i], types[i], AnimationTarget.PERCENTAGE_VIEWPORT_WIDTH);
                float y = animationTarget.svgToUserSpace
                    (values[i + 1], types[i + 1], AnimationTarget.PERCENTAGE_VIEWPORT_HEIGHT);
                ret[i / 2] = new AnimatableMotionPointValue(animationTarget, x, y, 0);
            }
            return ret;
        } catch (ParseException pEx ) {
            throw new BridgeException
                (ctx, element, pEx, ErrorConstants.ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] { SVG_VALUES_ATTRIBUTE, s });
        }
    }
    protected AnimatableValue parseLengthPair(String ln) {
        String s = element.getAttributeNS(null, ln);
        if (s.length() == 0) {
            return null;
        }
        return parseValues(s)[0];
    }
    public AnimatableValue getUnderlyingValue() {
        return new AnimatableMotionPointValue(animationTarget, 0f, 0f, 0f);
    }
    protected void initializeAnimation() {
        String uri = XLinkSupport.getXLinkHref(element);
        Node t;
        if (uri.length() == 0) {
            t = element.getParentNode();
        } else {
            t = ctx.getReferencedElement(element, uri);
            if (t.getOwnerDocument() != element.getOwnerDocument()) {
                throw new BridgeException
                    (ctx, element, ErrorConstants.ERR_URI_BAD_TARGET,
                     new Object[] { uri });
            }
        }
        animationTarget = null;
        if (t instanceof SVGOMElement) {
            targetElement = (SVGOMElement) t;
            animationTarget = targetElement;
        }
        if (animationTarget == null) {
            throw new BridgeException
                (ctx, element, ErrorConstants.ERR_URI_BAD_TARGET,
                 new Object[] { uri });
        }
        timedElement = createTimedElement();
        animation = createAnimation(animationTarget);
        eng.addAnimation(animationTarget, AnimationEngine.ANIM_TYPE_OTHER,
                         attributeNamespaceURI, attributeLocalName, animation);
    }
}
