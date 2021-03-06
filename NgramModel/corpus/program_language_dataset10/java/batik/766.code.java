package org.apache.batik.ext.awt.image.codec.imageio;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.renderable.DeferRable;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.RedRable;
import org.apache.batik.ext.awt.image.rendered.Any2sRGBRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.FormatRed;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.apache.batik.ext.awt.image.spi.MagicNumberRegistryEntry;
import org.apache.batik.util.ParsedURL;
public abstract class AbstractImageIORegistryEntry
    extends MagicNumberRegistryEntry {
    public AbstractImageIORegistryEntry(String    name,
                                        String [] exts,
                                        String [] mimeTypes,
                                        MagicNumber [] magicNumbers) {
        super(name, PRIORITY + 100, exts, mimeTypes, magicNumbers);
    }
    public AbstractImageIORegistryEntry(String name,
                                    String ext,
                                    String mimeType,
                                    int offset, byte[] magicNumber) {
        super(name, PRIORITY + 100, ext, mimeType, offset, magicNumber);
    }
    public Filter handleStream(InputStream inIS,
                               ParsedURL   origURL,
                               boolean     needRawData) {
        final DeferRable  dr  = new DeferRable();
        final InputStream is  = inIS;
        final String      errCode;
        final Object []   errParam;
        if (origURL != null) {
            errCode  = ERR_URL_FORMAT_UNREADABLE;
            errParam = new Object[] {getFormatName(), origURL};
        } else {
            errCode  = ERR_STREAM_FORMAT_UNREADABLE;
            errParam = new Object[] {getFormatName()};
        }
        Thread t = new Thread() {
                @Override
                public void run() {
                    Filter filt;
                    try{
                        Iterator<ImageReader> iter = ImageIO.getImageReadersByMIMEType(
                                getMimeTypes().get(0).toString());
                        if (!iter.hasNext()) {
                            throw new UnsupportedOperationException(
                                    "No image reader for "
                                        + getFormatName() + " available!");
                        }
                        ImageReader reader = iter.next();
                        ImageInputStream imageIn = ImageIO.createImageInputStream(is);
                        reader.setInput(imageIn, true);
                        int imageIndex = 0;
                        dr.setBounds(new Rectangle2D.Double
                                     (0, 0,
                                      reader.getWidth(imageIndex),
                                      reader.getHeight(imageIndex)));
                        CachableRed cr;
                        BufferedImage bi = reader.read(imageIndex);
                        cr = GraphicsUtil.wrap(bi);
                        cr = new Any2sRGBRed(cr);
                        cr = new FormatRed(cr, GraphicsUtil.sRGB_Unpre);
                        WritableRaster wr = (WritableRaster)cr.getData();
                        ColorModel cm = cr.getColorModel();
                        BufferedImage image = new BufferedImage
                            (cm, wr, cm.isAlphaPremultiplied(), null);
                        cr = GraphicsUtil.wrap(image);
                        filt = new RedRable(cr);
                    } catch (IOException ioe) {
                        filt = ImageTagRegistry.getBrokenLinkImage
                            (AbstractImageIORegistryEntry.this,
                             errCode, errParam);
                    } catch (ThreadDeath td) {
                        filt = ImageTagRegistry.getBrokenLinkImage
                            (AbstractImageIORegistryEntry.this,
                             errCode, errParam);
                        dr.setSource(filt);
                        throw td;
                    } catch (Throwable t) {
                        filt = ImageTagRegistry.getBrokenLinkImage
                            (AbstractImageIORegistryEntry.this,
                             errCode, errParam);
                    }
                    dr.setSource(filt);
                }
            };
        t.start();
        return dr;
    }
}
