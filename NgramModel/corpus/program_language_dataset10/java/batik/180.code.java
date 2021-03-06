package org.apache.batik.bridge;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import org.apache.batik.css.engine.CSSEngineEvent;
import org.apache.batik.dom.svg.AnimatedLiveAttributeValue;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.RootGraphicsNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.MutationEvent;
public class SVGDocumentBridge implements DocumentBridge, BridgeUpdateHandler,
                                          SVGContext {
    protected Document document;
    protected RootGraphicsNode node;
    protected BridgeContext ctx;
    public SVGDocumentBridge() {
    }
    public String getNamespaceURI() {
        return null;
    }
    public String getLocalName() {
        return null;
    }
    public Bridge getInstance() {
        return new SVGDocumentBridge();
    }
    public RootGraphicsNode createGraphicsNode(BridgeContext ctx,
                                               Document doc) {
        RootGraphicsNode gn = new RootGraphicsNode();
        this.document = doc;
        this.node = gn;
        this.ctx = ctx;
        ((SVGOMDocument) doc).setSVGContext(this);
        return gn;
    }
    public void buildGraphicsNode(BridgeContext ctx,
                                  Document doc,
                                  RootGraphicsNode node) {
        if (ctx.isDynamic()) {
            ctx.bind(doc, node);
        }
    }
    public void handleDOMAttrModifiedEvent(MutationEvent evt) {
    }
    public void handleDOMNodeInsertedEvent(MutationEvent evt) {
        if (evt.getTarget() instanceof Element) {
            Element childElt = (Element) evt.getTarget();
            GVTBuilder builder = ctx.getGVTBuilder();
            GraphicsNode childNode = builder.build(ctx, childElt);
            if (childNode == null) {
                return;
            }
            node.add(childNode);
        }
    }
    public void handleDOMNodeRemovedEvent(MutationEvent evt) {
    }
    public void handleDOMCharacterDataModified(MutationEvent evt) {
    }
    public void handleCSSEngineEvent(CSSEngineEvent evt) {
    }
    public void handleAnimatedAttributeChanged(AnimatedLiveAttributeValue alav) {
    }
    public void handleOtherAnimationChanged(String type) {
    }
    public void dispose() {
        ((SVGOMDocument) document).setSVGContext(null);
        ctx.unbind(document);
    }
    public float getPixelUnitToMillimeter() {
        return ctx.getUserAgent().getPixelUnitToMillimeter();
    }
    public float getPixelToMM() {
        return getPixelUnitToMillimeter();
    }
    public Rectangle2D getBBox() { return null; }
    public AffineTransform getScreenTransform() {
        return ctx.getUserAgent().getTransform();
    }
    public void setScreenTransform(AffineTransform at) {
        ctx.getUserAgent().setTransform(at);
    }
    public AffineTransform getCTM() { return null; }
    public AffineTransform getGlobalTransform() { return null; }
    public float getViewportWidth() { return 0f; }
    public float getViewportHeight() { return 0f; }
    public float getFontSize() { return 0; }
}
