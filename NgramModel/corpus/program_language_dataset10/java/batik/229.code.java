package org.apache.batik.bridge;
import java.awt.Cursor;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.svg.AbstractSVGAnimatedLength;
import org.apache.batik.dom.svg.AnimatedLiveAttributeValue;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.svg.SVGOMAnimatedLength;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.svg.SVGOMUseElement;
import org.apache.batik.dom.svg.SVGOMUseShadowRoot;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.svg.SVGTransformable;
import org.w3c.dom.svg.SVGUseElement;
public class SVGUseElementBridge extends AbstractGraphicsNodeBridge {
    protected ReferencedElementMutationListener l;
    protected BridgeContext subCtx;
    public SVGUseElementBridge() {}
    public String getLocalName() {
        return SVG_USE_TAG;
    }
    public Bridge getInstance(){
        return new SVGUseElementBridge();
    }
    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
        if (!SVGUtilities.matchUserAgent(e, ctx.getUserAgent()))
            return null;
        CompositeGraphicsNode gn = buildCompositeGraphicsNode(ctx, e, null);
        associateSVGContext(ctx, e, gn);
        return gn;
    }
    public CompositeGraphicsNode buildCompositeGraphicsNode
            (BridgeContext ctx, Element e, CompositeGraphicsNode gn) {
        SVGOMUseElement ue = (SVGOMUseElement) e;
        String uri = ue.getHref().getAnimVal();
        if (uri.length() == 0) {
            throw new BridgeException(ctx, e, ERR_ATTRIBUTE_MISSING,
                                      new Object[] {"xlink:href"});
        }
        Element refElement = ctx.getReferencedElement(e, uri);
        SVGOMDocument document, refDocument;
        document    = (SVGOMDocument)e.getOwnerDocument();
        refDocument = (SVGOMDocument)refElement.getOwnerDocument();
        boolean isLocal = (refDocument == document);
        BridgeContext theCtx = ctx;
        subCtx = null;
        if (!isLocal) {
            subCtx = (BridgeContext)refDocument.getCSSEngine().getCSSContext();
            theCtx = subCtx;
        }
        Element localRefElement;
        localRefElement = (Element)document.importNode(refElement, true, true);
        if (SVG_SYMBOL_TAG.equals(localRefElement.getLocalName())) {
            Element svgElement = document.createElementNS(SVG_NAMESPACE_URI, 
                                                          SVG_SVG_TAG);
            NamedNodeMap attrs = localRefElement.getAttributes();
            int len = attrs.getLength();
            for (int i = 0; i < len; i++) {
                Attr attr = (Attr)attrs.item(i);
                svgElement.setAttributeNS(attr.getNamespaceURI(),
                                          attr.getName(),
                                          attr.getValue());
            }
            for (Node n = localRefElement.getFirstChild();
                 n != null;
                 n = localRefElement.getFirstChild()) {
                svgElement.appendChild(n);
            }
            localRefElement = svgElement;
        }
        if (SVG_SVG_TAG.equals(localRefElement.getLocalName())) {
            try {
                SVGOMAnimatedLength al = (SVGOMAnimatedLength) ue.getWidth();
                if (al.isSpecified()) {
                    localRefElement.setAttributeNS
                        (null, SVG_WIDTH_ATTRIBUTE,
                         al.getAnimVal().getValueAsString());
                }
                al = (SVGOMAnimatedLength) ue.getHeight();
                if (al.isSpecified()) {
                    localRefElement.setAttributeNS
                        (null, SVG_HEIGHT_ATTRIBUTE,
                         al.getAnimVal().getValueAsString());
                }
            } catch (LiveAttributeException ex) {
                throw new BridgeException(ctx, ex);
            }
        }
        SVGOMUseShadowRoot root;
        root = new SVGOMUseShadowRoot(document, e, isLocal);
        root.appendChild(localRefElement);
        if (gn == null) {
            gn = new CompositeGraphicsNode();
            associateSVGContext(ctx, e, node);
        } else {
            int s = gn.size();
            for (int i=0; i<s; i++)
                gn.remove(0);
        }
        Node oldRoot = ue.getCSSFirstChild();
        if (oldRoot != null) {
            disposeTree(oldRoot);
        }
        ue.setUseShadowTree(root);
        Element g = localRefElement;
        CSSUtilities.computeStyleAndURIs(refElement, localRefElement, uri);
        GVTBuilder builder = ctx.getGVTBuilder();
        GraphicsNode refNode = builder.build(ctx, g);
        gn.getChildren().add(refNode);
        gn.setTransform(computeTransform((SVGTransformable) e, ctx));
        gn.setVisible(CSSUtilities.convertVisibility(e));
        RenderingHints hints = null;
        hints = CSSUtilities.convertColorRendering(e, hints);
        if (hints != null)
            gn.setRenderingHints(hints);
        Rectangle2D r = CSSUtilities.convertEnableBackground(e);
        if (r != null)
            gn.setBackgroundEnable(r);
        if (l != null) {
            NodeEventTarget target = l.target;
            target.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMAttrModified",
                 l, true);
            target.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeInserted",
                 l, true);
            target.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeRemoved",
                 l, true);
            target.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMCharacterDataModified",
                 l, true);
            l = null;
        }
        if (isLocal && ctx.isDynamic()) {
            l = new ReferencedElementMutationListener();
            NodeEventTarget target = (NodeEventTarget)refElement;
            l.target = target;
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMAttrModified",
                 l, true, null);
            theCtx.storeEventListenerNS
                (target, XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMAttrModified",
                 l, true);
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeInserted",
                 l, true, null);
            theCtx.storeEventListenerNS
                (target, XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeInserted",
                 l, true);
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeRemoved",
                 l, true, null);
            theCtx.storeEventListenerNS
                (target, XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeRemoved",
                 l, true);
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMCharacterDataModified",
                 l, true, null);
            theCtx.storeEventListenerNS
                (target, XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMCharacterDataModified",
                 l, true);
        }
        return gn;
    }
    public void dispose() {
        if (l != null) {
            NodeEventTarget target = l.target;
            target.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMAttrModified",
                 l, true);
            target.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeInserted",
                 l, true);
            target.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeRemoved",
                 l, true);
            target.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMCharacterDataModified",
                 l, true);
            l = null;
        }
        SVGOMUseElement ue = (SVGOMUseElement)e;
        if (ue != null && ue.getCSSFirstChild() != null) {
            disposeTree(ue.getCSSFirstChild());
        }
        super.dispose();
        subCtx = null;
    }
    protected AffineTransform computeTransform(SVGTransformable e,
                                               BridgeContext ctx) {
        AffineTransform at = super.computeTransform(e, ctx);
        SVGUseElement ue = (SVGUseElement) e;
        try {
            AbstractSVGAnimatedLength _x =
                (AbstractSVGAnimatedLength) ue.getX();
            float x = _x.getCheckedValue();
            AbstractSVGAnimatedLength _y =
                (AbstractSVGAnimatedLength) ue.getY();
            float y = _y.getCheckedValue();
            AffineTransform xy = AffineTransform.getTranslateInstance(x, y);
            xy.preConcatenate(at);
            return xy;
        } catch (LiveAttributeException ex) {
            throw new BridgeException(ctx, ex);
        }
     }
    protected GraphicsNode instantiateGraphicsNode() {
        return null; 
    }
    public boolean isComposite() {
        return false;
    }
    public void buildGraphicsNode(BridgeContext ctx,
                                  Element e,
                                  GraphicsNode node) {
        super.buildGraphicsNode(ctx, e, node);
        if (ctx.isInteractive()) {
            NodeEventTarget target = (NodeEventTarget)e;
            EventListener l = new CursorMouseOverListener(ctx);
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOVER,
                 l, false, null);
            ctx.storeEventListenerNS
                (target, XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOVER,
                 l, false);
        }
    }
    public static class CursorMouseOverListener implements EventListener {
        protected BridgeContext ctx;
        public CursorMouseOverListener(BridgeContext ctx) {
            this.ctx = ctx;
        }
        public void handleEvent(Event evt) {
            Element currentTarget = (Element)evt.getCurrentTarget();
            if (!CSSUtilities.isAutoCursor(currentTarget)) {
                Cursor cursor;
                cursor = CSSUtilities.convertCursor(currentTarget, ctx);
                if (cursor != null) {
                    ctx.getUserAgent().setSVGCursor(cursor);
                }
            }
        }
    }
    protected class ReferencedElementMutationListener implements EventListener {
        protected NodeEventTarget target;
        public void handleEvent(Event evt) {
            buildCompositeGraphicsNode(ctx, e, (CompositeGraphicsNode)node);
        }
    }
    public void handleAnimatedAttributeChanged
            (AnimatedLiveAttributeValue alav) {
        try {
            String ns = alav.getNamespaceURI();
            String ln = alav.getLocalName();
            if (ns == null) {
                if (ln.equals(SVG_X_ATTRIBUTE) ||
                    ln.equals(SVG_Y_ATTRIBUTE) ||
                    ln.equals(SVG_TRANSFORM_ATTRIBUTE)) {
                    node.setTransform
                        (computeTransform((SVGTransformable) e, ctx));
                    handleGeometryChanged();
                } 
                else if (ln.equals(SVG_WIDTH_ATTRIBUTE) ||
                         ln.equals(SVG_HEIGHT_ATTRIBUTE))
                    buildCompositeGraphicsNode
                        (ctx, e, (CompositeGraphicsNode)node);
            } else {
                if (ns.equals(XLINK_NAMESPACE_URI) &&
                    ln.equals(XLINK_HREF_ATTRIBUTE)) 
                    buildCompositeGraphicsNode
                        (ctx, e, (CompositeGraphicsNode)node);
            }
        } catch (LiveAttributeException ex) {
            throw new BridgeException(ctx, ex);
        }
        super.handleAnimatedAttributeChanged(alav);
    }
}
