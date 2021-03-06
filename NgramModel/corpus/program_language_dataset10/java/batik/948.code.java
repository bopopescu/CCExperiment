package org.apache.batik.gvt;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
public class CanvasGraphicsNode extends CompositeGraphicsNode {
    protected AffineTransform positionTransform;
    protected AffineTransform viewingTransform;
    protected Paint backgroundPaint;
    public CanvasGraphicsNode() {}
    public void setBackgroundPaint(Paint newBackgroundPaint) {
        this.backgroundPaint = newBackgroundPaint;
    }
    public Paint getBackgroundPaint() {
        return backgroundPaint;
    }
    public void setPositionTransform(AffineTransform at) {
        fireGraphicsNodeChangeStarted();
        invalidateGeometryCache();
        this.positionTransform = at;
        if (positionTransform != null) {
            transform = new AffineTransform(positionTransform);
            if (viewingTransform != null)
                transform.concatenate(viewingTransform);
        } else if (viewingTransform != null)
            transform = new AffineTransform(viewingTransform);
        else
            transform = new AffineTransform();
        if (transform.getDeterminant() != 0){
            try{
                inverseTransform = transform.createInverse();
            }catch(NoninvertibleTransformException e){
                throw new Error( e.getMessage() );
            }
        }
        else{
            inverseTransform = transform;
        }
        fireGraphicsNodeChangeCompleted();
    }
    public AffineTransform getPositionTransform() {
        return positionTransform;
    }
    public void setViewingTransform(AffineTransform at) {
        fireGraphicsNodeChangeStarted();
        invalidateGeometryCache();
        this.viewingTransform = at;
        if (positionTransform != null) {
            transform = new AffineTransform(positionTransform);
            if (viewingTransform != null)
                transform.concatenate(viewingTransform);
        } else if (viewingTransform != null)
            transform = new AffineTransform(viewingTransform);
        else
            transform = new AffineTransform();
        if(transform.getDeterminant() != 0){
            try{
                inverseTransform = transform.createInverse();
            }catch(NoninvertibleTransformException e){
                throw new Error( e.getMessage() );
            }
        }
        else{
            inverseTransform = transform;
        }
        fireGraphicsNodeChangeCompleted();
    }
    public AffineTransform getViewingTransform() {
        return viewingTransform;
    }
    public void primitivePaint(Graphics2D g2d) {
        if (backgroundPaint != null) {
            g2d.setPaint(backgroundPaint);
            g2d.fill(g2d.getClip()); 
        }
        super.primitivePaint(g2d);
    }
}
