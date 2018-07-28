package org.apache.batik.bridge;
import java.awt.Shape;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.gvt.CompositeShapePainter;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.ShapePainter;
import org.w3c.dom.Element;
public abstract class SVGDecoratedShapeElementBridge
        extends SVGShapeElementBridge {
    protected SVGDecoratedShapeElementBridge() {}
    ShapePainter createFillStrokePainter(BridgeContext ctx, 
                                         Element e,
                                         ShapeNode shapeNode) {
        return super.createShapePainter(ctx, e, shapeNode);
    }
    ShapePainter createMarkerPainter(BridgeContext ctx, 
                                     Element e,
                                     ShapeNode shapeNode) {
        return PaintServer.convertMarkers(e, shapeNode, ctx);
    }
    protected ShapePainter createShapePainter(BridgeContext ctx,
                                              Element e,
                                              ShapeNode shapeNode) {
        ShapePainter fillAndStroke;
        fillAndStroke = createFillStrokePainter(ctx, e, shapeNode);
        ShapePainter markerPainter = createMarkerPainter(ctx, e, shapeNode);
        Shape shape = shapeNode.getShape();
        ShapePainter painter;
        if (markerPainter != null) {
            if (fillAndStroke != null) {
                CompositeShapePainter cp = new CompositeShapePainter(shape);
                cp.addShapePainter(fillAndStroke);
                cp.addShapePainter(markerPainter);
                painter = cp;
            } else {
                painter = markerPainter;
            }
        } else {
            painter = fillAndStroke;
        }
        return painter;
    }
    protected void handleCSSPropertyChanged(int property) {
        switch(property) {
        case SVGCSSEngine.MARKER_START_INDEX:
        case SVGCSSEngine.MARKER_MID_INDEX:
        case SVGCSSEngine.MARKER_END_INDEX:
            if (!hasNewShapePainter) {
                hasNewShapePainter = true;
                ShapeNode shapeNode = (ShapeNode)node;
                shapeNode.setShapePainter(createShapePainter(ctx, e, shapeNode));
            }
            break;
        default:
            super.handleCSSPropertyChanged(property);
        }
    }
}