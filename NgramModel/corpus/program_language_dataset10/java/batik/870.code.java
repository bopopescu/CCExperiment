package org.apache.batik.ext.awt.image.rendered;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import org.apache.batik.ext.awt.ColorSpaceHintKey;
public class FilterAlphaRed extends AbstractRed {
    public FilterAlphaRed(CachableRed src) {
        super(src, src.getBounds(), 
              src.getColorModel(),
              src.getSampleModel(),
              src.getTileGridXOffset(),
              src.getTileGridYOffset(),
              null);
        props.put(ColorSpaceHintKey.PROPERTY_COLORSPACE,
                  ColorSpaceHintKey.VALUE_COLORSPACE_ALPHA);
    }
    public WritableRaster copyData(WritableRaster wr) {
        CachableRed srcRed = (CachableRed)getSources().get(0);
        SampleModel sm = srcRed.getSampleModel();
        if (sm.getNumBands() == 1)
            return srcRed.copyData(wr);
        PadRed.ZeroRecter.zeroRect(wr);
        Raster srcRas = srcRed.getData(wr.getBounds());
        AbstractRed.copyBand(srcRas, srcRas.getNumBands()-1, wr, 
                             wr.getNumBands()-1);
        return wr;
    }
}    
