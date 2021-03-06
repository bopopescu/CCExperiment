package org.apache.batik.bridge;
import java.awt.geom.Line2D;
import org.apache.batik.dom.svg.AbstractSVGAnimatedLength;
import org.apache.batik.dom.svg.AnimatedLiveAttributeValue;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.svg.SVGOMLineElement;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.ShapePainter;
import org.w3c.dom.Element;
public class SVGLineElementBridge extends SVGDecoratedShapeElementBridge {
    public SVGLineElementBridge() {}
    public String getLocalName() {
        return SVG_LINE_TAG;
    }
    public Bridge getInstance() {
        return new SVGLineElementBridge();
    }
    protected ShapePainter createFillStrokePainter(BridgeContext ctx,
                                                   Element e,
                                                   ShapeNode shapeNode) {
        return PaintServer.convertStrokePainter(e, shapeNode, ctx);
    }
    protected void buildShape(BridgeContext ctx,
                              Element e,
                              ShapeNode shapeNode) {
        try {
            SVGOMLineElement le = (SVGOMLineElement) e;
            AbstractSVGAnimatedLength _x1 =
                (AbstractSVGAnimatedLength) le.getX1();
            float x1 = _x1.getCheckedValue();
            AbstractSVGAnimatedLength _y1 =
                (AbstractSVGAnimatedLength) le.getY1();
            float y1 = _y1.getCheckedValue();
            AbstractSVGAnimatedLength _x2 =
                (AbstractSVGAnimatedLength) le.getX2();
            float x2 = _x2.getCheckedValue();
            AbstractSVGAnimatedLength _y2 =
                (AbstractSVGAnimatedLength) le.getY2();
            float y2 = _y2.getCheckedValue();
            shapeNode.setShape(new Line2D.Float(x1, y1, x2, y2));
        } catch (LiveAttributeException ex) {
            throw new BridgeException(ctx, ex);
        }
    }
    public void handleAnimatedAttributeChanged
            (AnimatedLiveAttributeValue alav) {
        if (alav.getNamespaceURI() == null) {
            String ln = alav.getLocalName();
            if (ln.equals(SVG_X1_ATTRIBUTE)
                    || ln.equals(SVG_Y1_ATTRIBUTE)
                    || ln.equals(SVG_X2_ATTRIBUTE)
                    || ln.equals(SVG_Y2_ATTRIBUTE)) {
                buildShape(ctx, e, (ShapeNode)node);
                handleGeometryChanged();
                return;
            }
        }
        super.handleAnimatedAttributeChanged(alav);
    }
}
