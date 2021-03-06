package org.apache.batik.gvt;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.TileRable;
import org.apache.batik.ext.awt.image.renderable.TileRable8Bit;
import org.apache.batik.ext.awt.image.rendered.TileCacheRed;
public class PatternPaintContext implements PaintContext {
    private ColorModel rasterCM;
    private WritableRaster raster;
    private RenderedImage tiled;
    protected AffineTransform usr2dev;
    public AffineTransform getUsr2Dev() { return usr2dev; }
    private static Rectangle EVERYTHING = 
        new Rectangle(Integer.MIN_VALUE/4, Integer.MIN_VALUE/4, 
                      Integer.MAX_VALUE/2, Integer.MAX_VALUE/2);
    public PatternPaintContext(ColorModel      destCM,
                               AffineTransform usr2dev,
                               RenderingHints  hints,
                               Filter          tile,
                               Rectangle2D     patternRegion,
                               boolean         overflow) {
        if(usr2dev == null){
            throw new IllegalArgumentException();
        }
        if(hints == null){
            hints = new RenderingHints(null);
        }
        if(tile == null){
            throw new IllegalArgumentException();
        }
        this.usr2dev    = usr2dev;
        TileRable tileRable = new TileRable8Bit(tile,
                                                EVERYTHING,
                                                patternRegion,
                                                overflow);
        ColorSpace destCS = destCM.getColorSpace();
        if (destCS == ColorSpace.getInstance(ColorSpace.CS_sRGB))
            tileRable.setColorSpaceLinear(false);
        else if (destCS == ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB))
            tileRable.setColorSpaceLinear(true);
        RenderContext rc = new RenderContext(usr2dev,  EVERYTHING, hints);
        tiled = tileRable.createRendering(rc);
        if(tiled != null) {
            Rectangle2D devRgn = usr2dev.createTransformedShape
                (patternRegion).getBounds();
            if ((devRgn.getWidth() > 128) ||
                (devRgn.getHeight() > 128))
                tiled = new TileCacheRed(GraphicsUtil.wrap(tiled), 256, 64);
        } else {
            rasterCM = ColorModel.getRGBdefault();
            WritableRaster wr;
            wr = rasterCM.createCompatibleWritableRaster(32, 32);
            tiled = GraphicsUtil.wrap
                (new BufferedImage(rasterCM, wr, false, null));
            return;
        }
        rasterCM = tiled.getColorModel();
        if (rasterCM.hasAlpha()) {
            if (destCM.hasAlpha()) 
                rasterCM = GraphicsUtil.coerceColorModel
                    (rasterCM, destCM.isAlphaPremultiplied());
            else 
                rasterCM = GraphicsUtil.coerceColorModel(rasterCM, false);
        }
    }
    public void dispose(){
        raster = null;
    }
    public ColorModel getColorModel(){
        return rasterCM;
    }
    public Raster getRaster(int x, int y, int width, int height){
        if ((raster == null)             ||
            (raster.getWidth() < width)  ||
            (raster.getHeight() < height)) {
            raster = rasterCM.createCompatibleWritableRaster(width, height);
        }
        WritableRaster wr
            = raster.createWritableChild(0, 0, width, height, x, y, null);
        tiled.copyData(wr);
        GraphicsUtil.coerceData(wr, tiled.getColorModel(), 
                                rasterCM.isAlphaPremultiplied());
        if ((raster.getWidth()  == width) &&
            (raster.getHeight() == height))
            return raster;
        return wr.createTranslatedChild(0,0);
    }
}
