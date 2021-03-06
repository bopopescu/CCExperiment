package org.apache.batik.gvt;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
public class ImageNode extends CompositeGraphicsNode {
    protected boolean hitCheckChildren = false;
    public ImageNode() {}
    public void setVisible(boolean isVisible) {
        fireGraphicsNodeChangeStarted();
        this.isVisible = isVisible;
        invalidateGeometryCache();
        fireGraphicsNodeChangeCompleted();
    }
    public Rectangle2D getPrimitiveBounds() {
        if (!isVisible)    return null;
        return super.getPrimitiveBounds();
    }
    public void setHitCheckChildren(boolean hitCheckChildren) {
        this.hitCheckChildren = hitCheckChildren;
    }
    public boolean getHitCheckChildren() { 
        return hitCheckChildren; 
    }
    public void paint(Graphics2D g2d) {
        if (isVisible) {
            super.paint(g2d);
        }
    }
    public boolean contains(Point2D p) {
        switch(pointerEventType) {
        case VISIBLE_PAINTED:
        case VISIBLE_FILL:
        case VISIBLE_STROKE:
        case VISIBLE:
            return isVisible && super.contains(p);
        case PAINTED:
        case FILL:
        case STROKE:
        case ALL:
            return super.contains(p);
        case NONE:
            return false;
        default:
            return false;
        }
    }
    public GraphicsNode nodeHitAt(Point2D p) {
        if (hitCheckChildren) return super.nodeHitAt(p);
        return (contains(p) ? this : null);
    }
    public void setImage(GraphicsNode newImage) {
        fireGraphicsNodeChangeStarted();
        invalidateGeometryCache();
        if (count == 0) ensureCapacity(1);
        children[0] = newImage;
        ((AbstractGraphicsNode)newImage).setParent(this);
        ((AbstractGraphicsNode)newImage).setRoot(getRoot());
        count=1;
        fireGraphicsNodeChangeCompleted();
    }
    public GraphicsNode getImage() {
        if (count > 0) {
            return children[0];
        } else {
            return null;
        }
    }
}
