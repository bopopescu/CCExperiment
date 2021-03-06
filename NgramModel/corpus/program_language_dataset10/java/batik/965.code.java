package org.apache.batik.gvt;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.apache.batik.util.HaltingThread;
public class ShapeNode extends AbstractGraphicsNode {
    protected Shape shape;
    protected ShapePainter shapePainter;
    private Rectangle2D primitiveBounds;
    private Rectangle2D geometryBounds;
    private Rectangle2D sensitiveBounds;
    private Shape paintedArea;
    private Shape sensitiveArea;
    public ShapeNode() {}
    public void setShape(Shape newShape) {
        fireGraphicsNodeChangeStarted();
        invalidateGeometryCache();
        this.shape = newShape;
        if(this.shapePainter != null){
            if (newShape != null) {
                this.shapePainter.setShape(newShape);
            } else {
                this.shapePainter = null;
            }
        }
        fireGraphicsNodeChangeCompleted();
    }
    public Shape getShape() {
        return shape;
    }
    public void setShapePainter(ShapePainter newShapePainter) {
        if (shape == null) 
            return;
        fireGraphicsNodeChangeStarted();
        invalidateGeometryCache();
        this.shapePainter = newShapePainter;
        if(shapePainter != null && shape != this.shapePainter.getShape()){
            shapePainter.setShape(shape);
        }
        fireGraphicsNodeChangeCompleted();
    }
    public ShapePainter getShapePainter() {
        return shapePainter;
    }
    public void paint(Graphics2D g2d) {
        if (isVisible)
            super.paint(g2d);
    }
    public void primitivePaint(Graphics2D g2d) {
        if (shapePainter != null) {
            shapePainter.paint(g2d);
        }
    }
    protected void invalidateGeometryCache() {
        super.invalidateGeometryCache();
        primitiveBounds = null;
        geometryBounds = null;
        sensitiveBounds = null;
        paintedArea = null;
        sensitiveArea = null;
    }
    public void setPointerEventType(int pointerEventType) {
        super.setPointerEventType(pointerEventType);
        sensitiveBounds = null;
        sensitiveArea = null;
    }
    public boolean contains(Point2D p) {
        switch(pointerEventType) {
        case VISIBLE_PAINTED:
        case VISIBLE_FILL:
        case VISIBLE_STROKE:
        case VISIBLE:
            if (!isVisible) return false;
        case PAINTED:
        case FILL:
        case STROKE:
        case ALL: {
            Rectangle2D b = getSensitiveBounds();
            if (b == null || !b.contains(p))
                return false;
            return inSensitiveArea(p);
        }
        case NONE:
        default:
            return false;
        }
    }
    public boolean intersects(Rectangle2D r) {
        Rectangle2D b = getBounds();
        if (b != null) {
            return (b.intersects(r) &&
                    paintedArea != null &&
                    paintedArea.intersects(r));
        }
        return false;
    }
    public Rectangle2D getPrimitiveBounds() {
        if (!isVisible)    return null;
        if (shape == null) return null;
        if (primitiveBounds != null) 
            return primitiveBounds;
        if (shapePainter == null)
            primitiveBounds = shape.getBounds2D();
        else
            primitiveBounds = shapePainter.getPaintedBounds2D();
        if (HaltingThread.hasBeenHalted()) {
            invalidateGeometryCache();
        }
        return primitiveBounds;
    }
    public boolean inSensitiveArea(Point2D pt) {
        if (shapePainter == null)
            return false;
        ShapePainter strokeShapePainter = null;
        ShapePainter fillShapePainter = null;
        if (shapePainter instanceof StrokeShapePainter) {
            strokeShapePainter = shapePainter;
        } else if (shapePainter instanceof FillShapePainter) {
            fillShapePainter = shapePainter;
        } else if (shapePainter instanceof CompositeShapePainter) {
            CompositeShapePainter cp = (CompositeShapePainter)shapePainter;
            for (int i=0; i < cp.getShapePainterCount(); ++i) {
                ShapePainter sp = cp.getShapePainter(i);
                if (sp instanceof StrokeShapePainter) {
                    strokeShapePainter = sp;
                } else if (sp instanceof FillShapePainter) {
                    fillShapePainter = sp;
                }
            }
        } else {
            return false; 
        }
        switch(pointerEventType) {
        case VISIBLE_PAINTED:
        case PAINTED:
            return shapePainter.inPaintedArea(pt);
        case VISIBLE:
        case ALL:
            return shapePainter.inSensitiveArea(pt);
        case VISIBLE_FILL:
        case FILL:
            if (fillShapePainter != null)
                return fillShapePainter.inSensitiveArea(pt);
            break;
        case VISIBLE_STROKE:
        case STROKE:
            if (strokeShapePainter != null)
                return strokeShapePainter.inSensitiveArea(pt);
            break;
        case NONE:
        default:
        }
        return false;
    }
    public Rectangle2D getSensitiveBounds() {
        if (sensitiveBounds != null)
            return sensitiveBounds;
        if (shapePainter == null)
            return null;
        ShapePainter strokeShapePainter = null;
        ShapePainter fillShapePainter = null;
        if (shapePainter instanceof StrokeShapePainter) {
            strokeShapePainter = shapePainter;
        } else if (shapePainter instanceof FillShapePainter) {
            fillShapePainter = shapePainter;
        } else if (shapePainter instanceof CompositeShapePainter) {
            CompositeShapePainter cp = (CompositeShapePainter)shapePainter;
            for (int i=0; i < cp.getShapePainterCount(); ++i) {
                ShapePainter sp = cp.getShapePainter(i);
                if (sp instanceof StrokeShapePainter) {
                    strokeShapePainter = sp;
                } else if (sp instanceof FillShapePainter) {
                    fillShapePainter = sp;
                }
            }
        } else return null; 
        switch(pointerEventType) {
        case VISIBLE_PAINTED:
        case PAINTED:
            sensitiveBounds = shapePainter.getPaintedBounds2D();
            break;
        case VISIBLE_FILL:
        case FILL:
            if (fillShapePainter != null) {
                sensitiveBounds = fillShapePainter.getSensitiveBounds2D();
            }
            break;
        case VISIBLE_STROKE:
        case STROKE:
            if (strokeShapePainter != null) {
                sensitiveBounds = strokeShapePainter.getSensitiveBounds2D();
            }
            break;
        case VISIBLE:
        case ALL:
            sensitiveBounds = shapePainter.getSensitiveBounds2D();
            break;
        case NONE:
        default:
        }
        return sensitiveBounds;
    }
    public Shape getSensitiveArea() {
        if (sensitiveArea != null) 
            return sensitiveArea;
        if (shapePainter == null)
            return null;
        ShapePainter strokeShapePainter = null;
        ShapePainter fillShapePainter = null;
        if (shapePainter instanceof StrokeShapePainter) {
            strokeShapePainter = shapePainter;
        } else if (shapePainter instanceof FillShapePainter) {
            fillShapePainter = shapePainter;
        } else if (shapePainter instanceof CompositeShapePainter) {
            CompositeShapePainter cp = (CompositeShapePainter)shapePainter;
            for (int i=0; i < cp.getShapePainterCount(); ++i) {
                ShapePainter sp = cp.getShapePainter(i);
                if (sp instanceof StrokeShapePainter) {
                    strokeShapePainter = sp;
                } else if (sp instanceof FillShapePainter) {
                    fillShapePainter = sp;
                }
            }
        } else return null; 
        switch(pointerEventType) {
        case VISIBLE_PAINTED:
        case PAINTED:
            sensitiveArea = shapePainter.getPaintedArea();
            break;
        case VISIBLE_FILL:
        case FILL:
            if (fillShapePainter != null) {
                sensitiveArea = fillShapePainter.getSensitiveArea();
            }
            break;
        case VISIBLE_STROKE:
        case STROKE:
            if (strokeShapePainter != null) {
                sensitiveArea = strokeShapePainter.getSensitiveArea();
            }
            break;
        case VISIBLE:
        case ALL:
            sensitiveArea = shapePainter.getSensitiveArea();
            break;
        case NONE:
        default:
        }
        return sensitiveArea;
    }
    public Rectangle2D getGeometryBounds(){
        if (geometryBounds == null) {
            if (shape == null) {
                return null;
            }
            geometryBounds = normalizeRectangle(shape.getBounds2D());
        }
        return geometryBounds;
    }
    public Shape getOutline() {
        return shape;
    }
}
