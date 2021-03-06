package org.apache.batik.bridge.svg12;
import org.apache.batik.bridge.AbstractGraphicsNodeBridge;
import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.SVGUtilities;
import org.apache.batik.bridge.ScriptingEnvironment;
import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.dom.svg12.BindableElement;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.MutationEvent;
public class BindableElementBridge
        extends AbstractGraphicsNodeBridge
        implements SVG12BridgeUpdateHandler {
    public BindableElementBridge() {
    }
    public String getNamespaceURI() {
        return "*";
    }
    public String getLocalName() {
        return "*";
    }
    public Bridge getInstance() {
        return new BindableElementBridge();
    }
    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
        if (!SVGUtilities.matchUserAgent(e, ctx.getUserAgent())) {
            return null;
        }
        CompositeGraphicsNode gn = buildCompositeGraphicsNode(ctx, e, null);
        return gn;
    }
    public CompositeGraphicsNode
            buildCompositeGraphicsNode(BridgeContext ctx, 
                                       Element e,
                                       CompositeGraphicsNode gn) {
        BindableElement be = (BindableElement) e;
        Element shadowTree = be.getXblShadowTree();
        UpdateManager um = ctx.getUpdateManager();
        ScriptingEnvironment se = um == null ? null
                                             : um.getScriptingEnvironment();
        if (se != null && shadowTree != null) {
            se.addScriptingListeners(shadowTree);
        }
        if (gn == null) {
            gn = new CompositeGraphicsNode();
            associateSVGContext(ctx, e, gn);
        } else {
            int s = gn.size();
            for (int i = 0; i < s; i++) {
                gn.remove(0);
            }
        }
        GVTBuilder builder = ctx.getGVTBuilder();
        if (shadowTree != null) {
            GraphicsNode shadowNode = builder.build(ctx, shadowTree);
            if (shadowNode != null) {
                gn.add(shadowNode);
            }
        } else {
            for (Node m = e.getFirstChild(); m != null; m = m.getNextSibling()) {
                if (m.getNodeType() == Node.ELEMENT_NODE) {
                    GraphicsNode n = builder.build(ctx, (Element) m);
                    if (n != null) {
                        gn.add(n);
                    }
                }
            }
        }
        return gn;
    }
    public void dispose() {
        BindableElement be = (BindableElement) e;
        if (be != null && be.getCSSFirstChild() != null) {
            disposeTree(be.getCSSFirstChild());
        }
        super.dispose();
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
        initializeDynamicSupport(ctx, e, node);
    }
    public void handleDOMNodeInsertedEvent(MutationEvent evt) {
        BindableElement be = (BindableElement) e;
        Element shadowTree = be.getXblShadowTree();
        if (shadowTree == null && evt.getTarget() instanceof Element) {
            handleElementAdded((CompositeGraphicsNode) node, 
                               e, 
                               (Element) evt.getTarget());
        }
    }
    public void handleBindingEvent(Element bindableElement,
                                   Element shadowTree) {
        CompositeGraphicsNode gn = node.getParent();
        gn.remove(node);
        disposeTree(e);
        handleElementAdded(gn, e.getParentNode(), e);
    }
    public void handleContentSelectionChangedEvent
            (ContentSelectionChangedEvent csce) {
    }
    protected void handleElementAdded(CompositeGraphicsNode gn, 
                                      Node parent, 
                                      Element childElt) {
        GVTBuilder builder = ctx.getGVTBuilder();
        GraphicsNode childNode = builder.build(ctx, childElt);
        if (childNode == null) {
            return; 
        }
        int idx = -1;
        for(Node ps = childElt.getPreviousSibling(); ps != null;
            ps = ps.getPreviousSibling()) {
            if (ps.getNodeType() != Node.ELEMENT_NODE)
                continue;
            Element pse = (Element)ps;
            GraphicsNode psgn = ctx.getGraphicsNode(pse);
            while ((psgn != null) && (psgn.getParent() != gn)) {
                psgn = psgn.getParent();
            }
            if (psgn == null)
                continue;
            idx = gn.indexOf(psgn);
            if (idx == -1)
                continue;
            break;
        }
        idx++; 
        gn.add(idx, childNode);
    }
}
