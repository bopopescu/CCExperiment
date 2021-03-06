package org.apache.batik.bridge;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.dom.svg.AnimatedLiveAttributeValue;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.svg.SVGAnimatedPathDataSupport;
import org.apache.batik.dom.svg.SVGOMAnimatedPathData;
import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.dom.svg.SVGPathContext;
import org.apache.batik.ext.awt.geom.PathLength;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.parser.AWTPathProducer;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGPathSegList;
public class SVGPathElementBridge extends SVGDecoratedShapeElementBridge 
       implements SVGPathContext {
    protected static final Shape DEFAULT_SHAPE = new GeneralPath();
    public SVGPathElementBridge() {}
    public String getLocalName() {
        return SVG_PATH_TAG;
    }
    public Bridge getInstance() {
        return new SVGPathElementBridge();
    }
    protected void buildShape(BridgeContext ctx,
                              Element e,
                              ShapeNode shapeNode) {
        SVGOMPathElement pe = (SVGOMPathElement) e;
        AWTPathProducer app = new AWTPathProducer();
        try {
            SVGOMAnimatedPathData _d = pe.getAnimatedPathData();
            _d.check();
            SVGPathSegList p = _d.getAnimatedPathSegList();
            app.setWindingRule(CSSUtilities.convertFillRule(e));
            SVGAnimatedPathDataSupport.handlePathSegList(p, app);
        } catch (LiveAttributeException ex) {
            throw new BridgeException(ctx, ex);
        } finally {
            shapeNode.setShape(app.getShape());
        }
    }
    public void handleAnimatedAttributeChanged
            (AnimatedLiveAttributeValue alav) {
        if (alav.getNamespaceURI() == null &&
                alav.getLocalName().equals(SVG_D_ATTRIBUTE)) {
            buildShape(ctx, e, (ShapeNode) node);
            handleGeometryChanged();
        } else {
            super.handleAnimatedAttributeChanged(alav);
        }
    }
    protected void handleCSSPropertyChanged(int property) {
        switch(property) {
        case SVGCSSEngine.FILL_RULE_INDEX:
            buildShape(ctx, e, (ShapeNode) node);
            handleGeometryChanged();
            break;
        default:
            super.handleCSSPropertyChanged(property);
        }
    }
    protected Shape pathLengthShape;
    protected PathLength pathLength;
    protected PathLength getPathLengthObj() {
        Shape s = ((ShapeNode)node).getShape();
        if (pathLengthShape != s) {
            pathLength = new PathLength(s);
            pathLengthShape = s;
        }
        return pathLength;
    }
    public float getTotalLength() {
        PathLength pl = getPathLengthObj();
        return pl.lengthOfPath();
    }
    public Point2D getPointAtLength(float distance) {
        PathLength pl = getPathLengthObj();
        return pl.pointAtLength(distance);
    }
    public int getPathSegAtLength(float distance) {
        PathLength pl = getPathLengthObj();
        return pl.segmentAtLength(distance);
    }
}
