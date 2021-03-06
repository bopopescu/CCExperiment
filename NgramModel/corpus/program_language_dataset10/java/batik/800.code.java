package org.apache.batik.ext.awt.image.codec.util;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
public interface ImageDecoder {
    ImageDecodeParam getParam();
    void setParam(ImageDecodeParam param);
    SeekableStream getInputStream();
    int getNumPages() throws IOException;
    Raster decodeAsRaster() throws IOException;
    Raster decodeAsRaster(int page) throws IOException;
    RenderedImage decodeAsRenderedImage() throws IOException;
    RenderedImage decodeAsRenderedImage(int page) throws IOException;
}
