package org.apache.batik.ext.awt.image.renderable;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.util.Map;
import java.util.Vector;
public class DeferRable implements Filter {
    volatile Filter  src;
    Rectangle2D bounds;
    Map         props;
    public DeferRable() {
    }
    public synchronized Filter getSource() {
        while (src == null) {
            try {
                wait();
            } catch(InterruptedException ie) {
            }
        }
        return src;
    }
    public synchronized void setSource(Filter src) {
        if (this.src != null) return;
        this.src    = src;
        this.bounds = src.getBounds2D();
        notifyAll();
    }
    public synchronized void setBounds(Rectangle2D bounds) {
        if (this.bounds != null) return;
        this.bounds = bounds;
        notifyAll();
    }
    public synchronized void setProperties(Map props) {
        this.props = props;
        notifyAll();
    }
    public long getTimeStamp() {
        return getSource().getTimeStamp();
    }
    public Vector getSources() {
        return getSource().getSources();
    }
    public boolean isDynamic() {
        return getSource().isDynamic();
    }
    public Rectangle2D getBounds2D() {
        synchronized(this) {
            while ((src == null) && (bounds == null))  {
                try {
                    wait();
                }
                catch(InterruptedException ie) {
                }
            }
        }
        if (src != null)
            return src.getBounds2D();
        return bounds;
    }
    public float getMinX() {
        return (float)getBounds2D().getX();
    }
    public float getMinY() {
        return (float)getBounds2D().getY();
    }
    public float getWidth() {
        return (float)getBounds2D().getWidth();
    }
    public float getHeight() {
        return (float)getBounds2D().getHeight();
    }
    public Object getProperty(String name) {
        synchronized (this) {
            while ((src == null) && (props == null)) {
                try {
                    wait();
                } catch(InterruptedException ie) { }
            }
        }
        if (src != null)
            return src.getProperty(name);
        return props.get(name);
    }
    public String [] getPropertyNames() {
        synchronized (this) {
            while ((src == null) && (props == null)) {
                try {
                    wait();
                } catch(InterruptedException ie) { }
            }
        }
        if (src != null)
            return src.getPropertyNames();
        String [] ret = new String[props.size()];
        props.keySet().toArray(ret);
        return ret;
    }
    public RenderedImage createDefaultRendering() {
        return getSource().createDefaultRendering();
    }
    public RenderedImage createScaledRendering(int w, int h,
                                               RenderingHints hints) {
        return getSource().createScaledRendering(w, h, hints);
    }
    public RenderedImage createRendering(RenderContext rc) {
        return getSource().createRendering(rc);
    }
    public Shape getDependencyRegion(int srcIndex,
                                     Rectangle2D outputRgn) {
        return getSource().getDependencyRegion(srcIndex, outputRgn);
    }
    public Shape getDirtyRegion(int srcIndex,
                                Rectangle2D inputRgn) {
        return getSource().getDirtyRegion(srcIndex, inputRgn);
    }
}
