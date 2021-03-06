package org.apache.batik.ext.awt.image.renderable;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.RenderableImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.PadRed;
import org.apache.batik.ext.awt.image.rendered.RenderedImageCachableRed;
public abstract class AbstractRable implements Filter {
    protected Vector srcs;
    protected Map    props = new HashMap();
    protected long   stamp = 0;
    protected AbstractRable() {
        srcs = new Vector();
    }
    protected AbstractRable(Filter src) {
        init(src, null);
    }
    protected AbstractRable(Filter src, Map props) {
        init(src, props);
    }
    protected AbstractRable(List srcs) {
        this(srcs, null);
    }
    protected AbstractRable(List srcs, Map props) {
        init(srcs, props);
    }
    public final void touch() { stamp++; }
    public long getTimeStamp() { return stamp; }
    protected void init(Filter src) {
        touch();
        this.srcs   = new Vector(1);
        if (src != null) {
            this.srcs.add(src);
        }
    }
    protected void init(Filter src, Map props) {
        init (src);
        if(props != null){
            this.props.putAll(props);
        }
    }
    protected void init(List srcs) {
        touch();
        this.srcs   = new Vector(srcs);
    }
    protected void init(List srcs, Map props) {
        init (srcs);
        if(props != null)
            this.props.putAll(props);
    }
    public Rectangle2D getBounds2D() {
        Rectangle2D bounds = null;
        if (this.srcs.size() != 0) {
            Iterator i = srcs.iterator();
            Filter src = (Filter)i.next();
            bounds = (Rectangle2D)src.getBounds2D().clone();
            Rectangle2D r;
            while (i.hasNext()) {
                src = (Filter)i.next();
                r = src.getBounds2D();
                Rectangle2D.union(bounds, r, bounds);
            }
        }
        return bounds;
    }
    public Vector getSources() {
        return srcs;
    }
    public RenderedImage createDefaultRendering() {
        return createScaledRendering(100, 100, null);
    }
    public RenderedImage createScaledRendering(int w, int h,
                                           RenderingHints hints) {
        float sX = w/getWidth();
        float sY = h/getHeight();
        float scale = Math.min(sX, sY);
        AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
        RenderContext rc = new RenderContext(at, hints);
        float dX = (getWidth()*scale)-w;
        float dY = (getHeight()*scale)-h;
        RenderedImage ri = createRendering(rc);
        CachableRed cr = RenderedImageCachableRed.wrap(ri);
        return new PadRed(cr, new Rectangle((int)(dX/2), (int)(dY/2), w, h),
                          PadMode.ZERO_PAD, null);
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
        Object ret = props.get(name);
        if (ret != null) return ret;
        Iterator i = srcs.iterator();
        while (i.hasNext()) {
            RenderableImage ri = (RenderableImage)i.next();
            ret = ri.getProperty(name);
            if (ret != null) return ret;
        }
        return null;
    }
    public String [] getPropertyNames() {
        Set keys = props.keySet();
        Iterator iter = keys.iterator();
        String[] ret  = new String[keys.size()];
        int i=0;
        while (iter.hasNext()) {
            ret[i++] = (String)iter.next();
        }
        iter = srcs.iterator();
        while (iter.hasNext()) {
            RenderableImage ri = (RenderableImage)iter.next();
            String [] srcProps = ri.getPropertyNames();
            if (srcProps.length != 0) {
                String [] tmp = new String[ret.length+srcProps.length];
                System.arraycopy(ret,0,tmp,0,ret.length);
                System.arraycopy(tmp,ret.length,srcProps,0,srcProps.length);
                ret = tmp;
            }
        }
        return ret;
    }
    public boolean isDynamic() { return false; }
    public Shape getDependencyRegion(int srcIndex,
                                     Rectangle2D outputRgn) {
        if ((srcIndex < 0) || (srcIndex > srcs.size()))
            throw new IndexOutOfBoundsException
                ("Nonexistant source requested.");
        Rectangle2D srect = (Rectangle2D)outputRgn.clone();
        Rectangle2D bounds = getBounds2D();
        if ( ! bounds.intersects(srect) )
            return new Rectangle2D.Float();
        Rectangle2D.intersect(srect, bounds, srect);
        return srect;
    }
    public Shape getDirtyRegion(int srcIndex,
                                Rectangle2D inputRgn) {
        if ((srcIndex < 0) || (srcIndex > srcs.size()))
            throw new IndexOutOfBoundsException
                ("Nonexistant source requested.");
        Rectangle2D drect = (Rectangle2D)inputRgn.clone();
        Rectangle2D bounds = getBounds2D();
        if ( ! bounds.intersects(drect) )
            return new Rectangle2D.Float();
        Rectangle2D.intersect(drect, bounds, drect);
        return drect;
    }
}
