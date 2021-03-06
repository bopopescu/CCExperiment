package org.apache.batik.ext.awt.image.renderable;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.RenderableImage;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.List;
import org.apache.batik.ext.awt.image.CompositeRule;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.SVGComposite;
import org.apache.batik.ext.awt.image.rendered.AffineRed;
import org.apache.batik.ext.awt.image.rendered.TileCacheRed;
public class FilterResRable8Bit extends AbstractRable
    implements FilterResRable, PaintRable {
    private int filterResolutionX = -1;
    private int filterResolutionY = -1;
    public FilterResRable8Bit() {
    }
    public FilterResRable8Bit(Filter src, int filterResX, int filterResY) {
        init(src, null);
        setFilterResolutionX(filterResX);
        setFilterResolutionY(filterResY);
    }
    public Filter getSource() {
        return (Filter)srcs.get(0);
    }
    public void setSource(Filter src){
        init(src, null);
    }
    public int getFilterResolutionX(){
        return filterResolutionX;
    }
    public void setFilterResolutionX(int filterResolutionX){
        if(filterResolutionX < 0){
            throw new IllegalArgumentException();
        }
        touch();
        this.filterResolutionX = filterResolutionX;
    }
    public int getFilterResolutionY(){
        return filterResolutionY;
    }
    public void setFilterResolutionY(int filterResolutionY){
        touch();
        this.filterResolutionY = filterResolutionY;
    }
    public boolean allPaintRable(RenderableImage ri) {
        if (!(ri instanceof PaintRable))
            return false;
        List v = ri.getSources();
        if (v == null) return true;
        Iterator i = v.iterator();
        while (i.hasNext()) {
            RenderableImage nri = (RenderableImage)i.next();
            if (!allPaintRable(nri)) return false;
        }
        return true;
    }
    public boolean distributeAcross(RenderableImage src, Graphics2D g2d) {
        boolean ret;
        if (src instanceof PadRable) {
            PadRable pad = (PadRable)src;
            Shape clip = g2d.getClip();
            g2d.clip(pad.getPadRect());
            ret = distributeAcross(pad.getSource(), g2d);
            g2d.setClip(clip);
            return ret;
        }
        if (src instanceof CompositeRable) {
            CompositeRable comp = (CompositeRable)src;
            if (comp.getCompositeRule() != CompositeRule.OVER)
                return false;
            if (false) {
                ColorSpace crCS  = comp.getOperationColorSpace();
                ColorSpace g2dCS = GraphicsUtil.getDestinationColorSpace(g2d);
                if ((g2dCS == null) || (g2dCS != crCS))
                    return false;
            }
            List v = comp.getSources();
            if (v == null) return true;
            ListIterator li = v.listIterator(v.size());
            while (li.hasPrevious()) {
                RenderableImage csrc = (RenderableImage)li.previous();
                if (!allPaintRable(csrc)) {
                    li.next();
                    break;
                }
            }
            if (!li.hasPrevious()) {
                GraphicsUtil.drawImage(g2d, comp);
                return true;
            }
            if (!li.hasNext())
                return false;
            int idx = li.nextIndex();  
            Filter f = new CompositeRable8Bit(v.subList(0, idx),
                                              comp.getCompositeRule(),
                                              comp.isColorSpaceLinear());
            f = new FilterResRable8Bit(f, getFilterResolutionX(),
                                       getFilterResolutionY());
            GraphicsUtil.drawImage(g2d, f);
            while (li.hasNext()) {
                PaintRable pr = (PaintRable)li.next();
                if (!pr.paintRable(g2d)) {
                    Filter     prf  = (Filter)pr;
                    prf = new FilterResRable8Bit(prf, getFilterResolutionX(),
                                                 getFilterResolutionY());
                    GraphicsUtil.drawImage(g2d, prf);
                }
            }
            return true;
        }
        return false;
    }
    public boolean paintRable(Graphics2D g2d) {
        Composite c = g2d.getComposite();
        if (!SVGComposite.OVER.equals(c))
            return false;
        Filter src = getSource();
        return distributeAcross(src, g2d);
    }
    Reference resRed = null;
    float     resScale = 0;
    private float getResScale() {
        return resScale;
    }
    private RenderedImage getResRed(RenderingHints hints) {
        Rectangle2D imageRect = getBounds2D();
        double resScaleX = getFilterResolutionX()/imageRect.getWidth();
        double resScaleY = getFilterResolutionY()/imageRect.getHeight();
        float resScale = (float)Math.min(resScaleX, resScaleY);
        RenderedImage ret;
        if (resScale == this.resScale) {
            ret = (RenderedImage)resRed.get();
            if (ret != null)
                return ret;
        }
        AffineTransform resUsr2Dev;
        resUsr2Dev = AffineTransform.getScaleInstance(resScale, resScale);
        RenderContext newRC = new RenderContext(resUsr2Dev, null, hints);
        ret = getSource().createRendering(newRC);
        ret = new TileCacheRed(GraphicsUtil.wrap(ret));
        this.resScale = resScale;
        this.resRed   = new SoftReference(ret);
        return ret;
    }
    public RenderedImage createRendering(RenderContext renderContext) {
        AffineTransform usr2dev = renderContext.getTransform();
        if(usr2dev == null){
            usr2dev = new AffineTransform();
        }
        RenderingHints hints = renderContext.getRenderingHints();
        int filterResolutionX = getFilterResolutionX();
        int filterResolutionY = getFilterResolutionY();
        if ((filterResolutionX <= 0) || (filterResolutionY == 0))
            return null;
        Rectangle2D imageRect = getBounds2D();
        Rectangle   devRect;
        devRect = usr2dev.createTransformedShape(imageRect).getBounds();
        float scaleX = 1;
        if(filterResolutionX < devRect.width)
            scaleX = filterResolutionX / (float)devRect.width;
        float scaleY = 1;
        if(filterResolutionY < 0)
            scaleY = scaleX;
        else if(filterResolutionY < devRect.height)
            scaleY = filterResolutionY / (float)devRect.height;
        if ((scaleX >= 1) && (scaleY >= 1))
            return getSource().createRendering(renderContext);
        RenderedImage resRed   = getResRed(hints);
        float         resScale = getResScale();
        AffineTransform residualAT;
        residualAT = new AffineTransform(usr2dev.getScaleX()/resScale,
                                         usr2dev.getShearY()/resScale,
                                         usr2dev.getShearX()/resScale,
                                         usr2dev.getScaleY()/resScale,
                                         usr2dev.getTranslateX(),
                                         usr2dev.getTranslateY());
        return new AffineRed(GraphicsUtil.wrap(resRed), residualAT, hints);
    }
}
