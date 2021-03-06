package org.apache.batik.bridge;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.RootGraphicsNode;
import org.apache.batik.util.HaltingThread;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
public class GVTBuilder implements SVGConstants {
    public GVTBuilder() { }
    public GraphicsNode build(BridgeContext ctx, Document document) {
        ctx.setDocument(document);
        ctx.initializeDocument(document);
        ctx.setGVTBuilder(this);
        DocumentBridge dBridge = ctx.getDocumentBridge();
        RootGraphicsNode rootNode = null;
        try {
            rootNode = dBridge.createGraphicsNode(ctx, document);
            Element svgElement = document.getDocumentElement();
            GraphicsNode topNode = null;
            Bridge bridge = ctx.getBridge(svgElement);
            if (bridge == null || !(bridge instanceof GraphicsNodeBridge)) {
                return null;
            }
            GraphicsNodeBridge gnBridge = (GraphicsNodeBridge)bridge;
            topNode = gnBridge.createGraphicsNode(ctx, svgElement);
            if (topNode == null) {
                return null;
            }
            rootNode.getChildren().add(topNode);
            buildComposite(ctx, svgElement, (CompositeGraphicsNode)topNode);
            gnBridge.buildGraphicsNode(ctx, svgElement, topNode);
            dBridge.buildGraphicsNode(ctx, document, rootNode);
        } catch (BridgeException ex) {
            ex.setGraphicsNode(rootNode);
            throw ex; 
        }
        if (ctx.isInteractive()) {
            ctx.addUIEventListeners(document);
            ctx.addGVTListener(document);
        }
        if (ctx.isDynamic()) {
            ctx.addDOMListeners();
        }
        return rootNode;
    }
    public GraphicsNode build(BridgeContext ctx, Element e) {
        Bridge bridge = ctx.getBridge(e);
        if (bridge instanceof GenericBridge) {
            ((GenericBridge) bridge).handleElement(ctx, e);
            handleGenericBridges(ctx, e);
            return null;
        } else if (bridge == null || !(bridge instanceof GraphicsNodeBridge)) {
            handleGenericBridges(ctx, e);
            return null;
        }
        GraphicsNodeBridge gnBridge = (GraphicsNodeBridge)bridge;
        if (!gnBridge.getDisplay(e)) {
            handleGenericBridges(ctx, e);
            return null;
        }
        GraphicsNode gn = gnBridge.createGraphicsNode(ctx, e);
        if (gn != null) {
            if (gnBridge.isComposite()) {
                buildComposite(ctx, e, (CompositeGraphicsNode)gn);
            } else {
                handleGenericBridges(ctx, e);
            }
            gnBridge.buildGraphicsNode(ctx, e, gn);
        }
        if (ctx.isDynamic()) {
        }
        return gn;
    }
    protected void buildComposite(BridgeContext ctx,
                                  Element e,
                                  CompositeGraphicsNode parentNode) {
        for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                buildGraphicsNode(ctx, (Element)n, parentNode);
            }
        }
    }
    protected void buildGraphicsNode(BridgeContext ctx,
                                     Element e,
                                     CompositeGraphicsNode parentNode) {
        if (HaltingThread.hasBeenHalted()) {
            throw new InterruptedBridgeException();
        }
        Bridge bridge = ctx.getBridge(e);
        if (bridge instanceof GenericBridge) {
            ((GenericBridge) bridge).handleElement(ctx, e);
            handleGenericBridges(ctx, e);
            return;
        } else if (bridge == null || !(bridge instanceof GraphicsNodeBridge)) {
            handleGenericBridges(ctx, e);
            return;
        }
        if (!CSSUtilities.convertDisplay(e)) {
            handleGenericBridges(ctx, e);
            return;
        }
        GraphicsNodeBridge gnBridge = (GraphicsNodeBridge)bridge;
        try {
            GraphicsNode gn = gnBridge.createGraphicsNode(ctx, e);
            if (gn != null) {
                parentNode.getChildren().add(gn);
                if (gnBridge.isComposite()) {
                    buildComposite(ctx, e, (CompositeGraphicsNode)gn);
                } else {
                    handleGenericBridges(ctx, e);
                }
                gnBridge.buildGraphicsNode(ctx, e, gn);
            } else {
                handleGenericBridges(ctx, e);
            }
        } catch (BridgeException ex) {
            GraphicsNode errNode = ex.getGraphicsNode();
            if (errNode != null) {
                parentNode.getChildren().add(errNode);
                gnBridge.buildGraphicsNode(ctx, e, errNode);
                ex.setGraphicsNode(null);
            }
            throw ex;
        }
    }
    protected void handleGenericBridges(BridgeContext ctx, Element e) {
        for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element) {
                Element e2 = (Element) n;
                Bridge b = ctx.getBridge(e2);
                if (b instanceof GenericBridge) {
                    ((GenericBridge) b).handleElement(ctx, e2);
                }
                handleGenericBridges(ctx, e2);
            }
        }
    }
}
