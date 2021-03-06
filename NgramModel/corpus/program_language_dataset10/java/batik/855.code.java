package org.apache.batik.ext.awt.image.renderable;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.image.rendered.TurbulencePatternRed;
public class TurbulenceRable8Bit
    extends    AbstractColorInterpolationRable
    implements TurbulenceRable {
    int     seed          = 0;     
    int     numOctaves    = 1;     
    double  baseFreqX     = 0;     
    double  baseFreqY     = 0;
    boolean stitched       = false; 
    boolean fractalNoise = false; 
    Rectangle2D region;
    public TurbulenceRable8Bit(Rectangle2D region) {
        super();
        this.region = region;
    }
    public TurbulenceRable8Bit(Rectangle2D region,
                                   int         seed,
                                   int         numOctaves,
                                   double      baseFreqX,
                                   double      baseFreqY,
                                   boolean     stitched,
                                   boolean     fractalNoise) {
        super();
        this.seed          = seed;
        this.numOctaves    = numOctaves;
        this.baseFreqX     = baseFreqX;
        this.baseFreqY     = baseFreqY;
        this.stitched      = stitched;
        this.fractalNoise  = fractalNoise;
        this.region        = region;
    }
    public Rectangle2D getTurbulenceRegion() {
        return (Rectangle2D)region.clone();
    }
    public Rectangle2D getBounds2D() {
        return (Rectangle2D)region.clone();
    }
    public int getSeed() {
        return seed;
    }
    public int getNumOctaves() {
        return numOctaves;
    }
    public double getBaseFrequencyX() {
        return baseFreqX;
    }
    public double getBaseFrequencyY() {
        return baseFreqY;
    }
    public boolean isStitched() {
        return stitched;
    }
    public boolean isFractalNoise() {
        return fractalNoise;
    }
    public void setTurbulenceRegion(Rectangle2D turbulenceRegion) {
        touch();
        this.region = turbulenceRegion;
    }
    public void setSeed(int seed) {
        touch();
        this.seed = seed;
    }
    public void setNumOctaves(int numOctaves) {
        touch();
        this.numOctaves = numOctaves;
    }
    public void setBaseFrequencyX(double baseFreqX) {
        touch();
        this.baseFreqX = baseFreqX;
    }
    public void setBaseFrequencyY(double baseFreqY) {
        touch();
        this.baseFreqY = baseFreqY;
    }
    public void setStitched(boolean stitched) {
        touch();
        this.stitched = stitched;
    }
    public void setFractalNoise(boolean fractalNoise) {
        touch();
        this.fractalNoise = fractalNoise;
    }
    public RenderedImage createRendering(RenderContext rc){
        Rectangle2D aoiRect;
        Shape aoi = rc.getAreaOfInterest();
        if(aoi == null){
            aoiRect = getBounds2D();
        } else {
            Rectangle2D rect = getBounds2D();
            aoiRect          = aoi.getBounds2D();
            if ( ! aoiRect.intersects(rect) )
                return null;
            Rectangle2D.intersect(aoiRect, rect, aoiRect);
        }
        AffineTransform usr2dev = rc.getTransform();
        final Rectangle devRect
            = usr2dev.createTransformedShape(aoiRect).getBounds();
        if ((devRect.width <= 0) ||
            (devRect.height <= 0))
            return null;
        ColorSpace cs = getOperationColorSpace();
        Rectangle2D tile = null;
        if (stitched)
            tile = (Rectangle2D)region.clone();
        AffineTransform patternTxf = new AffineTransform();
        try{
            patternTxf = usr2dev.createInverse();
        }catch(NoninvertibleTransformException e){
        }
        return new TurbulencePatternRed
            (baseFreqX, baseFreqY, numOctaves, seed, fractalNoise,
             tile, patternTxf, devRect, cs, true);
    }
}
