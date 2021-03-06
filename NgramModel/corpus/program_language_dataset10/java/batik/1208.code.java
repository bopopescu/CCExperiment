package org.apache.batik.svggen;
import java.awt.Graphics2D;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class SVGTexturePaint extends AbstractSVGConverter {
    public SVGTexturePaint(SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }
    public SVGDescriptor toSVG(GraphicContext gc) {
        return toSVG((TexturePaint)gc.getPaint());
    }
    public SVGPaintDescriptor toSVG(TexturePaint texture) {
        SVGPaintDescriptor patternDesc = (SVGPaintDescriptor)descMap.get(texture);
        Document domFactory = generatorContext.domFactory;
        if (patternDesc == null) {
            Rectangle2D anchorRect = texture.getAnchorRect();
            Element patternDef = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                            SVG_PATTERN_TAG);
            patternDef.setAttributeNS(null, SVG_PATTERN_UNITS_ATTRIBUTE,
                                      SVG_USER_SPACE_ON_USE_VALUE);
            patternDef.setAttributeNS(null, SVG_X_ATTRIBUTE,
                                    doubleString(anchorRect.getX()));
            patternDef.setAttributeNS(null, SVG_Y_ATTRIBUTE,
                                    doubleString(anchorRect.getY()));
            patternDef.setAttributeNS(null, SVG_WIDTH_ATTRIBUTE,
                                    doubleString(anchorRect.getWidth()));
            patternDef.setAttributeNS(null, SVG_HEIGHT_ATTRIBUTE,
                                    doubleString(anchorRect.getHeight()));
            BufferedImage textureImage = texture.getImage();
            if (textureImage.getWidth() > 0 &&
                textureImage.getHeight() > 0){
                if(textureImage.getWidth() != anchorRect.getWidth() ||
                   textureImage.getHeight() != anchorRect.getHeight()){
                    if(anchorRect.getWidth() > 0 &&
                       anchorRect.getHeight() > 0){
                        double scaleX =
                            anchorRect.getWidth()/textureImage.getWidth();
                        double scaleY =
                            anchorRect.getHeight()/textureImage.getHeight();
                        BufferedImage newImage
                            = new BufferedImage((int)(scaleX*
                                                      textureImage.getWidth()),
                                                (int)(scaleY*
                                                      textureImage.getHeight()),
                                                BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g = newImage.createGraphics();
                        g.scale(scaleX, scaleY);
                        g.drawImage(textureImage, 0, 0, null);
                        g.dispose();
                        textureImage = newImage;
                    }
                }
            }
            Element patternContent
                = generatorContext.genericImageHandler.createElement
                (generatorContext);
            generatorContext.genericImageHandler.handleImage
                ((RenderedImage)textureImage,
                 patternContent,
                 0,
                 0,
                 textureImage.getWidth(),
                 textureImage.getHeight(),
                 generatorContext);
            patternDef.appendChild(patternContent);
            patternDef.setAttributeNS(null, SVG_ID_ATTRIBUTE,
                                      generatorContext.idGenerator.
                                      generateID(ID_PREFIX_PATTERN));
            String patternAttrBuf = URL_PREFIX
                    + SIGN_POUND
                    + patternDef.getAttributeNS(null, SVG_ID_ATTRIBUTE)
                    + URL_SUFFIX;
            patternDesc = new SVGPaintDescriptor(patternAttrBuf, SVG_OPAQUE_VALUE, patternDef);
            descMap.put(texture, patternDesc);
            defSet.add(patternDef);
        }
        return patternDesc;
    }
}
