package org.apache.batik.bridge;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.gvt.text.TextPath;
import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathParser;
import org.w3c.dom.Element;
public class SVGTextPathElementBridge extends AnimatableGenericSVGBridge
                                      implements ErrorConstants {
    public SVGTextPathElementBridge() {}
    public String getLocalName() {
        return SVG_TEXT_PATH_TAG;
    }
    public void handleElement(BridgeContext ctx, Element e) {
    }
    public TextPath createTextPath(BridgeContext ctx, Element textPathElement) {
        String uri = XLinkSupport.getXLinkHref(textPathElement);
        Element pathElement = ctx.getReferencedElement(textPathElement, uri);
        if ((pathElement == null) ||
            (!SVG_NAMESPACE_URI.equals(pathElement.getNamespaceURI())) ||
            (!pathElement.getLocalName().equals(SVG_PATH_TAG))) {
            throw new BridgeException(ctx, textPathElement, ERR_URI_BAD_TARGET,
                                      new Object[] {uri});
        }
        String s = pathElement.getAttributeNS(null, SVG_D_ATTRIBUTE);
        Shape pathShape = null;
        if (s.length() != 0) {
            AWTPathProducer app = new AWTPathProducer();
            app.setWindingRule(CSSUtilities.convertFillRule(pathElement));
            try {
                PathParser pathParser = new PathParser();
                pathParser.setPathHandler(app);
                pathParser.parse(s);
            } catch (ParseException pEx ) {
               throw new BridgeException
                   (ctx, pathElement, pEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                    new Object[] {SVG_D_ATTRIBUTE});
            } finally {
                pathShape = app.getShape();
            }
        } else {
            throw new BridgeException(ctx, pathElement, ERR_ATTRIBUTE_MISSING,
                                      new Object[] {SVG_D_ATTRIBUTE});
        }
        s = pathElement.getAttributeNS(null, SVG_TRANSFORM_ATTRIBUTE);
        if (s.length() != 0) {
            AffineTransform tr =
                SVGUtilities.convertTransform(pathElement,
                                              SVG_TRANSFORM_ATTRIBUTE, s, ctx);
            pathShape = tr.createTransformedShape(pathShape);
        }
        TextPath textPath = new TextPath(new GeneralPath(pathShape));
        s = textPathElement.getAttributeNS(null, SVG_START_OFFSET_ATTRIBUTE);
        if (s.length() > 0) {
            float startOffset = 0;
            int percentIndex = s.indexOf('%');
            if (percentIndex != -1) {
                float pathLength = textPath.lengthOfPath();
                String percentString = s.substring(0,percentIndex);
                float startOffsetPercent = 0;
                try {
                    startOffsetPercent = SVGUtilities.convertSVGNumber(percentString);
                } catch (NumberFormatException e) {
                    throw new BridgeException
                        (ctx, textPathElement, ERR_ATTRIBUTE_VALUE_MALFORMED,
                         new Object[] {SVG_START_OFFSET_ATTRIBUTE, s});
                }
                startOffset = (float)(startOffsetPercent * pathLength/100.0);
            } else {
                UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, textPathElement);
                startOffset = UnitProcessor.svgOtherLengthToUserSpace(s, SVG_START_OFFSET_ATTRIBUTE, uctx);
            }
            textPath.setStartOffset(startOffset);
        }
        return textPath;
    }
}
