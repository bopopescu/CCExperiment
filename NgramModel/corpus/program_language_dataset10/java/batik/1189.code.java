package org.apache.batik.svggen;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.AttributedCharacterIterator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.batik.ext.awt.g2d.AbstractGraphics2D;
import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
public class SVGGraphics2D extends AbstractGraphics2D
    implements Cloneable, SVGSyntax, ErrorConstants {
    public static final String DEFAULT_XML_ENCODING = "ISO-8859-1";
    public static final int DEFAULT_MAX_GC_OVERRIDES = 3;
    protected DOMTreeManager domTreeManager;
    protected DOMGroupManager domGroupManager;
    protected SVGGeneratorContext generatorCtx;
    protected SVGShape shapeConverter;
    protected Dimension svgCanvasSize;
    protected Graphics2D fmg;
    {
        BufferedImage bi
            = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        fmg = bi.createGraphics();
    }
    public final Dimension getSVGCanvasSize(){
        return svgCanvasSize;
    }
    public final void setSVGCanvasSize(Dimension svgCanvasSize) {
        this.svgCanvasSize = new Dimension(svgCanvasSize);
    }
    public final SVGGeneratorContext getGeneratorContext() {
        return generatorCtx;
    }
    public final SVGShape getShapeConverter() {
        return shapeConverter;
    }
    public final DOMTreeManager getDOMTreeManager(){
        return domTreeManager;
    }
     protected final void setDOMTreeManager(DOMTreeManager treeMgr) {
        this.domTreeManager = treeMgr;
        generatorCtx.genericImageHandler.setDOMTreeManager(domTreeManager);
    }
    protected final DOMGroupManager getDOMGroupManager(){
        return domGroupManager;
    }
     protected final void setDOMGroupManager(DOMGroupManager groupMgr) {
        this.domGroupManager = groupMgr;
    }
    public final Document getDOMFactory(){
        return generatorCtx.domFactory;
    }
    public final ImageHandler getImageHandler(){
        return generatorCtx.imageHandler;
    }
    public final GenericImageHandler getGenericImageHandler(){
        return generatorCtx.genericImageHandler;
    }
    public final ExtensionHandler getExtensionHandler(){
        return generatorCtx.extensionHandler;
    }
    public final void setExtensionHandler(ExtensionHandler extensionHandler) {
        generatorCtx.setExtensionHandler(extensionHandler);
    }
    public SVGGraphics2D(Document domFactory) {
        this(SVGGeneratorContext.createDefault(domFactory), false);
    }
    public SVGGraphics2D(Document domFactory,
                         ImageHandler imageHandler,
                         ExtensionHandler extensionHandler,
                         boolean textAsShapes) {
        this(buildSVGGeneratorContext(domFactory,
                                      imageHandler,
                                      extensionHandler),
             textAsShapes);
    }
    public static SVGGeneratorContext
        buildSVGGeneratorContext(Document domFactory,
                                 ImageHandler imageHandler,
                                 ExtensionHandler extensionHandler){
        SVGGeneratorContext generatorCtx = new SVGGeneratorContext(domFactory);
        generatorCtx.setIDGenerator(new SVGIDGenerator());
        generatorCtx.setExtensionHandler(extensionHandler);
        generatorCtx.setImageHandler(imageHandler);
        generatorCtx.setStyleHandler(new DefaultStyleHandler());
        generatorCtx.setComment("Generated by the Batik Graphics2D SVG Generator");
        generatorCtx.setErrorHandler(new DefaultErrorHandler());
        return generatorCtx;
    }
    public SVGGraphics2D(SVGGeneratorContext generatorCtx,
                         boolean textAsShapes) {
        super(textAsShapes);
        if (generatorCtx == null)
            throw new SVGGraphics2DRuntimeException(ERR_CONTEXT_NULL);
        setGeneratorContext(generatorCtx);
    }
    protected void setGeneratorContext(SVGGeneratorContext generatorCtx) {
        this.generatorCtx = generatorCtx;
        this.gc = new GraphicContext(new AffineTransform());
        SVGGeneratorContext.GraphicContextDefaults gcDefaults =
            generatorCtx.getGraphicContextDefaults();
        if(gcDefaults != null){
            if(gcDefaults.getPaint() != null){
                gc.setPaint(gcDefaults.getPaint());
            }
            if(gcDefaults.getStroke() != null){
                gc.setStroke(gcDefaults.getStroke());
            }
            if(gcDefaults.getComposite() != null){
                gc.setComposite(gcDefaults.getComposite());
            }
            if(gcDefaults.getClip() != null){
                gc.setClip(gcDefaults.getClip());
            }
            if(gcDefaults.getRenderingHints() != null){
                gc.setRenderingHints(gcDefaults.getRenderingHints());
            }
            if(gcDefaults.getFont() != null){
                gc.setFont(gcDefaults.getFont());
            }
            if(gcDefaults.getBackground() != null){
                gc.setBackground(gcDefaults.getBackground());
            }
        }
        this.shapeConverter = new SVGShape(generatorCtx);
        this.domTreeManager = new DOMTreeManager(gc,
                                                 generatorCtx,
                                                 DEFAULT_MAX_GC_OVERRIDES);
        this.domGroupManager = new DOMGroupManager(gc, domTreeManager);
        this.domTreeManager.addGroupManager(domGroupManager);
        generatorCtx.genericImageHandler.setDOMTreeManager(domTreeManager);
    }
    public SVGGraphics2D(SVGGraphics2D g) {
        super(g);
        this.generatorCtx = g.generatorCtx;
        this.gc.validateTransformStack();
        this.shapeConverter = g.shapeConverter;
        this.domTreeManager = g.domTreeManager;
        this.domGroupManager = new DOMGroupManager(this.gc, this.domTreeManager);
        this.domTreeManager.addGroupManager(this.domGroupManager);
    }
    public void stream(String svgFileName) throws SVGGraphics2DIOException {
        stream(svgFileName, false);
    }
    public void stream(String svgFileName, boolean useCss)
        throws SVGGraphics2DIOException {
        try {
            OutputStreamWriter writer =
                new OutputStreamWriter(new FileOutputStream(svgFileName),
                                       DEFAULT_XML_ENCODING);
            stream(writer, useCss);
            writer.flush();
            writer.close();
        } catch (SVGGraphics2DIOException io) {
            throw io;
        } catch (IOException e) {
            generatorCtx.errorHandler.
                handleError(new SVGGraphics2DIOException(e));
        }
    }
    public void stream(Writer writer) throws SVGGraphics2DIOException {
        stream(writer, false);
    }
    public void stream(Writer writer, boolean useCss, boolean escaped)
        throws SVGGraphics2DIOException {
        Element svgRoot = getRoot();
        stream(svgRoot, writer, useCss, escaped);
    }
    public void stream(Writer writer, boolean useCss)
        throws SVGGraphics2DIOException {
        Element svgRoot = getRoot();
        stream(svgRoot, writer, useCss, false);
    }
    public void stream(Element svgRoot, Writer writer)
        throws SVGGraphics2DIOException {
        stream(svgRoot, writer, false, false);
    }
    public void stream(Element svgRoot, Writer writer, boolean useCss, boolean escaped)
        throws SVGGraphics2DIOException {
        Node rootParent = svgRoot.getParentNode();
        Node nextSibling = svgRoot.getNextSibling();
        try {
            svgRoot.setAttributeNS(XMLNS_NAMESPACE_URI,
                                   XMLNS_PREFIX,
                                   SVG_NAMESPACE_URI);
            svgRoot.setAttributeNS(XMLNS_NAMESPACE_URI,
                                   XMLNS_PREFIX + ":" + XLINK_PREFIX,
                                   XLINK_NAMESPACE_URI);
            DocumentFragment svgDocument =
                svgRoot.getOwnerDocument().createDocumentFragment();
            svgDocument.appendChild(svgRoot);
            if (useCss)
                SVGCSSStyler.style(svgDocument);
            XmlWriter.writeXml(svgDocument, writer, escaped);
            writer.flush();
        } catch (SVGGraphics2DIOException e) {
            generatorCtx.errorHandler.
                handleError(e);
        } catch (IOException io) {
            generatorCtx.errorHandler.
                handleError(new SVGGraphics2DIOException(io));
        } finally {
            if (rootParent != null) {
                if (nextSibling == null) {
                    rootParent.appendChild(svgRoot);
                } else {
                    rootParent.insertBefore(svgRoot, nextSibling);
                }
            }
        }
    }
    public java.util.List getDefinitionSet(){
        return domTreeManager.getDefinitionSet();
    }
    public Element getTopLevelGroup(){
        return getTopLevelGroup(true);
    }
    public Element getTopLevelGroup(boolean includeDefinitionSet){
        return domTreeManager.getTopLevelGroup(includeDefinitionSet);
    }
    public void setTopLevelGroup(Element topLevelGroup){
        domTreeManager.setTopLevelGroup(topLevelGroup);
    }
    public Element getRoot(){
        return getRoot(null);
    }
    public Element getRoot(Element svgRoot) {
        svgRoot = domTreeManager.getRoot(svgRoot);
        if (svgCanvasSize != null){
            svgRoot.setAttributeNS(null, SVG_WIDTH_ATTRIBUTE,  String.valueOf( svgCanvasSize.width ) );
            svgRoot.setAttributeNS(null, SVG_HEIGHT_ATTRIBUTE, String.valueOf( svgCanvasSize.height) );
        }
        return svgRoot;
    }
    public Graphics create(){
        return new SVGGraphics2D(this);
    }
    public void setXORMode(Color c1) {
        generatorCtx.errorHandler.
            handleError(new SVGGraphics2DRuntimeException(ERR_XOR));
    }
    public FontMetrics getFontMetrics(Font f){
        return fmg.getFontMetrics(f);
    }
    public void copyArea(int x, int y, int width, int height,
                         int dx, int dy){
    }
    public boolean drawImage(Image img, int x, int y,
                             ImageObserver observer) {
        Element imageElement =
            getGenericImageHandler().createElement(getGeneratorContext());
        AffineTransform xform = getGenericImageHandler().handleImage(
                                                         img, imageElement,
                                                         x, y,
                                                         img.getWidth(null),
                                                         img.getHeight(null),
                                                         getGeneratorContext());
        if (xform == null) {
            domGroupManager.addElement(imageElement);
        } else {
            AffineTransform inverseTransform = null;
            try {
                inverseTransform = xform.createInverse();
            } catch(NoninvertibleTransformException e) {
                throw new SVGGraphics2DRuntimeException(ERR_UNEXPECTED);
            }
            gc.transform(xform);
            domGroupManager.addElement(imageElement);
            gc.transform(inverseTransform);
        }
        return true;
    }
    public boolean drawImage(Image img, int x, int y,
                             int width, int height,
                             ImageObserver observer){
        Element imageElement =
            getGenericImageHandler().createElement(getGeneratorContext());
        AffineTransform xform
            = getGenericImageHandler().handleImage(
                                       img, imageElement,
                                       x, y,
                                       width, height,
                                       getGeneratorContext());
        if (xform == null) {
            domGroupManager.addElement(imageElement);
        } else {
            AffineTransform inverseTransform = null;
            try {
                inverseTransform = xform.createInverse();
            } catch(NoninvertibleTransformException e) {
                throw new SVGGraphics2DRuntimeException(ERR_UNEXPECTED);
            }
            gc.transform(xform);
            domGroupManager.addElement(imageElement);
            gc.transform(inverseTransform);
        }
        return true;
    }
    public void dispose() {
        this.domTreeManager.removeGroupManager(this.domGroupManager);
    }
    public void draw(Shape s) {
        Stroke stroke = gc.getStroke();
        if (stroke instanceof BasicStroke) {
            Element svgShape = shapeConverter.toSVG(s);
            if (svgShape != null) {
                domGroupManager.addElement(svgShape, DOMGroupManager.DRAW);
            }
        } else {
            Shape strokedShape = stroke.createStrokedShape(s);
            fill(strokedShape);
        }
    }
    public boolean drawImage(Image img,
                             AffineTransform xform,
                             ImageObserver obs){
        boolean retVal = true;
        if (xform == null) {
            retVal = drawImage(img, 0, 0, null);
        } else if(xform.getDeterminant() != 0){
            AffineTransform inverseTransform = null;
            try{
                inverseTransform = xform.createInverse();
            }   catch(NoninvertibleTransformException e){
                throw new SVGGraphics2DRuntimeException(ERR_UNEXPECTED);
            }
            gc.transform(xform);
            retVal = drawImage(img, 0, 0, null);
            gc.transform(inverseTransform);
        } else {
            AffineTransform savTransform = new AffineTransform(gc.getTransform());
            gc.transform(xform);
            retVal = drawImage(img, 0, 0, null);
            gc.setTransform(savTransform);
        }
        return retVal;
    }
    public void drawImage(BufferedImage img,
                          BufferedImageOp op,
                          int x,
                          int y){
        img = op.filter(img, null);
        drawImage(img, x, y, null);
    }
    public void drawRenderedImage(RenderedImage img,
                                  AffineTransform trans2) {
        Element image =
            getGenericImageHandler().createElement(getGeneratorContext());
        AffineTransform trans1
            = getGenericImageHandler().handleImage(
                                       img, image,
                                       img.getMinX(),
                                       img.getMinY(),
                                       img.getWidth(),
                                       img.getHeight(),
                                       getGeneratorContext());
        AffineTransform xform;
        if (trans2 == null) {
            xform = trans1;
        } else {
            if(trans1 == null) {
                xform = trans2;
             } else {
                xform = new AffineTransform(trans2);
                xform.concatenate(trans1);
            }
        }
        if(xform == null) {
            domGroupManager.addElement(image);
        } else if(xform.getDeterminant() != 0){
            AffineTransform inverseTransform = null;
            try{
                inverseTransform = xform.createInverse();
            }catch(NoninvertibleTransformException e){
                throw new SVGGraphics2DRuntimeException(ERR_UNEXPECTED);
            }
            gc.transform(xform);
            domGroupManager.addElement(image);
            gc.transform(inverseTransform);
        } else {
            AffineTransform savTransform = new AffineTransform(gc.getTransform());
            gc.transform(xform);
            domGroupManager.addElement(image);
            gc.setTransform(savTransform);
        }
    }
    public void drawRenderableImage(RenderableImage img,
                                    AffineTransform trans2){
        Element image =
            getGenericImageHandler().createElement(getGeneratorContext());
        AffineTransform trans1 =
            getGenericImageHandler().handleImage(
                                     img, image,
                                     img.getMinX(),
                                     img.getMinY(),
                                     img.getWidth(),
                                     img.getHeight(),
                                     getGeneratorContext());
        AffineTransform xform;
        if (trans2 == null) {
            xform = trans1;
        } else {
            if(trans1 == null) {
                xform = trans2;
             } else {
                xform = new AffineTransform(trans2);
                xform.concatenate(trans1);
            }
        }
        if (xform == null) {
            domGroupManager.addElement(image);
        } else if(xform.getDeterminant() != 0){
            AffineTransform inverseTransform = null;
            try{
                inverseTransform = xform.createInverse();
            }catch(NoninvertibleTransformException e){
                throw new SVGGraphics2DRuntimeException(ERR_UNEXPECTED);
            }
            gc.transform(xform);
            domGroupManager.addElement(image);
            gc.transform(inverseTransform);
        } else {
            AffineTransform savTransform = new AffineTransform(gc.getTransform());
            gc.transform(xform);
            domGroupManager.addElement(image);
            gc.setTransform(savTransform);
        }
    }
    public void drawString(String s, float x, float y) {
        if (textAsShapes)  {
            GlyphVector gv = getFont().
                createGlyphVector(getFontRenderContext(), s);
            drawGlyphVector(gv, x, y);
            return;
        }
        if (generatorCtx.svgFont) {
            domTreeManager.gcConverter.
                getFontConverter().recordFontUsage(s, getFont());
        }
        AffineTransform savTxf = getTransform();
        AffineTransform txtTxf = transformText(x, y);
        Element text =
            getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_TEXT_TAG);
        text.setAttributeNS(null, SVG_X_ATTRIBUTE, generatorCtx.doubleString(x));
        text.setAttributeNS(null, SVG_Y_ATTRIBUTE, generatorCtx.doubleString(y));
        text.setAttributeNS(XML_NAMESPACE_URI,
                            XML_SPACE_QNAME,
                            XML_PRESERVE_VALUE);
        text.appendChild(getDOMFactory().createTextNode(s));
        domGroupManager.addElement(text, DOMGroupManager.FILL);
        if (txtTxf != null){
            this.setTransform(savTxf);
        }
    }
    private AffineTransform transformText(float x, float y) {
        AffineTransform txtTxf = null;
        Font font = getFont();
        if (font != null){
            txtTxf = font.getTransform();
            if (txtTxf != null && !txtTxf.isIdentity()){
                AffineTransform t = new AffineTransform();
                t.translate(x, y);
                t.concatenate(txtTxf);
                t.translate(-x, -y);
                this.transform(t);
            } else {
                txtTxf = null;
            }
        }
        return txtTxf;
    }
    public void drawString(AttributedCharacterIterator ati, float x, float y) {
        if ((textAsShapes) || (usesUnsupportedAttributes(ati))) {
            TextLayout layout = new TextLayout(ati, getFontRenderContext());
            layout.draw(this, x, y);
            return;
        }
        boolean multiSpans = false;
        if (ati.getRunLimit() < ati.getEndIndex()) multiSpans = true;
        Element text = getDOMFactory().createElementNS(SVG_NAMESPACE_URI,
                                                       SVG_TEXT_TAG);
        text.setAttributeNS(null, SVG_X_ATTRIBUTE,
                            generatorCtx.doubleString(x));
        text.setAttributeNS(null, SVG_Y_ATTRIBUTE,
                            generatorCtx.doubleString(y));
        text.setAttributeNS(XML_NAMESPACE_URI, XML_SPACE_QNAME,
                            XML_PRESERVE_VALUE);
        Font  baseFont  = getFont();
        Paint basePaint = getPaint();
        char ch = ati.first();
        setTextElementFill   (ati);
        setTextFontAttributes(ati, baseFont);
        SVGGraphicContext textGC;
        textGC = domTreeManager.getGraphicContextConverter().toSVG(gc);
        domGroupManager.addElement(text, DOMGroupManager.FILL);
        textGC.getContext().put(SVG_STROKE_ATTRIBUTE, SVG_NONE_VALUE);
        textGC.getGroupContext().put(SVG_STROKE_ATTRIBUTE, SVG_NONE_VALUE);
        boolean firstSpan = true;
        AffineTransform savTxf = getTransform();
        AffineTransform txtTxf = null;
        while (ch != AttributedCharacterIterator.DONE) {
            Element tspan = text;
            if (multiSpans) {
                tspan = getDOMFactory().createElementNS
                    (SVG_NAMESPACE_URI, SVG_TSPAN_TAG);
                text.appendChild(tspan);
            }
            setTextElementFill(ati);
            boolean resetTransform = setTextFontAttributes(ati, baseFont);
            if (resetTransform || firstSpan) {
                txtTxf = transformText(x, y);
                firstSpan = false;
            }
            int start = ati.getIndex();
            int end   = ati.getRunLimit()-1;
            StringBuffer buf = new StringBuffer( end - start );
            buf.append(ch);
            for (int i=start; i<end; i++) {
                ch = ati.next();
                buf.append(ch);
            }
            String s = buf.toString();
            if (generatorCtx.isEmbeddedFontsOn()) {
                getDOMTreeManager().getGraphicContextConverter().
                    getFontConverter().recordFontUsage(s, getFont());
            }
            SVGGraphicContext elementGC;
            elementGC = domTreeManager.gcConverter.toSVG(gc);
            elementGC.getGroupContext().put(SVG_STROKE_ATTRIBUTE,
                                            SVG_NONE_VALUE);
            SVGGraphicContext deltaGC;
            deltaGC = DOMGroupManager.processDeltaGC(elementGC, textGC);
            setTextElementAttributes(deltaGC, ati);
            domTreeManager.getStyleHandler().
                setStyle(tspan, deltaGC.getContext(),
                         domTreeManager.getGeneratorContext());
            tspan.appendChild(getDOMFactory().createTextNode(s));
            if ((resetTransform || firstSpan) && (txtTxf != null)) {
                this.setTransform(savTxf);
            }
            ch = ati.next();  
        }
        setFont(baseFont);
        setPaint(basePaint);
    }
    public void fill(Shape s) {
        Element svgShape = shapeConverter.toSVG(s);
        if (svgShape != null) {
            domGroupManager.addElement(svgShape, DOMGroupManager.FILL);
        }
    }
    private boolean setTextFontAttributes(AttributedCharacterIterator ati,
                                          Font baseFont) {
        boolean resetTransform = false;
        if ((ati.getAttribute(TextAttribute.FONT) != null) ||
            (ati.getAttribute(TextAttribute.FAMILY) != null) ||
            (ati.getAttribute(TextAttribute.WEIGHT) != null) ||
            (ati.getAttribute(TextAttribute.POSTURE) != null) ||
            (ati.getAttribute(TextAttribute.SIZE) != null)) {
            Map m = ati.getAttributes();
            Font f = baseFont.deriveFont(m);
            setFont(f);
            resetTransform = true;
        }
        return resetTransform;
    }
    private void setTextElementFill(AttributedCharacterIterator ati) {
        if (ati.getAttribute(TextAttribute.FOREGROUND) != null) {
            Color color = (Color)ati.getAttribute(TextAttribute.FOREGROUND);
            setPaint(color);
        }
    }
    private void setTextElementAttributes(SVGGraphicContext tspanGC,
                                          AttributedCharacterIterator ati) {
        String decoration = "";
        if (isUnderline(ati))
            decoration += CSS_UNDERLINE_VALUE + " ";
        if (isStrikeThrough(ati))
            decoration += CSS_LINE_THROUGH_VALUE + " ";
        int len = decoration.length();
        if (len != 0)
            tspanGC.getContext().put(CSS_TEXT_DECORATION_PROPERTY,
                                     decoration.substring(0, len-1));
    }
    private boolean isBold(AttributedCharacterIterator ati) {
        Object weight = ati.getAttribute(TextAttribute.WEIGHT);
        if (weight == null)
            return false;
        if (weight.equals(TextAttribute.WEIGHT_REGULAR))
            return false;
        if (weight.equals(TextAttribute.WEIGHT_DEMILIGHT))
            return false;
        if (weight.equals(TextAttribute.WEIGHT_EXTRA_LIGHT))
            return false;
        if (weight.equals(TextAttribute.WEIGHT_LIGHT))
            return false;
        return true;
    }
    private boolean isItalic(AttributedCharacterIterator ati) {
        Object attr = ati.getAttribute(TextAttribute.POSTURE);
        if (TextAttribute.POSTURE_OBLIQUE.equals(attr)) return true;
        return false;
    }
    private boolean isUnderline(AttributedCharacterIterator ati) {
        Object attr = ati.getAttribute(TextAttribute.UNDERLINE);
        if (TextAttribute.UNDERLINE_ON.equals(attr)) return true;
        else return false;
    }
    private boolean isStrikeThrough(AttributedCharacterIterator ati) {
        Object attr = ati.getAttribute(TextAttribute.STRIKETHROUGH);
        if (TextAttribute.STRIKETHROUGH_ON.equals(attr)) return true;
        return false;
    }
    public GraphicsConfiguration getDeviceConfiguration(){
        return null;
    }
    protected Set unsupportedAttributes;
    {
        unsupportedAttributes = new HashSet();
        unsupportedAttributes.add(TextAttribute.BACKGROUND);
        unsupportedAttributes.add(TextAttribute.BIDI_EMBEDDING);
        unsupportedAttributes.add(TextAttribute.CHAR_REPLACEMENT);
        unsupportedAttributes.add(TextAttribute.JUSTIFICATION);
        unsupportedAttributes.add(TextAttribute.RUN_DIRECTION);
        unsupportedAttributes.add(TextAttribute.SUPERSCRIPT);
        unsupportedAttributes.add(TextAttribute.SWAP_COLORS);
        unsupportedAttributes.add(TextAttribute.TRANSFORM);
        unsupportedAttributes.add(TextAttribute.WIDTH);
    }
    public void setUnsupportedAttributes(Set attrs) {
        if (attrs == null) unsupportedAttributes = null;
        else               unsupportedAttributes = new HashSet(attrs);
    }
    public boolean usesUnsupportedAttributes(AttributedCharacterIterator aci){
        if (unsupportedAttributes == null) return false;
        Set      allAttrs = aci.getAllAttributeKeys();
        Iterator iter     = allAttrs.iterator();
        while (iter.hasNext()) {
            if (unsupportedAttributes.contains(iter.next())) {
                return true;
            }
        }
        return false;
    }
}
