package org.apache.batik.ext.awt.image.renderable;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.util.List;
import org.apache.batik.ext.awt.image.ARGBChannel;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.rendered.AffineRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.DisplacementMapRed;
public class DisplacementMapRable8Bit
    extends    AbstractColorInterpolationRable
    implements DisplacementMapRable {
    private double scale;
    private ARGBChannel xChannelSelector;
    private ARGBChannel yChannelSelector;
    public DisplacementMapRable8Bit(List sources,
                                    double scale,
                                    ARGBChannel xChannelSelector,
                                    ARGBChannel yChannelSelector){
        setSources(sources);
        setScale(scale);
        setXChannelSelector(xChannelSelector);
        setYChannelSelector(yChannelSelector);
    }
    public Rectangle2D getBounds2D(){
        return ((Filter)(getSources().get(0))).getBounds2D();
    }
    public void setScale(double scale){
        touch();
        this.scale = scale;
    }
    public double getScale(){
        return scale;
    }
    public void setSources(List sources){
        if(sources.size() != 2){
            throw new IllegalArgumentException();
        }
        init(sources, null);
    }
    public void setXChannelSelector(ARGBChannel xChannelSelector){
        if(xChannelSelector == null){
            throw new IllegalArgumentException();
        }
        touch();
        this.xChannelSelector = xChannelSelector;
    }
    public ARGBChannel getXChannelSelector(){
        return xChannelSelector;
    }
    public void setYChannelSelector(ARGBChannel yChannelSelector){
        if(yChannelSelector == null){
            throw new IllegalArgumentException();
        }
        touch();
        this.yChannelSelector = yChannelSelector;
    }
    public ARGBChannel getYChannelSelector(){
        return yChannelSelector;
    }
    public RenderedImage createRendering(RenderContext rc) {
        Filter displaced = (Filter)getSources().get(0);
        Filter map = (Filter)getSources().get(1);
        RenderingHints rh = rc.getRenderingHints();
        if (rh == null) rh = new RenderingHints(null);
        AffineTransform at = rc.getTransform();
        double sx = at.getScaleX();
        double sy = at.getScaleY();
        double shx = at.getShearX();
        double shy = at.getShearY();
        double tx = at.getTranslateX();
        double ty = at.getTranslateY();
        double atScaleX = Math.sqrt(sx*sx + shy*shy);
        double atScaleY = Math.sqrt(sy*sy + shx*shx);
        float scaleX = (float)(scale*atScaleX);
        float scaleY = (float)(scale*atScaleY);
        if ((scaleX == 0) && (scaleY == 0))
            return displaced.createRendering(rc);
        AffineTransform srcAt
            = AffineTransform.getScaleInstance(atScaleX, atScaleY);
        Shape origAOI = rc.getAreaOfInterest();
        if (origAOI == null)
            origAOI = getBounds2D();
        Rectangle2D aoiR = origAOI.getBounds2D();
        RenderContext srcRc = new RenderContext(srcAt, aoiR, rh);
        RenderedImage mapRed = map.createRendering(srcRc);
        if (mapRed == null) return null;
        aoiR = new Rectangle2D.Double(aoiR.getX()      - scale/2,
                                      aoiR.getY()      - scale/2,
                                      aoiR.getWidth()  + scale,
                                      aoiR.getHeight() + scale);
        Rectangle2D displacedRect = displaced.getBounds2D();
        if ( ! aoiR.intersects(displacedRect) )
            return null;
        aoiR = aoiR.createIntersection(displacedRect);
        srcRc = new RenderContext(srcAt, aoiR, rh);
        RenderedImage displacedRed = displaced.createRendering(srcRc);
        if (displacedRed == null) return null;
        mapRed = convertSourceCS(mapRed);
        CachableRed cr = new DisplacementMapRed
            (GraphicsUtil.wrap(displacedRed),
             GraphicsUtil.wrap(mapRed),
             xChannelSelector, yChannelSelector,
             scaleX, scaleY, rh);
        AffineTransform resAt
            = new AffineTransform(sx/atScaleX, shy/atScaleX,
                                  shx/atScaleY,  sy/atScaleY,
                                  tx, ty);
        if(!resAt.isIdentity())
            cr = new AffineRed(cr, resAt, rh);
        return cr;
    }
    public Shape getDependencyRegion(int srcIndex, Rectangle2D outputRgn){
        return super.getDependencyRegion(srcIndex, outputRgn);
    }
    public Shape getDirtyRegion(int srcIndex, Rectangle2D inputRgn){
        return super.getDirtyRegion(srcIndex, inputRgn);
    }
}
