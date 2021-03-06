package org.apache.batik.ext.awt.image.rendered;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import org.apache.batik.ext.awt.color.ICCColorSpaceExt;
public class ProfileRed extends AbstractRed {
    private static final ColorSpace sRGBCS
        = ColorSpace.getInstance(ColorSpace.CS_sRGB);
    private static final ColorModel sRGBCM
        = new DirectColorModel(sRGBCS,
                               32,
                               0x00ff0000,
                               0x0000ff00,
                               0x000000ff,
                               0xff000000,
                               false,
                               DataBuffer.TYPE_INT);
    private ICCColorSpaceExt colorSpace;
    public ProfileRed(CachableRed src,
                      ICCColorSpaceExt colorSpace){
        this.colorSpace = colorSpace;
        init(src, src.getBounds(),
             sRGBCM, sRGBCM.createCompatibleSampleModel(src.getWidth(),
                                                        src.getHeight()),
             src.getTileGridXOffset(), src.getTileGridYOffset(), null);
    }
    public CachableRed getSource() {
        return (CachableRed)getSources().get(0);
    }
    public WritableRaster copyData(WritableRaster argbWR){
        try{
            RenderedImage img = getSource();
            ColorModel imgCM = img.getColorModel();
            ColorSpace imgCS = imgCM.getColorSpace();
            int nImageComponents = imgCS.getNumComponents();
            int nProfileComponents = colorSpace.getNumComponents();
            if(nImageComponents != nProfileComponents){
                System.err.println("Input image and associated color profile have" +
                                   " mismatching number of color components: conversion is not possible");
                return argbWR;
            }
            int w = argbWR.getWidth();
            int h = argbWR.getHeight();
            int minX = argbWR.getMinX();
            int minY = argbWR.getMinY();
            WritableRaster srcWR =
                imgCM.createCompatibleWritableRaster(w, h);
            srcWR = srcWR.createWritableTranslatedChild(minX, minY);
            img.copyData(srcWR);
            if(!(imgCM instanceof ComponentColorModel) ||
               !(img.getSampleModel() instanceof BandedSampleModel) ||
               (imgCM.hasAlpha() && imgCM.isAlphaPremultiplied() )) {
                ComponentColorModel imgCompCM
                    = new ComponentColorModel
                        (imgCS,                      
                         imgCM.getComponentSize(),   
                         imgCM.hasAlpha(),             
                         false, 
                         imgCM.getTransparency(),      
                         DataBuffer.TYPE_BYTE);        
                WritableRaster wr = Raster.createBandedRaster
                    (DataBuffer.TYPE_BYTE,
                     argbWR.getWidth(), argbWR.getHeight(),
                     imgCompCM.getNumComponents(),
                     new Point(0, 0));
                BufferedImage imgComp = new BufferedImage
                    (imgCompCM, wr, imgCompCM.isAlphaPremultiplied(), null);
                BufferedImage srcImg = new BufferedImage
                    (imgCM, srcWR.createWritableTranslatedChild(0, 0),
                     imgCM.isAlphaPremultiplied(), null);
                Graphics2D g = imgComp.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                                   RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                g.drawImage(srcImg, 0, 0, null);
                img = imgComp;
                imgCM = imgCompCM;
                srcWR = wr.createWritableTranslatedChild(minX, minY);
            }
            ComponentColorModel newCM = new ComponentColorModel
                (colorSpace,                    
                 imgCM.getComponentSize(),      
                 false,                         
                 false,                         
                 Transparency.OPAQUE,           
                 DataBuffer.TYPE_BYTE);         
            DataBufferByte data = (DataBufferByte)srcWR.getDataBuffer();
            srcWR = Raster.createBandedRaster
                (data, argbWR.getWidth(), argbWR.getHeight(),
                 argbWR.getWidth(), new int[]{0, 1, 2},
                 new int[]{0, 0, 0}, new Point(0, 0));
            BufferedImage newImg = new BufferedImage
                (newCM, srcWR, newCM.isAlphaPremultiplied(), null);
            ComponentColorModel sRGBCompCM = new ComponentColorModel
                (ColorSpace.getInstance(ColorSpace.CS_sRGB),
                 new int[]{8, 8, 8},
                 false,
                 false,
                 Transparency.OPAQUE,
                 DataBuffer.TYPE_BYTE);
            WritableRaster wr = Raster.createBandedRaster
                (DataBuffer.TYPE_BYTE, argbWR.getWidth(), argbWR.getHeight(),
                 sRGBCompCM.getNumComponents(), new Point(0, 0));
            BufferedImage sRGBImage = new BufferedImage
                (sRGBCompCM, wr, false, null);
            ColorConvertOp colorConvertOp = new ColorConvertOp(null);
            colorConvertOp.filter(newImg, sRGBImage);
            if (imgCM.hasAlpha()){
                DataBufferByte rgbData = (DataBufferByte)wr.getDataBuffer();
                byte[][] imgBanks = data.getBankData();
                byte[][] rgbBanks = rgbData.getBankData();
                byte[][] argbBanks = {rgbBanks[0], rgbBanks[1],
                                      rgbBanks[2], imgBanks[3]};
                DataBufferByte argbData = new DataBufferByte(argbBanks, imgBanks[0].length);
                srcWR = Raster.createBandedRaster
                    (argbData, argbWR.getWidth(), argbWR.getHeight(),
                     argbWR.getWidth(), new int[]{0, 1, 2, 3},
                     new int[]{0, 0, 0, 0}, new Point(0, 0));
                sRGBCompCM = new ComponentColorModel
                    (ColorSpace.getInstance(ColorSpace.CS_sRGB),
                     new int[]{8, 8, 8, 8},
                     true,
                     false,
                     Transparency.TRANSLUCENT,
                     DataBuffer.TYPE_BYTE);
                sRGBImage = new BufferedImage(sRGBCompCM,
                                              srcWR,
                                              false,
                                              null);
            }
            BufferedImage result = new BufferedImage(sRGBCM,
                                                     argbWR.createWritableTranslatedChild(0, 0),
                                                     false,
                                                     null);
            Graphics2D g = result.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                               RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g.drawImage(sRGBImage, 0, 0, null);
            g.dispose();
            return argbWR;
        }catch(Exception e){
            e.printStackTrace();
            throw new Error( e.getMessage() );
        }
    }
}
