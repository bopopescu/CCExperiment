package org.apache.batik.ext.awt.g2d;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
public class GraphicContext implements Cloneable{
    protected AffineTransform defaultTransform = new AffineTransform();
    protected AffineTransform transform = new AffineTransform();
    protected List transformStack = new ArrayList();
    protected boolean transformStackValid = true;
    protected Paint paint = Color.black;
    protected Stroke stroke = new BasicStroke();
    protected Composite composite = AlphaComposite.SrcOver;
    protected Shape clip = null;
    protected RenderingHints hints = new RenderingHints(null);
    protected Font font = new Font("sanserif", Font.PLAIN, 12);
    protected Color background = new Color(0, 0, 0, 0);
    protected Color foreground = Color.black;
    public GraphicContext() {
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
    }
    public GraphicContext(AffineTransform defaultDeviceTransform) {
        this();
        defaultTransform = new AffineTransform(defaultDeviceTransform);
        transform = new AffineTransform(defaultTransform);
        if (!defaultTransform.isIdentity())
            transformStack.add(TransformStackElement.createGeneralTransformElement(defaultTransform));
    }
    public Object clone(){
        GraphicContext copyGc = new GraphicContext(defaultTransform);
        copyGc.transform = new AffineTransform(this.transform);
        copyGc.transformStack = new ArrayList( transformStack.size() );
        for(int i=0; i<this.transformStack.size(); i++){
            TransformStackElement stackElement =
                (TransformStackElement)this.transformStack.get(i);
            copyGc.transformStack.add(stackElement.clone());
        }
        copyGc.transformStackValid = this.transformStackValid;
        copyGc.paint = this.paint;
        copyGc.stroke = this.stroke;
        copyGc.composite = this.composite;
        if(clip != null)
            copyGc.clip = new GeneralPath(clip);
        else
            copyGc.clip = null;
        copyGc.hints = (RenderingHints)this.hints.clone();
        copyGc.font = this.font;
        copyGc.background = this.background;
        copyGc.foreground = this.foreground;
        return copyGc;
    }
    public Color getColor(){
        return foreground;
    }
    public void setColor(Color c){
        if(c == null)
            return;
        if(paint != c)
            setPaint(c);
    }
    public Font getFont(){
        return font;
    }
    public void setFont(Font font){
        if(font != null)
            this.font = font;
    }
    public Rectangle getClipBounds(){
        Shape c = getClip();
        if(c==null)
            return null;
        else
            return c.getBounds();
    }
    public void clipRect(int x, int y, int width, int height){
        clip(new Rectangle(x, y, width, height));
    }
    public void setClip(int x, int y, int width, int height){
        setClip(new Rectangle(x, y, width, height));
    }
    public Shape getClip(){
        try{
            return transform.createInverse().createTransformedShape(clip);
        }catch(NoninvertibleTransformException e){
            return null;
        }
    }
    public void setClip(Shape clip) {
        if (clip != null)
            this.clip = transform.createTransformedShape(clip);
        else
            this.clip = null;
    }
    public void setComposite(Composite comp){
        this.composite = comp;
    }
    public void setPaint( Paint paint ){
        if(paint == null)
            return;
        this.paint = paint;
        if(paint instanceof Color)
            foreground = (Color)paint;
    }
    public void setStroke(Stroke s){
        stroke = s;
    }
    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue){
        hints.put(hintKey, hintValue);
    }
    public Object getRenderingHint(RenderingHints.Key hintKey){
        return hints.get(hintKey);
    }
    public void setRenderingHints(Map hints){
        this.hints = new RenderingHints(hints);
    }
    public void addRenderingHints(Map hints){
        this.hints.putAll(hints);
    }
    public RenderingHints getRenderingHints(){
        return hints;
    }
    public void translate(int x, int y){
        if(x!=0 || y!=0){
            transform.translate(x, y);
            transformStack.add(TransformStackElement.createTranslateElement(x, y));
        }
    }
    public void translate(double tx, double ty){
        transform.translate(tx, ty);
        transformStack.add(TransformStackElement.createTranslateElement(tx, ty));
    }
    public void rotate(double theta){
        transform.rotate(theta);
        transformStack.add(TransformStackElement.createRotateElement(theta));
    }
    public void rotate(double theta, double x, double y){
        transform.rotate(theta, x, y);
        transformStack.add(TransformStackElement.createTranslateElement(x, y));
        transformStack.add(TransformStackElement.createRotateElement(theta));
        transformStack.add(TransformStackElement.createTranslateElement(-x, -y));
    }
    public void scale(double sx, double sy){
        transform.scale(sx, sy);
        transformStack.add(TransformStackElement.createScaleElement(sx, sy));
    }
    public void shear(double shx, double shy){
        transform.shear(shx, shy);
        transformStack.add(TransformStackElement.createShearElement(shx, shy));
    }
    public void transform(AffineTransform Tx){
        transform.concatenate(Tx);
        transformStack.add(TransformStackElement.createGeneralTransformElement(Tx));
    }
    public void setTransform(AffineTransform Tx){
        transform = new AffineTransform(Tx);
        invalidateTransformStack();
        if(!Tx.isIdentity())
            transformStack.add(TransformStackElement.createGeneralTransformElement(Tx));
    }
    public void validateTransformStack(){
        transformStackValid = true;
    }
    public boolean isTransformStackValid(){
        return transformStackValid;
    }
    public TransformStackElement[] getTransformStack(){
        TransformStackElement[] stack = new TransformStackElement[transformStack.size()];
        transformStack.toArray( stack );
        return stack;
    }
    protected void invalidateTransformStack(){
        transformStack.clear();
        transformStackValid = false;
    }
    public AffineTransform getTransform(){
        return new AffineTransform(transform);
    }
    public Paint getPaint(){
        return paint;
    }
    public Composite getComposite(){
        return composite;
    }
    public void setBackground(Color color){
        if(color == null)
            return;
        background = color;
    }
    public Color getBackground(){
        return background;
    }
    public Stroke getStroke(){
        return stroke;
    }
    public void clip(Shape s){
        if (s != null)
            s = transform.createTransformedShape(s);
        if (clip != null) {
            Area newClip = new Area(clip);
            newClip.intersect(new Area(s));
            clip = new GeneralPath(newClip);
        } else {
            clip = s;
        }
    }
    public FontRenderContext getFontRenderContext(){
        Object antialiasingHint = hints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
        boolean isAntialiased = true;
        if(antialiasingHint != RenderingHints.VALUE_TEXT_ANTIALIAS_ON &&
           antialiasingHint != RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT){
            if(antialiasingHint != RenderingHints.VALUE_TEXT_ANTIALIAS_OFF){
                antialiasingHint = hints.get(RenderingHints.KEY_ANTIALIASING);
                if(antialiasingHint != RenderingHints.VALUE_ANTIALIAS_ON &&
                   antialiasingHint != RenderingHints.VALUE_ANTIALIAS_DEFAULT){
                    if(antialiasingHint == RenderingHints.VALUE_ANTIALIAS_OFF)
                        isAntialiased = false;
                }
            }
            else
                isAntialiased = false;
        }
        boolean useFractionalMetrics = true;
        if(hints.get(RenderingHints.KEY_FRACTIONALMETRICS)
           == RenderingHints.VALUE_FRACTIONALMETRICS_OFF)
            useFractionalMetrics = false;
        FontRenderContext frc = new FontRenderContext(defaultTransform,
                                                      isAntialiased,
                                                      useFractionalMetrics);
        return frc;
    }
}
