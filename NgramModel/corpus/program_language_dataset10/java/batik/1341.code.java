package org.apache.batik.transcoder.image;
import java.awt.image.BufferedImage;
import java.awt.image.SinglePixelPackedSampleModel;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.keys.StringKey;
public class TIFFTranscoder extends ImageTranscoder {
    public TIFFTranscoder() { 
        hints.put(KEY_FORCE_TRANSPARENT_WHITE, Boolean.FALSE);
    }
    public UserAgent getUserAgent() {
        return this.userAgent;
    }
    public BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }
    private WriteAdapter getWriteAdapter(String className) {
        WriteAdapter adapter;
        try {
            Class clazz = Class.forName(className);
            adapter = (WriteAdapter)clazz.newInstance();
            return adapter;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }
    public void writeImage(BufferedImage img, TranscoderOutput output)
            throws TranscoderException {
        boolean forceTransparentWhite = false;
        if (hints.containsKey(PNGTranscoder.KEY_FORCE_TRANSPARENT_WHITE)) {
            forceTransparentWhite =
                ((Boolean)hints.get
                 (PNGTranscoder.KEY_FORCE_TRANSPARENT_WHITE)).booleanValue();
        }
        if (forceTransparentWhite) {
            SinglePixelPackedSampleModel sppsm;
            sppsm = (SinglePixelPackedSampleModel)img.getSampleModel();
            forceTransparentWhite(img, sppsm);
        }
        WriteAdapter adapter = getWriteAdapter(
                "org.apache.batik.ext.awt.image.codec.tiff.TIFFTranscoderInternalCodecWriteAdapter");
        if (adapter == null) {
            adapter = getWriteAdapter(
                "org.apache.batik.transcoder.image.TIFFTranscoderImageIOWriteAdapter");
        }
        if (adapter == null) {
            throw new TranscoderException(
                    "Could not write TIFF file because no WriteAdapter is availble");
        }
        adapter.writeImage(this, img, output);
    }
    public interface WriteAdapter {
        void writeImage(TIFFTranscoder transcoder, BufferedImage img, 
                TranscoderOutput output) throws TranscoderException;
    }
    public static final TranscodingHints.Key KEY_FORCE_TRANSPARENT_WHITE
        = ImageTranscoder.KEY_FORCE_TRANSPARENT_WHITE;
    public static final TranscodingHints.Key KEY_COMPRESSION_METHOD
        = new StringKey();
}
