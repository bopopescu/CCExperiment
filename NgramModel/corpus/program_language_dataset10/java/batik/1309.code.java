package org.apache.batik.swing.svg;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.net.URL;
import javax.swing.JOptionPane;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.BridgeExtension;
import org.apache.batik.bridge.DefaultScriptSecurity;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.ErrorConstants;
import org.apache.batik.bridge.ExternalResourceSecurity;
import org.apache.batik.bridge.RelaxedExternalResourceSecurity;
import org.apache.batik.bridge.ScriptSecurity;
import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.bridge.UpdateManagerEvent;
import org.apache.batik.bridge.UpdateManagerListener;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.event.EventDispatcher;
import org.apache.batik.gvt.text.Mark;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.script.Interpreter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.gvt.JGVTComponent;
import org.apache.batik.swing.gvt.JGVTComponentListener;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.RunnableQueue;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.SVGFeatureStrings;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAElement;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;
public class JSVGComponent extends JGVTComponent {
    public static final int AUTODETECT = 0;
    public static final int ALWAYS_DYNAMIC = 1;
    public static final int ALWAYS_STATIC = 2;
    public static final int ALWAYS_INTERACTIVE = 3;
    public static final String SCRIPT_ALERT = "script.alert";
    public static final String SCRIPT_PROMPT = "script.prompt";
    public static final String SCRIPT_CONFIRM = "script.confirm";
    public static final String BROKEN_LINK_TITLE = "broken.link.title";
    protected SVGDocumentLoader documentLoader;
    protected SVGDocumentLoader nextDocumentLoader;
    protected DocumentLoader loader;
    protected GVTTreeBuilder gvtTreeBuilder;
    protected GVTTreeBuilder nextGVTTreeBuilder;
    protected SVGLoadEventDispatcher svgLoadEventDispatcher;
    protected UpdateManager updateManager;
    protected UpdateManager nextUpdateManager;
    protected SVGDocument svgDocument;
    protected List svgDocumentLoaderListeners = new LinkedList();
    protected List gvtTreeBuilderListeners = new LinkedList();
    protected List svgLoadEventDispatcherListeners = new LinkedList();
    protected List linkActivationListeners = new LinkedList();
    protected List updateManagerListeners = new LinkedList();
    protected UserAgent userAgent;
    protected SVGUserAgent svgUserAgent;
    protected BridgeContext bridgeContext;
    protected String fragmentIdentifier;
    protected boolean isDynamicDocument;
    protected boolean isInteractiveDocument;
    protected boolean selfCallingDisableInteractions = false;
    protected boolean userSetDisableInteractions = false;
    protected int documentState;
    protected Dimension prevComponentSize;
    protected Runnable afterStopRunnable = null;
    protected SVGUpdateOverlay updateOverlay; 
    protected boolean recenterOnResize = true;
    protected AffineTransform viewingTransform = null;
    protected int animationLimitingMode;
    protected float animationLimitingAmount;
    public JSVGComponent() {
        this(null, false, false);
    }
    public JSVGComponent(SVGUserAgent ua, boolean eventsEnabled,
                         boolean selectableText) {
        super(eventsEnabled, selectableText);
        svgUserAgent = ua;
        userAgent = new BridgeUserAgentWrapper(createUserAgent());
        addSVGDocumentLoaderListener((SVGListener)listener);
        addGVTTreeBuilderListener((SVGListener)listener);
        addSVGLoadEventDispatcherListener((SVGListener)listener);
        if (updateOverlay != null)
            getOverlays().add(updateOverlay);
    }
    public void dispose() {
        setSVGDocument(null);
    }
    public void setDisableInteractions(boolean b) {
        super.setDisableInteractions(b);
        if (!selfCallingDisableInteractions)
            userSetDisableInteractions = true;
    }
    public void clearUserSetDisableInteractions() {
        userSetDisableInteractions = false;
        updateZoomAndPanEnable(svgDocument);
    }
    public void updateZoomAndPanEnable(Document doc) {
        if (userSetDisableInteractions) return;
        if (doc == null) return;
        try {
            Element root = doc.getDocumentElement();
            String znp = root.getAttributeNS
                (null, SVGConstants.SVG_ZOOM_AND_PAN_ATTRIBUTE);
            boolean enable = SVGConstants.SVG_MAGNIFY_VALUE.equals(znp);
            selfCallingDisableInteractions = true;
            setDisableInteractions(!enable);
        } finally {
            selfCallingDisableInteractions = false;
        }
    }
    public boolean getRecenterOnResize() {
        return recenterOnResize;
    }
    public void setRecenterOnResize(boolean recenterOnResize) {
        this.recenterOnResize = recenterOnResize;
    }
    public boolean isDynamic() {
        return isDynamicDocument;
    }
    public boolean isInteractive() {
        return isInteractiveDocument;
    }
    public void setDocumentState(int state) {
        documentState = state;
    }
    public UpdateManager getUpdateManager() {
        if (svgLoadEventDispatcher != null) {
            return svgLoadEventDispatcher.getUpdateManager();
        }
        if (nextUpdateManager != null) {
            return nextUpdateManager;
        }
        return updateManager;
    }
    public void resumeProcessing() {
        if (updateManager != null) {
            updateManager.resume();
        }
    }
    public void suspendProcessing() {
        if (updateManager != null) {
            updateManager.suspend();
        }
    }
    public void stopProcessing() {
        nextDocumentLoader = null;
        nextGVTTreeBuilder = null;
        if (documentLoader != null) {
            documentLoader.halt();
        }
        if (gvtTreeBuilder != null) {
            gvtTreeBuilder.halt();
        }
        if (svgLoadEventDispatcher != null) {
            svgLoadEventDispatcher.halt();
        }
        if (nextUpdateManager != null) {
            nextUpdateManager.interrupt();
            nextUpdateManager = null;
        }
        if (updateManager != null) {
            updateManager.interrupt();
        }
        super.stopProcessing();
    }
    public void loadSVGDocument(String url) {
        String oldURI = null;
        if (svgDocument != null) {
            oldURI = svgDocument.getURL();
        }
        final ParsedURL newURI = new ParsedURL(oldURI, url);
        stopThenRun(new Runnable() {
                public void run() {
                    String url = newURI.toString();
                    fragmentIdentifier = newURI.getRef();
                    loader = new DocumentLoader(userAgent);
                    nextDocumentLoader = new SVGDocumentLoader(url, loader);
                    nextDocumentLoader.setPriority(Thread.MIN_PRIORITY);
                    Iterator it = svgDocumentLoaderListeners.iterator();
                    while (it.hasNext()) {
                        nextDocumentLoader.addSVGDocumentLoaderListener
                            ((SVGDocumentLoaderListener)it.next());
                    }
                    startDocumentLoader();
                }
            });
    }
    private void startDocumentLoader() {
        documentLoader = nextDocumentLoader;
        nextDocumentLoader = null;
        documentLoader.start();
    }
    public void setDocument(Document doc) {
        if ((doc != null) &&
            !(doc.getImplementation() instanceof SVGDOMImplementation)) {
            DOMImplementation impl;
            impl = SVGDOMImplementation.getDOMImplementation();
            Document d = DOMUtilities.deepCloneDocument(doc, impl);
            doc = d;
        }
        setSVGDocument((SVGDocument)doc);
    }
    public void setSVGDocument(SVGDocument doc) {
        if ((doc != null) &&
            !(doc.getImplementation() instanceof SVGDOMImplementation)) {
            DOMImplementation impl;
            impl = SVGDOMImplementation.getDOMImplementation();
            Document d = DOMUtilities.deepCloneDocument(doc, impl);
            doc = (SVGDocument)d;
        }
        final SVGDocument svgdoc = doc;
        stopThenRun(new Runnable() {
                public void run() {
                    installSVGDocument(svgdoc);
                }
            });
    }
    protected void stopThenRun(final Runnable r) {
        if (afterStopRunnable != null) {
            afterStopRunnable = r;
            return;
        }
        afterStopRunnable = r;
        stopProcessing();
        if ((documentLoader         == null) &&
            (gvtTreeBuilder         == null) &&
            (gvtTreeRenderer        == null) &&
            (svgLoadEventDispatcher == null) &&
            (nextUpdateManager      == null) &&
            (updateManager          == null)) {
            Runnable asr = afterStopRunnable;
            afterStopRunnable = null;
            asr.run();
        }
    }
    protected void installSVGDocument(SVGDocument doc) {
        svgDocument = doc;
        if (bridgeContext != null) {
            bridgeContext.dispose();
            bridgeContext = null;
        }
        releaseRenderingReferences();
        if (doc == null) {
            isDynamicDocument = false;
            isInteractiveDocument = false;
            disableInteractions = true;
            initialTransform = new AffineTransform();
            setRenderingTransform(initialTransform, false);
            Rectangle vRect = getRenderRect();
            repaint(vRect.x,     vRect.y,
                    vRect.width, vRect.height);
            return;
        }
        bridgeContext = createBridgeContext((SVGOMDocument) doc);
        switch (documentState) {
        case ALWAYS_STATIC:
            isDynamicDocument = false;
            isInteractiveDocument = false;
            break;
        case ALWAYS_INTERACTIVE:
            isDynamicDocument = false;
            isInteractiveDocument = true;
            break;
        case ALWAYS_DYNAMIC:
            isDynamicDocument = true;
            isInteractiveDocument = true;
            break;
        case AUTODETECT:
            isDynamicDocument = bridgeContext.isDynamicDocument(doc);
            isInteractiveDocument =
                isDynamicDocument || bridgeContext.isInteractiveDocument(doc);
        }
        if (isInteractiveDocument) {
            if (isDynamicDocument)
                bridgeContext.setDynamicState(BridgeContext.DYNAMIC);
            else
                bridgeContext.setDynamicState(BridgeContext.INTERACTIVE);
        }
        setBridgeContextAnimationLimitingMode();
        updateZoomAndPanEnable(doc);
        nextGVTTreeBuilder = new GVTTreeBuilder(doc, bridgeContext);
        nextGVTTreeBuilder.setPriority(Thread.MIN_PRIORITY);
        Iterator it = gvtTreeBuilderListeners.iterator();
        while (it.hasNext()) {
            nextGVTTreeBuilder.addGVTTreeBuilderListener
                ((GVTTreeBuilderListener)it.next());
        }
        initializeEventHandling();
        if (gvtTreeBuilder == null &&
            documentLoader == null &&
            gvtTreeRenderer == null &&
            svgLoadEventDispatcher == null &&
            updateManager == null) {
            startGVTTreeBuilder();
        }
    }
    protected void startGVTTreeBuilder() {
        gvtTreeBuilder = nextGVTTreeBuilder;
        nextGVTTreeBuilder = null;
        gvtTreeBuilder.start();
    }
    public SVGDocument getSVGDocument() {
        return svgDocument;
    }
    public Dimension2D getSVGDocumentSize() {
        return bridgeContext.getDocumentSize();
    }
    public String getFragmentIdentifier() {
        return fragmentIdentifier;
    }
    public void setFragmentIdentifier(String fi) {
        fragmentIdentifier = fi;
        if (computeRenderingTransform())
            scheduleGVTRendering();
    }
    public void flushImageCache() {
        ImageTagRegistry reg = ImageTagRegistry.getRegistry();
        reg.flushCache();
    }
    public void setGraphicsNode(GraphicsNode gn, boolean createDispatcher) {
        Dimension2D dim = bridgeContext.getDocumentSize();
        Dimension   mySz = new Dimension((int)dim.getWidth(),
                                         (int)dim.getHeight());
        JSVGComponent.this.setMySize(mySz);
        SVGSVGElement elt = svgDocument.getRootElement();
        prevComponentSize = getSize();
        AffineTransform at = calculateViewingTransform
            (fragmentIdentifier, elt);
        CanvasGraphicsNode cgn = getCanvasGraphicsNode(gn);
        if (cgn != null) {
            cgn.setViewingTransform(at);
        }
        viewingTransform = null;
        initialTransform = new AffineTransform();
        setRenderingTransform(initialTransform, false);
        jsvgComponentListener.updateMatrix(initialTransform);
        addJGVTComponentListener(jsvgComponentListener);
        addComponentListener(jsvgComponentListener);
        super.setGraphicsNode(gn, createDispatcher);
    }
    protected BridgeContext createBridgeContext(SVGOMDocument doc) {
        if (loader == null) {
            loader = new DocumentLoader(userAgent);
        }
        BridgeContext result;
        if (doc.isSVG12()) {
            result = new SVG12BridgeContext(userAgent, loader);
        } else {
            result = new BridgeContext(userAgent, loader);
        }
        return result;
    }
    protected void startSVGLoadEventDispatcher(GraphicsNode root) {
        UpdateManager um = new UpdateManager(bridgeContext,
                                             root,
                                             svgDocument);
        svgLoadEventDispatcher =
            new SVGLoadEventDispatcher(root,
                                       svgDocument,
                                       bridgeContext,
                                       um);
        Iterator it = svgLoadEventDispatcherListeners.iterator();
        while (it.hasNext()) {
            svgLoadEventDispatcher.addSVGLoadEventDispatcherListener
                ((SVGLoadEventDispatcherListener)it.next());
        }
        svgLoadEventDispatcher.start();
    }
    protected ImageRenderer createImageRenderer() {
        if (isDynamicDocument) {
            return rendererFactory.createDynamicImageRenderer();
        } else {
            return rendererFactory.createStaticImageRenderer();
        }
    }
    public CanvasGraphicsNode getCanvasGraphicsNode() {
        return getCanvasGraphicsNode(gvtRoot);
    }
    protected CanvasGraphicsNode getCanvasGraphicsNode(GraphicsNode gn) {
        if (!(gn instanceof CompositeGraphicsNode))
            return null;
        CompositeGraphicsNode cgn = (CompositeGraphicsNode)gn;
        List children = cgn.getChildren();
        if (children.size() == 0)
            return null;
        gn = (GraphicsNode)children.get(0);
        if (!(gn instanceof CanvasGraphicsNode))
            return null;
        return (CanvasGraphicsNode)gn;
    }
    public AffineTransform getViewingTransform() {
        AffineTransform vt;
        synchronized (this) {
            vt = viewingTransform;
            if (vt == null) {
                CanvasGraphicsNode cgn = getCanvasGraphicsNode();
                if (cgn != null)
                    vt = cgn.getViewingTransform();
            }
        }
        return vt;
    }
    public AffineTransform getViewBoxTransform() {
        AffineTransform at = getRenderingTransform();
        if (at == null) at = new AffineTransform();
        else            at = new AffineTransform(at);
        AffineTransform vt = getViewingTransform();
        if (vt != null) {
            at.concatenate(vt);
        }
        return at;
    }
    protected boolean computeRenderingTransform() {
        if ((svgDocument == null) || (gvtRoot == null))
            return false;
        boolean ret = updateRenderingTransform();
        initialTransform = new AffineTransform();
        if (!initialTransform.equals(getRenderingTransform())) {
            setRenderingTransform(initialTransform, false);
            ret = true;
        }
        return ret;
    }
    protected AffineTransform calculateViewingTransform
        (String fragIdent, SVGSVGElement svgElt) {
        Dimension d = getSize();
        if (d.width  < 1) d.width  = 1;
        if (d.height < 1) d.height = 1;
        return ViewBox.getViewTransform
            (fragIdent, svgElt, d.width, d.height, bridgeContext);
    }
    protected boolean updateRenderingTransform() {
        if ((svgDocument == null) || (gvtRoot == null))
            return false;
        try {
            SVGSVGElement elt = svgDocument.getRootElement();
            Dimension d = getSize();
            Dimension oldD = prevComponentSize;
            if (oldD == null) oldD = d;
            prevComponentSize = d;
            if (d.width  < 1) d.width  = 1;
            if (d.height < 1) d.height = 1;
            final AffineTransform at = calculateViewingTransform
                (fragmentIdentifier, elt);
            AffineTransform vt = getViewingTransform();
            if (at.equals(vt)) {
                return ((oldD.width != d.width) || (oldD.height != d.height));
            }
            if (recenterOnResize) {
                Point2D pt = new Point2D.Float(oldD.width/2.0f,
                                               oldD.height/2.0f);
                AffineTransform rendAT = getRenderingTransform();
                if (rendAT != null) {
                    try {
                        AffineTransform invRendAT = rendAT.createInverse();
                        pt = invRendAT.transform(pt, null);
                    } catch (NoninvertibleTransformException e) { }
                }
                if (vt != null) {
                    try {
                        AffineTransform invVT = vt.createInverse();
                        pt = invVT.transform(pt, null);
                    } catch (NoninvertibleTransformException e) { }
                }
                if (at != null)
                    pt = at.transform(pt, null);
                if (rendAT != null)
                    pt = rendAT.transform(pt, null);
                float dx = (float)((d.width/2.0f) -pt.getX());
                float dy = (float)((d.height/2.0f)-pt.getY());
                dx = (int)((dx < 0)?(dx - .5):(dx + .5));
                dy = (int)((dy < 0)?(dy - .5):(dy + .5));
                if ((dx != 0) || (dy != 0)) {
                    rendAT.preConcatenate
                        (AffineTransform.getTranslateInstance(dx, dy));
                    setRenderingTransform(rendAT, false);
                }
            }
            synchronized (this) {
                viewingTransform = at;
            }
            Runnable r = new Runnable() {
                    AffineTransform myAT = at;
                    CanvasGraphicsNode myCGN = getCanvasGraphicsNode();
                    public void run() {
                        synchronized (JSVGComponent.this) {
                            if (myCGN != null) {
                                myCGN.setViewingTransform(myAT);
                            }
                            if (viewingTransform == myAT)
                                viewingTransform = null;
                        }
                    }
                };
            UpdateManager um = getUpdateManager();
            if (um != null) um.getUpdateRunnableQueue().invokeLater(r);
            else             r.run();
        } catch (BridgeException e) {
            userAgent.displayError(e);
        }
        return true;
    }
    protected void renderGVTTree() {
        if (!isInteractiveDocument ||
            updateManager == null ||
            !updateManager.isRunning()) {
            super.renderGVTTree();
            return;
        }
        final Rectangle visRect = getRenderRect();
        if ((gvtRoot == null)    ||
            (visRect.width <= 0) ||
            (visRect.height <= 0)) {
            return;
        }
        AffineTransform inv = null;
        try {
            inv = renderingTransform.createInverse();
        } catch (NoninvertibleTransformException e) {
        }
        final Shape s;
        if (inv == null) s = visRect;
        else             s = inv.createTransformedShape(visRect);
        class UpdateRenderingRunnable implements Runnable {
            AffineTransform at;
            boolean         doubleBuf;
            boolean         clearPaintTrans;
            Shape           aoi;
            int             width;
            int             height;
            boolean active;
            public UpdateRenderingRunnable(AffineTransform at,
                                           boolean doubleBuf,
                                           boolean clearPaintTrans,
                                           Shape aoi,
                                           int width, int height) {
                updateInfo(at, doubleBuf, clearPaintTrans, aoi, width, height);
                active = true;
            }
            public void updateInfo(AffineTransform at,
                                   boolean doubleBuf,
                                   boolean clearPaintTrans,
                                   Shape aoi,
                                   int width, int height) {
                this.at              = at;
                this.doubleBuf       = doubleBuf;
                this.clearPaintTrans = clearPaintTrans;
                this.aoi             = aoi;
                this.width           = width;
                this.height          = height;
                active = true;
            }
            public void deactivate() {
                active = false;
            }
            public void run() {
                if (!active) return;
                updateManager.updateRendering
                    (at, doubleBuf, clearPaintTrans, aoi, width, height);
            }
        }
        RunnableQueue rq = updateManager.getUpdateRunnableQueue();
        synchronized (rq.getIteratorLock()) {
            Iterator it = rq.iterator();
            while (it.hasNext()) {
                Object next = it.next();
                if (next instanceof UpdateRenderingRunnable) {
                    ((UpdateRenderingRunnable)next).deactivate();
                }
            }
        }
        rq.invokeLater(new UpdateRenderingRunnable
                       (renderingTransform,
                        doubleBufferedRendering, true, s,
                        visRect.width, visRect.height));
    }
    protected void handleException(Exception e) {
        userAgent.displayError(e);
    }
    public void addSVGDocumentLoaderListener(SVGDocumentLoaderListener l) {
        svgDocumentLoaderListeners.add(l);
    }
    public void removeSVGDocumentLoaderListener(SVGDocumentLoaderListener l) {
        svgDocumentLoaderListeners.remove(l);
    }
    public void addGVTTreeBuilderListener(GVTTreeBuilderListener l) {
        gvtTreeBuilderListeners.add(l);
    }
    public void removeGVTTreeBuilderListener(GVTTreeBuilderListener l) {
        gvtTreeBuilderListeners.remove(l);
    }
    public void addSVGLoadEventDispatcherListener
        (SVGLoadEventDispatcherListener l) {
        svgLoadEventDispatcherListeners.add(l);
    }
    public void removeSVGLoadEventDispatcherListener
        (SVGLoadEventDispatcherListener l) {
        svgLoadEventDispatcherListeners.remove(l);
    }
    public void addLinkActivationListener(LinkActivationListener l) {
        linkActivationListeners.add(l);
    }
    public void removeLinkActivationListener(LinkActivationListener l) {
        linkActivationListeners.remove(l);
    }
    public void addUpdateManagerListener(UpdateManagerListener l) {
        updateManagerListeners.add(l);
    }
    public void removeUpdateManagerListener(UpdateManagerListener l) {
        updateManagerListeners.remove(l);
    }
    public void showAlert(String message) {
        JOptionPane.showMessageDialog
            (this, Messages.formatMessage(SCRIPT_ALERT,
                                          new Object[] { message }));
    }
    public String showPrompt(String message) {
        return JOptionPane.showInputDialog
            (this, Messages.formatMessage(SCRIPT_PROMPT,
                                          new Object[] { message }));
    }
    public String showPrompt(String message, String defaultValue) {
        return (String)JOptionPane.showInputDialog
            (this,
             Messages.formatMessage(SCRIPT_PROMPT,
                                    new Object[] { message }),
             null,
             JOptionPane.PLAIN_MESSAGE,
             null, null, defaultValue);
    }
    public boolean showConfirm(String message) {
        return JOptionPane.showConfirmDialog
            (this, Messages.formatMessage(SCRIPT_CONFIRM,
                                          new Object[] { message }),
             "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
    public void setMySize(Dimension d) {
        setPreferredSize(d);
        invalidate();
    }
    public void setAnimationLimitingNone() {
        animationLimitingMode = 0;
        if (bridgeContext != null) {
            setBridgeContextAnimationLimitingMode();
        }
    }
    public void setAnimationLimitingCPU(float pc) {
        animationLimitingMode = 1;
        animationLimitingAmount = pc;
        if (bridgeContext != null) {
            setBridgeContextAnimationLimitingMode();
        }
    }
    public void setAnimationLimitingFPS(float fps) {
        animationLimitingMode = 2;
        animationLimitingAmount = fps;
        if (bridgeContext != null) {
            setBridgeContextAnimationLimitingMode();
        }
    }
    public Interpreter getInterpreter(String type) {
        if (bridgeContext != null) {
            return bridgeContext.getInterpreter(type);
        }
        return null;
    }
    protected void setBridgeContextAnimationLimitingMode() {
        switch (animationLimitingMode) {
            case 0: 
                bridgeContext.setAnimationLimitingNone();
                break;
            case 1: 
                bridgeContext.setAnimationLimitingCPU(animationLimitingAmount);
                break;
            case 2: 
                bridgeContext.setAnimationLimitingFPS(animationLimitingAmount);
                break;
        }
    }
    protected JSVGComponentListener jsvgComponentListener =
        new JSVGComponentListener();
    protected class JSVGComponentListener extends ComponentAdapter
        implements JGVTComponentListener {
        float prevScale = 0;
        float prevTransX = 0;
        float prevTransY = 0;
        public void componentResized(ComponentEvent ce) {
            if (isDynamicDocument &&
                (updateManager != null) && updateManager.isRunning()) {
                updateManager.getUpdateRunnableQueue().invokeLater
                    (new Runnable() {
                        public void run() {
                            try {
                                updateManager.dispatchSVGResizeEvent();
                            } catch (InterruptedException ie) {
                            }
                        }});
            }
        }
        public void componentTransformChanged(ComponentEvent event) {
            AffineTransform at = getRenderingTransform();
            float currScale  = (float)Math.sqrt(at.getDeterminant());
            float currTransX = (float)at.getTranslateX();
            float currTransY = (float)at.getTranslateY();
            final boolean dispatchZoom    = (currScale != prevScale);
            final boolean dispatchScroll  = ((currTransX != prevTransX) ||
                                             (currTransY != prevTransY));
            if (isDynamicDocument &&
                (updateManager != null) && updateManager.isRunning()) {
                updateManager.getUpdateRunnableQueue().invokeLater
                    (new Runnable() {
                        public void run() {
                            try {
                                if (dispatchZoom)
                                    updateManager.dispatchSVGZoomEvent();
                                if (dispatchScroll)
                                    updateManager.dispatchSVGScrollEvent();
                            } catch (InterruptedException ie) {
                            }
                        }});
            }
            prevScale = currScale;
            prevTransX = currTransX;
            prevTransY = currTransY;
        }
        public void updateMatrix(AffineTransform at) {
            prevScale  = (float)Math.sqrt(at.getDeterminant());
            prevTransX = (float)at.getTranslateX();
            prevTransY = (float)at.getTranslateY();
        }
    }
    protected Listener createListener() {
        return new SVGListener();
    }
    protected class SVGListener
        extends Listener
        implements SVGDocumentLoaderListener,
                   GVTTreeBuilderListener,
                   SVGLoadEventDispatcherListener,
                   UpdateManagerListener {
        protected SVGListener() {
        }
        public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
        }
        public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
            if (nextDocumentLoader != null) {
                startDocumentLoader();
                return;
            }
            documentLoader = null;
            if (afterStopRunnable != null) {
                EventQueue.invokeLater(afterStopRunnable);
                afterStopRunnable = null;
                return;
            }
            setSVGDocument(e.getSVGDocument());
        }
        public void documentLoadingCancelled(SVGDocumentLoaderEvent e) {
            if (nextDocumentLoader != null) {
                startDocumentLoader();
                return;
            }
            documentLoader = null;
            if (afterStopRunnable != null) {
                EventQueue.invokeLater(afterStopRunnable);
                afterStopRunnable = null;
                return;
            }
            if (nextGVTTreeBuilder != null) {
                startGVTTreeBuilder();
                return;
            }
        }
        public void documentLoadingFailed(SVGDocumentLoaderEvent e) {
            if (nextDocumentLoader != null) {
                startDocumentLoader();
                return;
            }
            documentLoader = null;
            userAgent.displayError(((SVGDocumentLoader)e.getSource()).
                                   getException());
            if (afterStopRunnable != null) {
                EventQueue.invokeLater(afterStopRunnable);
                afterStopRunnable = null;
                return;
            }
            if (nextGVTTreeBuilder != null) {
                startGVTTreeBuilder();
                return;
            }
        }
        public void gvtBuildStarted(GVTTreeBuilderEvent e) {
            removeJGVTComponentListener(jsvgComponentListener);
            removeComponentListener(jsvgComponentListener);
        }
        public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
            if (nextGVTTreeBuilder != null) {
                startGVTTreeBuilder();
                return;
            }
            loader = null;
            gvtTreeBuilder = null;
            if (afterStopRunnable != null) {
                EventQueue.invokeLater(afterStopRunnable);
                afterStopRunnable = null;
                return;
            }
            if (nextDocumentLoader != null) {
                startDocumentLoader();
                return;
            }
            gvtRoot = null;
            if (isDynamicDocument && JSVGComponent.this.eventsEnabled) {
                startSVGLoadEventDispatcher(e.getGVTRoot());
            } else {
                if (isInteractiveDocument) {
                    nextUpdateManager = new UpdateManager(bridgeContext,
                                                          e.getGVTRoot(),
                                                          svgDocument);
                }
                JSVGComponent.this.setGraphicsNode(e.getGVTRoot(), false);
                scheduleGVTRendering();
            }
        }
        public void gvtBuildCancelled(GVTTreeBuilderEvent e) {
            if (nextGVTTreeBuilder != null) {
                startGVTTreeBuilder();
                return;
            }
            loader = null;
            gvtTreeBuilder = null;
            if (afterStopRunnable != null) {
                EventQueue.invokeLater(afterStopRunnable);
                afterStopRunnable = null;
                return;
            }
            if (nextDocumentLoader != null) {
                startDocumentLoader();
                return;
            }
            JSVGComponent.this.image = null;
            repaint();
        }
        public void gvtBuildFailed(GVTTreeBuilderEvent e) {
            if (nextGVTTreeBuilder != null) {
                startGVTTreeBuilder();
                return;
            }
            loader = null;
            gvtTreeBuilder = null;
            if (afterStopRunnable != null) {
                EventQueue.invokeLater(afterStopRunnable);
                afterStopRunnable = null;
                return;
            }
            if (nextDocumentLoader != null) {
                startDocumentLoader();
                return;
            }
            GraphicsNode gn = e.getGVTRoot();
            if (gn == null) {
                JSVGComponent.this.image = null;
                repaint();
            } else {
                JSVGComponent.this.setGraphicsNode(gn, false);
                computeRenderingTransform();
            }
            userAgent.displayError(((GVTTreeBuilder)e.getSource())
                                   .getException());
        }
        public void svgLoadEventDispatchStarted
            (SVGLoadEventDispatcherEvent e) {
        }
        public void svgLoadEventDispatchCompleted
            (SVGLoadEventDispatcherEvent e) {
            nextUpdateManager = svgLoadEventDispatcher.getUpdateManager();
            svgLoadEventDispatcher = null;
            if (afterStopRunnable != null) {
                nextUpdateManager.interrupt();
                nextUpdateManager = null;
                EventQueue.invokeLater(afterStopRunnable);
                afterStopRunnable = null;
                return;
            }
            if (nextGVTTreeBuilder != null) {
                nextUpdateManager.interrupt();
                nextUpdateManager = null;
                startGVTTreeBuilder();
                return;
            }
            if (nextDocumentLoader != null) {
                nextUpdateManager.interrupt();
                nextUpdateManager = null;
                startDocumentLoader();
                return;
            }
            JSVGComponent.this.setGraphicsNode(e.getGVTRoot(), false);
            scheduleGVTRendering();
        }
        public void svgLoadEventDispatchCancelled
            (SVGLoadEventDispatcherEvent e) {
            nextUpdateManager = svgLoadEventDispatcher.getUpdateManager();
            svgLoadEventDispatcher = null;
            nextUpdateManager.interrupt();
            nextUpdateManager = null;
            if (afterStopRunnable != null) {
                EventQueue.invokeLater(afterStopRunnable);
                afterStopRunnable = null;
                return;
            }
            if (nextGVTTreeBuilder != null) {
                startGVTTreeBuilder();
                return;
            }
            if (nextDocumentLoader != null) {
                startDocumentLoader();
                return;
            }
        }
        public void svgLoadEventDispatchFailed
            (SVGLoadEventDispatcherEvent e) {
            nextUpdateManager = svgLoadEventDispatcher.getUpdateManager();
            svgLoadEventDispatcher = null;
            nextUpdateManager.interrupt();
            nextUpdateManager = null;
            if (afterStopRunnable != null) {
                EventQueue.invokeLater(afterStopRunnable);
                afterStopRunnable = null;
                return;
            }
            if (nextGVTTreeBuilder != null) {
                startGVTTreeBuilder();
                return;
            }
            if (nextDocumentLoader != null) {
                startDocumentLoader();
                return;
            }
            GraphicsNode gn = e.getGVTRoot();
            if (gn == null) {
                JSVGComponent.this.image = null;
                repaint();
            } else {
                JSVGComponent.this.setGraphicsNode(gn, false);
                computeRenderingTransform();
            }
            userAgent.displayError(((SVGLoadEventDispatcher)e.getSource())
                                   .getException());
        }
        public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
            super.gvtRenderingCompleted(e);
            if (afterStopRunnable != null) {
                if (nextUpdateManager != null) {
                    nextUpdateManager.interrupt();
                    nextUpdateManager = null;
                }
                EventQueue.invokeLater(afterStopRunnable);
                afterStopRunnable = null;
                return;
            }
            if (nextGVTTreeBuilder != null) {
                if (nextUpdateManager != null) {
                    nextUpdateManager.interrupt();
                    nextUpdateManager = null;
                }
                startGVTTreeBuilder();
                return;
            }
            if (nextDocumentLoader != null) {
                if (nextUpdateManager != null) {
                    nextUpdateManager.interrupt();
                    nextUpdateManager = null;
                }
                startDocumentLoader();
                return;
            }
            if (nextUpdateManager != null) {
                updateManager = nextUpdateManager;
                nextUpdateManager = null;
                updateManager.addUpdateManagerListener(this);
                updateManager.manageUpdates(renderer);
            }
        }
        public void gvtRenderingCancelled(GVTTreeRendererEvent e) {
            super.gvtRenderingCancelled(e);
            if (afterStopRunnable != null) {
                if (nextUpdateManager != null) {
                    nextUpdateManager.interrupt();
                    nextUpdateManager = null;
                }
                EventQueue.invokeLater(afterStopRunnable);
                afterStopRunnable = null;
                return;
            }
            if (nextGVTTreeBuilder != null) {
                if (nextUpdateManager != null) {
                    nextUpdateManager.interrupt();
                    nextUpdateManager = null;
                }
                startGVTTreeBuilder();
                return;
            }
            if (nextDocumentLoader != null) {
                if (nextUpdateManager != null) {
                    nextUpdateManager.interrupt();
                    nextUpdateManager = null;
                }
                startDocumentLoader();
                return;
            }
        }
        public void gvtRenderingFailed(GVTTreeRendererEvent e) {
            super.gvtRenderingFailed(e);
            if (afterStopRunnable != null) {
                if (nextUpdateManager != null) {
                    nextUpdateManager.interrupt();
                    nextUpdateManager = null;
                }
                EventQueue.invokeLater(afterStopRunnable);
                afterStopRunnable = null;
                return;
            }
            if (nextGVTTreeBuilder != null) {
                if (nextUpdateManager != null) {
                    nextUpdateManager.interrupt();
                    nextUpdateManager = null;
                }
                startGVTTreeBuilder();
                return;
            }
            if (nextDocumentLoader != null) {
                if (nextUpdateManager != null) {
                    nextUpdateManager.interrupt();
                    nextUpdateManager = null;
                }
                startDocumentLoader();
                return;
            }
        }
        public void managerStarted(final UpdateManagerEvent e) {
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        suspendInteractions = false;
                        Object[] dll = updateManagerListeners.toArray();
                        if (dll.length > 0) {
                            for (int i = 0; i < dll.length; i++) {
                                ((UpdateManagerListener)dll[i]).
                                    managerStarted(e);
                            }
                        }
                    }
                });
        }
        public void managerSuspended(final UpdateManagerEvent e) {
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        Object[] dll = updateManagerListeners.toArray();
                        if (dll.length > 0) {
                            for (int i = 0; i < dll.length; i++) {
                                ((UpdateManagerListener)dll[i]).
                                    managerSuspended(e);
                            }
                        }
                    }
                });
        }
        public void managerResumed(final UpdateManagerEvent e) {
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        Object[] dll = updateManagerListeners.toArray();
                        if (dll.length > 0) {
                            for (int i = 0; i < dll.length; i++) {
                                ((UpdateManagerListener)dll[i]).
                                    managerResumed(e);
                            }
                        }
                    }
                });
        }
        public void managerStopped(final UpdateManagerEvent e) {
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        updateManager = null;
                        Object[] dll = updateManagerListeners.toArray();
                        if (dll.length > 0) {
                            for (int i = 0; i < dll.length; i++) {
                                ((UpdateManagerListener)dll[i]).
                                    managerStopped(e);
                            }
                        }
                        if (afterStopRunnable != null) {
                            EventQueue.invokeLater(afterStopRunnable);
                            afterStopRunnable = null;
                            return;
                        }
                        if (nextGVTTreeBuilder != null) {
                            startGVTTreeBuilder();
                            return;
                        }
                        if (nextDocumentLoader != null) {
                            startDocumentLoader();
                            return;
                        }
                    }
                });
        }
        public void updateStarted(final UpdateManagerEvent e) {
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        if (!doubleBufferedRendering) {
                            image = e.getImage();
                        }
                        Object[] dll = updateManagerListeners.toArray();
                        if (dll.length > 0) {
                            for (int i = 0; i < dll.length; i++) {
                                ((UpdateManagerListener)dll[i]).
                                    updateStarted(e);
                            }
                        }
                    }
                });
        }
        public void updateCompleted(final UpdateManagerEvent e) {
            try {
                EventQueue.invokeAndWait(new Runnable() {
                        public void run() {
                            image = e.getImage();
                            if (e.getClearPaintingTransform())
                                paintingTransform = null;
                            List l = e.getDirtyAreas();
                            if (l != null) {
                                Iterator i = l.iterator();
                                while (i.hasNext()) {
                                    Rectangle r = (Rectangle)i.next();
                                    if (updateOverlay != null) {
                                        updateOverlay.addRect(r);
                                        r = getRenderRect();
                                    }
                                    if (doubleBufferedRendering)
                                        repaint(r);
                                    else
                                        paintImmediately(r);
                                }
                                if (updateOverlay != null)
                                    updateOverlay.endUpdate();
                            }
                            suspendInteractions = false;
                        }
                    });
            } catch (Exception ex) {
            }
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        Object[] dll = updateManagerListeners.toArray();
                        if (dll.length > 0) {
                            for (int i = 0; i < dll.length; i++) {
                                ((UpdateManagerListener)dll[i]).
                                    updateCompleted(e);
                            }
                        }
                    }
                });
        }
        public void updateFailed(final UpdateManagerEvent e) {
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        Object[] dll = updateManagerListeners.toArray();
                        if (dll.length > 0) {
                            for (int i = 0; i < dll.length; i++) {
                                ((UpdateManagerListener)dll[i]).
                                    updateFailed(e);
                            }
                        }
                    }
                });
        }
        protected void dispatchKeyTyped(final KeyEvent e) {
            if (!isDynamicDocument) {
                super.dispatchKeyTyped(e);
                return;
            }
            if (updateManager != null && updateManager.isRunning()) {
                updateManager.getUpdateRunnableQueue().invokeLater
                    (new Runnable() {
                        public void run() {
                            eventDispatcher.keyTyped(e);
                        }
                    });
            }
        }
        protected void dispatchKeyPressed(final KeyEvent e) {
            if (!isDynamicDocument) {
                super.dispatchKeyPressed(e);
                return;
            }
            if (updateManager != null && updateManager.isRunning()) {
                updateManager.getUpdateRunnableQueue().invokeLater
                    (new Runnable() {
                        public void run() {
                            eventDispatcher.keyPressed(e);
                        }
                    });
            }
        }
        protected void dispatchKeyReleased(final KeyEvent e) {
            if (!isDynamicDocument) {
                super.dispatchKeyReleased(e);
                return;
            }
            if (updateManager != null && updateManager.isRunning()) {
                updateManager.getUpdateRunnableQueue().invokeLater
                    (new Runnable() {
                        public void run() {
                            eventDispatcher.keyReleased(e);
                        }
                    });
            }
        }
        protected void dispatchMouseClicked(final MouseEvent e) {
            if (!isInteractiveDocument) {
                super.dispatchMouseClicked(e);
                return;
            }
            if (updateManager != null && updateManager.isRunning()) {
                updateManager.getUpdateRunnableQueue().invokeLater
                    (new Runnable() {
                        public void run() {
                            eventDispatcher.mouseClicked(e);
                        }
                    });
            }
        }
        protected void dispatchMousePressed(final MouseEvent e) {
            if (!isDynamicDocument) {
                super.dispatchMousePressed(e);
                return;
            }
            if (updateManager != null && updateManager.isRunning()) {
                updateManager.getUpdateRunnableQueue().invokeLater
                    (new Runnable() {
                        public void run() {
                            eventDispatcher.mousePressed(e);
                        }
                    });
            }
        }
        protected void dispatchMouseReleased(final MouseEvent e) {
            if (!isDynamicDocument) {
                super.dispatchMouseReleased(e);
                return;
            }
            if (updateManager != null && updateManager.isRunning()) {
                updateManager.getUpdateRunnableQueue().invokeLater
                    (new Runnable() {
                        public void run() {
                            eventDispatcher.mouseReleased(e);
                        }
                    });
            }
        }
        protected void dispatchMouseEntered(final MouseEvent e) {
            if (!isInteractiveDocument) {
                super.dispatchMouseEntered(e);
                return;
            }
            if (updateManager != null && updateManager.isRunning()) {
                updateManager.getUpdateRunnableQueue().invokeLater
                    (new Runnable() {
                        public void run() {
                            eventDispatcher.mouseEntered(e);
                        }
                    });
            }
        }
        protected void dispatchMouseExited(final MouseEvent e) {
            if (!isInteractiveDocument) {
                super.dispatchMouseExited(e);
                return;
            }
            if (updateManager != null && updateManager.isRunning()) {
                updateManager.getUpdateRunnableQueue().invokeLater
                    (new Runnable() {
                        public void run() {
                            eventDispatcher.mouseExited(e);
                        }
                    });
            }
        }
        protected void dispatchMouseDragged(MouseEvent e) {
            if (!isDynamicDocument) {
                super.dispatchMouseDragged(e);
                return;
            }
            class MouseDraggedRunnable implements Runnable {
                MouseEvent event;
                MouseDraggedRunnable(MouseEvent evt) {
                    event = evt;
                }
                public void run() {
                    eventDispatcher.mouseDragged(event);
                }
            }
            if (updateManager != null && updateManager.isRunning()) {
                RunnableQueue rq = updateManager.getUpdateRunnableQueue();
                synchronized (rq.getIteratorLock()) {
                    Iterator it = rq.iterator();
                    while (it.hasNext()) {
                        Object next = it.next();
                        if (next instanceof MouseDraggedRunnable) {
                            MouseDraggedRunnable mdr;
                            mdr = (MouseDraggedRunnable)next;
                            MouseEvent mev = mdr.event;
                            if (mev.getModifiers() == e.getModifiers()) {
                                mdr.event = e;
                            }
                            return;
                        }
                    }
                }
                rq.invokeLater(new MouseDraggedRunnable(e));
            }
        }
        protected void dispatchMouseMoved(MouseEvent e) {
            if (!isInteractiveDocument) {
                super.dispatchMouseMoved(e);
                return;
            }
            class MouseMovedRunnable implements Runnable {
                MouseEvent event;
                MouseMovedRunnable(MouseEvent evt) {
                    event = evt;
                }
                public void run() {
                    eventDispatcher.mouseMoved(event);
                }
            }
            if (updateManager != null && updateManager.isRunning()) {
                RunnableQueue rq = updateManager.getUpdateRunnableQueue();
                int i = 0;
                synchronized (rq.getIteratorLock()) {
                    Iterator it = rq.iterator();
                    while (it.hasNext()) {
                        Object next = it.next();
                        if (next instanceof MouseMovedRunnable) {
                            MouseMovedRunnable mmr;
                            mmr = (MouseMovedRunnable)next;
                            MouseEvent mev = mmr.event;
                            if (mev.getModifiers() == e.getModifiers()) {
                                mmr.event = e;
                            }
                            return;
                        }
                        i++;
                    }
                }
                rq.invokeLater(new MouseMovedRunnable(e));
            }
        }
        protected void dispatchMouseWheelMoved(final MouseWheelEvent e) {
            if (!isInteractiveDocument) {
                super.dispatchMouseWheelMoved(e);
                return;
            }
            if (updateManager != null && updateManager.isRunning()) {
                updateManager.getUpdateRunnableQueue().invokeLater
                    (new Runnable() {
                        public void run() {
                            eventDispatcher.mouseWheelMoved(e);
                        }
                    });
            }
        }
    }
    protected UserAgent createUserAgent() {
        return new BridgeUserAgent();
    }
    protected static class BridgeUserAgentWrapper implements UserAgent {
        protected UserAgent userAgent;
        public BridgeUserAgentWrapper(UserAgent ua) {
            userAgent = ua;
        }
        public EventDispatcher getEventDispatcher() {
            if (EventQueue.isDispatchThread()) {
                return userAgent.getEventDispatcher();
            } else {
                class Query implements Runnable {
                    EventDispatcher result;
                    public void run() {
                        result = userAgent.getEventDispatcher();
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public Dimension2D getViewportSize() {
            if (EventQueue.isDispatchThread()) {
                return userAgent.getViewportSize();
            } else {
                class Query implements Runnable {
                    Dimension2D result;
                    public void run() {
                        result = userAgent.getViewportSize();
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public void displayError(final Exception ex) {
            if (EventQueue.isDispatchThread()) {
                userAgent.displayError(ex);
            } else {
                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            userAgent.displayError(ex);
                        }
                    });
            }
        }
        public void displayMessage(final String message) {
            if (EventQueue.isDispatchThread()) {
                userAgent.displayMessage(message);
            } else {
                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            userAgent.displayMessage(message);
                        }
                    });
            }
        }
        public void showAlert(final String message) {
            if (EventQueue.isDispatchThread()) {
                userAgent.showAlert(message);
            } else {
                invokeAndWait(new Runnable() {
                        public void run() {
                            userAgent.showAlert(message);
                        }
                    });
            }
        }
        public String showPrompt(final String message) {
            if (EventQueue.isDispatchThread()) {
                return userAgent.showPrompt(message);
            } else {
                class Query implements Runnable {
                    String result;
                    public void run() {
                        result = userAgent.showPrompt(message);
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public String showPrompt(final String message,
                                 final String defaultValue) {
            if (EventQueue.isDispatchThread()) {
                return userAgent.showPrompt(message, defaultValue);
            } else {
                class Query implements Runnable {
                    String result;
                    public void run() {
                        result = userAgent.showPrompt(message, defaultValue);
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public boolean showConfirm(final String message) {
            if (EventQueue.isDispatchThread()) {
                return userAgent.showConfirm(message);
            } else {
                class Query implements Runnable {
                    boolean result;
                    public void run() {
                        result = userAgent.showConfirm(message);
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public float getPixelUnitToMillimeter() {
            if (EventQueue.isDispatchThread()) {
                return userAgent.getPixelUnitToMillimeter();
            } else {
                class Query implements Runnable {
                    float result;
                    public void run() {
                        result = userAgent.getPixelUnitToMillimeter();
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public float getPixelToMM() { return getPixelUnitToMillimeter(); }
        public String getDefaultFontFamily() {
            if (EventQueue.isDispatchThread()) {
                return userAgent.getDefaultFontFamily();
            } else {
                class Query implements Runnable {
                    String result;
                    public void run() {
                        result = userAgent.getDefaultFontFamily();
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public float getMediumFontSize() {
            if (EventQueue.isDispatchThread()) {
                return userAgent.getMediumFontSize();
            } else {
                class Query implements Runnable {
                    float result;
                    public void run() {
                        result = userAgent.getMediumFontSize();
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public float getLighterFontWeight(float f) {
            if (EventQueue.isDispatchThread()) {
                return userAgent.getLighterFontWeight(f);
            } else {
                final float ff = f;
                class Query implements Runnable {
                    float result;
                    public void run() {
                        result = userAgent.getLighterFontWeight(ff);
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public float getBolderFontWeight(float f) {
            if (EventQueue.isDispatchThread()) {
                return userAgent.getBolderFontWeight(f);
            } else {
                final float ff = f;
                class Query implements Runnable {
                    float result;
                    public void run() {
                        result = userAgent.getBolderFontWeight(ff);
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public String getLanguages() {
            if (EventQueue.isDispatchThread()) {
                return userAgent.getLanguages();
            } else {
                class Query implements Runnable {
                    String result;
                    public void run() {
                        result = userAgent.getLanguages();
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public String getUserStyleSheetURI() {
            if (EventQueue.isDispatchThread()) {
                return userAgent.getUserStyleSheetURI();
            } else {
                class Query implements Runnable {
                    String result;
                    public void run() {
                        result = userAgent.getUserStyleSheetURI();
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public void openLink(final SVGAElement elt) {
            if (EventQueue.isDispatchThread()) {
                userAgent.openLink(elt);
            } else {
                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            userAgent.openLink(elt);
                        }
                    });
            }
        }
        public void setSVGCursor(final Cursor cursor) {
            if (EventQueue.isDispatchThread()) {
                userAgent.setSVGCursor(cursor);
            } else {
                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            userAgent.setSVGCursor(cursor);
                        }
                    });
            }
        }
        public void setTextSelection(final Mark start, final Mark end) {
            if (EventQueue.isDispatchThread()) {
                userAgent.setTextSelection(start, end);
            } else {
                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            userAgent.setTextSelection(start, end);
                        }
                    });
            }
        }
        public void deselectAll() {
            if (EventQueue.isDispatchThread()) {
                userAgent.deselectAll();
            } else {
                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            userAgent.deselectAll();
                        }
                    });
            }
        }
        public String getXMLParserClassName() {
            if (EventQueue.isDispatchThread()) {
                return userAgent.getXMLParserClassName();
            } else {
                class Query implements Runnable {
                    String result;
                    public void run() {
                        result = userAgent.getXMLParserClassName();
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public boolean isXMLParserValidating() {
            if (EventQueue.isDispatchThread()) {
                return userAgent.isXMLParserValidating();
            } else {
                class Query implements Runnable {
                    boolean result;
                    public void run() {
                        result = userAgent.isXMLParserValidating();
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public AffineTransform getTransform() {
            if (EventQueue.isDispatchThread()) {
                return userAgent.getTransform();
            } else {
                class Query implements Runnable {
                    AffineTransform result;
                    public void run() {
                        result = userAgent.getTransform();
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public void setTransform(AffineTransform at) {
            if (EventQueue.isDispatchThread()) {
                userAgent.setTransform(at);
            } else {
                final AffineTransform affine = at;
                class Query implements Runnable {
                    public void run() {
                        userAgent.setTransform(affine);
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
            }
        }
        public String getMedia() {
            if (EventQueue.isDispatchThread()) {
                return userAgent.getMedia();
            } else {
                class Query implements Runnable {
                    String result;
                    public void run() {
                        result = userAgent.getMedia();
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public String getAlternateStyleSheet() {
            if (EventQueue.isDispatchThread()) {
                return userAgent.getAlternateStyleSheet();
            } else {
                class Query implements Runnable {
                    String result;
                    public void run() {
                        result = userAgent.getAlternateStyleSheet();
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public Point getClientAreaLocationOnScreen() {
            if (EventQueue.isDispatchThread()) {
                return userAgent.getClientAreaLocationOnScreen();
            } else {
                class Query implements Runnable {
                    Point result;
                    public void run() {
                        result = userAgent.getClientAreaLocationOnScreen();
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public boolean hasFeature(final String s) {
            if (EventQueue.isDispatchThread()) {
                return userAgent.hasFeature(s);
            } else {
                class Query implements Runnable {
                    boolean result;
                    public void run() {
                        result = userAgent.hasFeature(s);
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public boolean supportExtension(final String s) {
            if (EventQueue.isDispatchThread()) {
                return userAgent.supportExtension(s);
            } else {
                class Query implements Runnable {
                    boolean result;
                    public void run() {
                        result = userAgent.supportExtension(s);
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public void registerExtension(final BridgeExtension ext) {
            if (EventQueue.isDispatchThread()) {
                userAgent.registerExtension(ext);
            } else {
                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            userAgent.registerExtension(ext);
                        }
                    });
            }
        }
        public void handleElement(final Element elt, final Object data) {
            if (EventQueue.isDispatchThread()) {
                userAgent.handleElement(elt, data);
            } else {
                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            userAgent.handleElement(elt, data);
                        }
                    });
            }
        }
        public ScriptSecurity getScriptSecurity(String scriptType,
                                                ParsedURL scriptPURL,
                                                ParsedURL docPURL){
            if (EventQueue.isDispatchThread()) {
                return userAgent.getScriptSecurity(scriptType,
                                                   scriptPURL,
                                                   docPURL);
            } else {
                final String st = scriptType;
                final ParsedURL sPURL= scriptPURL;
                final ParsedURL dPURL= docPURL;
                class Query implements Runnable {
                    ScriptSecurity result;
                    public void run() {
                        result = userAgent.getScriptSecurity(st, sPURL, dPURL);
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public void checkLoadScript(String scriptType,
                                    ParsedURL scriptPURL,
                                    ParsedURL docPURL) throws SecurityException {
            if (EventQueue.isDispatchThread()) {
                userAgent.checkLoadScript(scriptType,
                                          scriptPURL,
                                          docPURL);
            } else {
                final String st = scriptType;
                final ParsedURL sPURL= scriptPURL;
                final ParsedURL dPURL= docPURL;
                class Query implements Runnable {
                    SecurityException se = null;
                    public void run() {
                        try {
                            userAgent.checkLoadScript(st, sPURL, dPURL);
                        } catch (SecurityException se) {
                            this.se = se;
                        }
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                if (q.se != null) {
                    q.se.fillInStackTrace();
                    throw q.se;
                }
            }
        }
        public ExternalResourceSecurity
            getExternalResourceSecurity(ParsedURL resourcePURL,
                                        ParsedURL docPURL){
            if (EventQueue.isDispatchThread()) {
                return userAgent.getExternalResourceSecurity(resourcePURL,
                                                             docPURL);
            } else {
                final ParsedURL rPURL= resourcePURL;
                final ParsedURL dPURL= docPURL;
                class Query implements Runnable {
                    ExternalResourceSecurity result;
                    public void run() {
                        result = userAgent.getExternalResourceSecurity(rPURL, dPURL);
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                return q.result;
            }
        }
        public void
            checkLoadExternalResource(ParsedURL resourceURL,
                                      ParsedURL docURL) throws SecurityException {
            if (EventQueue.isDispatchThread()) {
                userAgent.checkLoadExternalResource(resourceURL,
                                                    docURL);
            } else {
                final ParsedURL rPURL= resourceURL;
                final ParsedURL dPURL= docURL;
                class Query implements Runnable {
                    SecurityException se;
                    public void run() {
                        try {
                            userAgent.checkLoadExternalResource(rPURL, dPURL);
                        } catch (SecurityException se) {
                            this.se = se;
                        }
                    }
                }
                Query q = new Query();
                invokeAndWait(q);
                if (q.se != null) {
                    q.se.fillInStackTrace();
                    throw q.se;
                }
            }
        }
        public SVGDocument getBrokenLinkDocument(final Element e,
                                                 final String url,
                                                 final String msg) {
            if (EventQueue.isDispatchThread())
                return userAgent.getBrokenLinkDocument(e, url, msg);
            class Query implements Runnable {
                SVGDocument doc;
                RuntimeException rex = null;
                public void run() {
                    try {
                        doc = userAgent.getBrokenLinkDocument(e, url, msg);
                    } catch (RuntimeException re) { rex = re; }
                }
            }
            Query q = new Query();
            invokeAndWait(q);
            if (q.rex != null) throw q.rex;
            return q.doc;
        }
        protected void invokeAndWait(Runnable r) {
            try {
                EventQueue.invokeAndWait(r);
            } catch (Exception e) {
            }
        }
        public void loadDocument(String url) {
            userAgent.loadDocument(url);
        }
    }
    protected class BridgeUserAgent implements UserAgent {
        protected BridgeUserAgent() {
        }
        public Dimension2D getViewportSize() {
            return getSize();
        }
        public EventDispatcher getEventDispatcher() {
            return JSVGComponent.this.eventDispatcher;
        }
        public void displayError(String message) {
            if (svgUserAgent != null) {
                svgUserAgent.displayError(message);
            }
        }
        public void displayError(Exception ex) {
            if (svgUserAgent != null) {
                svgUserAgent.displayError(ex);
            }
        }
        public void displayMessage(String message) {
            if (svgUserAgent != null) {
                svgUserAgent.displayMessage(message);
            }
        }
        public void showAlert(String message) {
            if (svgUserAgent != null) {
                svgUserAgent.showAlert(message);
                return;
            }
            JSVGComponent.this.showAlert(message);
        }
        public String showPrompt(String message) {
            if (svgUserAgent != null) {
                return svgUserAgent.showPrompt(message);
            }
            return JSVGComponent.this.showPrompt(message);
        }
        public String showPrompt(String message, String defaultValue) {
            if (svgUserAgent != null) {
                return svgUserAgent.showPrompt(message, defaultValue);
            }
            return JSVGComponent.this.showPrompt
                (message, defaultValue);
        }
        public boolean showConfirm(String message) {
            if (svgUserAgent != null) {
                return svgUserAgent.showConfirm(message);
            }
            return JSVGComponent.this.showConfirm(message);
        }
        public float getPixelUnitToMillimeter() {
            if (svgUserAgent != null) {
                return svgUserAgent.getPixelUnitToMillimeter();
            }
            return 0.264583333333333333333f; 
        }
        public float getPixelToMM() { return getPixelUnitToMillimeter(); }
        public String getDefaultFontFamily() {
            if (svgUserAgent != null) {
                return svgUserAgent.getDefaultFontFamily();
            }
            return "Arial, Helvetica, sans-serif";
        }
        public float getMediumFontSize() {
            if (svgUserAgent != null) {
                return svgUserAgent.getMediumFontSize();
            }
            return 9f * 25.4f / (72f * getPixelUnitToMillimeter());
        }
        public float getLighterFontWeight(float f) {
            if (svgUserAgent != null) {
                return svgUserAgent.getLighterFontWeight(f);
            }
            int weight = ((int)((f+50)/100))*100;
            switch (weight) {
            case 100: return 100;
            case 200: return 100;
            case 300: return 200;
            case 400: return 300;
            case 500: return 400;
            case 600: return 400;
            case 700: return 400;
            case 800: return 400;
            case 900: return 400;
            default:
                throw new IllegalArgumentException("Bad Font Weight: " + f);
            }
        }
        public float getBolderFontWeight(float f) {
            if (svgUserAgent != null) {
                return svgUserAgent.getBolderFontWeight(f);
            }
            int weight = ((int)((f+50)/100))*100;
            switch (weight) {
            case 100: return 600;
            case 200: return 600;
            case 300: return 600;
            case 400: return 600;
            case 500: return 600;
            case 600: return 700;
            case 700: return 800;
            case 800: return 900;
            case 900: return 900;
            default:
                throw new IllegalArgumentException("Bad Font Weight: " + f);
            }
        }
        public String getLanguages() {
            if (svgUserAgent != null) {
                return svgUserAgent.getLanguages();
            }
            return "en";
        }
        public String getUserStyleSheetURI() {
            if (svgUserAgent != null) {
                return svgUserAgent.getUserStyleSheetURI();
            }
            return null;
        }
        public void openLink(SVGAElement elt) {
            String show = XLinkSupport.getXLinkShow(elt);
            String href = elt.getHref().getAnimVal();
            if (show.equals("new")) {
                fireLinkActivatedEvent(elt, href);
                if (svgUserAgent != null) {
                    String oldURI = svgDocument.getURL();
                    ParsedURL newURI = null;
                    if (elt.getOwnerDocument() != svgDocument) {
                        SVGDocument doc = (SVGDocument)elt.getOwnerDocument();
                        href = new ParsedURL(doc.getURL(), href).toString();
                    }
                    newURI = new ParsedURL(oldURI, href);
                    href = newURI.toString();
                    svgUserAgent.openLink(href, true);
                } else {
                    JSVGComponent.this.loadSVGDocument(href);
                }
                return;
            }
            ParsedURL newURI = new ParsedURL
                (((SVGDocument)elt.getOwnerDocument()).getURL(), href);
            href = newURI.toString();
            if (svgDocument != null) {
                ParsedURL oldURI = new ParsedURL(svgDocument.getURL());
                if (newURI.sameFile(oldURI)) {
                    String s = newURI.getRef();
                    if ((fragmentIdentifier != s) &&
                        ((s == null) || (!s.equals(fragmentIdentifier)))) {
                        fragmentIdentifier = s;
                        if (computeRenderingTransform())
                            scheduleGVTRendering();
                    }
                    fireLinkActivatedEvent(elt, href);
                    return;
                }
            }
            fireLinkActivatedEvent(elt, href);
            if (svgUserAgent != null) {
                svgUserAgent.openLink(href, false);
            } else {
                JSVGComponent.this.loadSVGDocument(href);
            }
        }
        protected void fireLinkActivatedEvent(SVGAElement elt, String href) {
            Object[] ll = linkActivationListeners.toArray();
            if (ll.length > 0) {
                LinkActivationEvent ev;
                ev = new LinkActivationEvent(JSVGComponent.this, elt, href);
                for (int i = 0; i < ll.length; i++) {
                    LinkActivationListener l = (LinkActivationListener)ll[i];
                    l.linkActivated(ev);
                }
            }
        }
        public void setSVGCursor(Cursor cursor) {
            if (cursor != JSVGComponent.this.getCursor())
                JSVGComponent.this.setCursor(cursor);
        }
        public void setTextSelection(Mark start, Mark end) {
            JSVGComponent.this.select(start, end);
        }
        public void deselectAll() {
            JSVGComponent.this.deselectAll();
        }
        public String getXMLParserClassName() {
            if (svgUserAgent != null) {
                return svgUserAgent.getXMLParserClassName();
            }
            return XMLResourceDescriptor.getXMLParserClassName();
        }
        public boolean isXMLParserValidating() {
            if (svgUserAgent != null) {
                return svgUserAgent.isXMLParserValidating();
            }
            return false;
        }
        public AffineTransform getTransform() {
            return JSVGComponent.this.renderingTransform;
        }
        public void setTransform(AffineTransform at) {
            JSVGComponent.this.setRenderingTransform(at);
        }
        public String getMedia() {
            if (svgUserAgent != null) {
                return svgUserAgent.getMedia();
            }
            return "screen";
        }
        public String getAlternateStyleSheet() {
            if (svgUserAgent != null) {
                return svgUserAgent.getAlternateStyleSheet();
            }
            return null;
        }
        public Point getClientAreaLocationOnScreen() {
            return getLocationOnScreen();
        }
        public boolean hasFeature(String s) {
            return FEATURES.contains(s);
        }
        protected Map extensions = new HashMap();
        public boolean supportExtension(String s) {
            if ((svgUserAgent != null) &&
                (svgUserAgent.supportExtension(s)))
                return true;
            return extensions.containsKey(s);
        }
        public void registerExtension(BridgeExtension ext) {
            Iterator i = ext.getImplementedExtensions();
            while (i.hasNext())
                extensions.put(i.next(), ext);
        }
        public void handleElement(Element elt, Object data) {
            if (svgUserAgent != null) {
                svgUserAgent.handleElement(elt, data);
            }
        }
        public ScriptSecurity getScriptSecurity(String scriptType,
                                                ParsedURL scriptURL,
                                                ParsedURL docURL){
            if (svgUserAgent != null){
                return svgUserAgent.getScriptSecurity(scriptType,
                                                      scriptURL,
                                                      docURL);
            } else {
                return new DefaultScriptSecurity(scriptType,
                                                 scriptURL,
                                                 docURL);
            }
        }
        public void checkLoadScript(String scriptType,
                                    ParsedURL scriptURL,
                                    ParsedURL docURL) throws SecurityException {
            if (svgUserAgent != null) {
                svgUserAgent.checkLoadScript(scriptType,
                                             scriptURL,
                                             docURL);
            } else {
                ScriptSecurity s = getScriptSecurity(scriptType,
                                                     scriptURL,
                                                     docURL);
                if (s != null) {
                    s.checkLoadScript();
                }
            }
        }
        public ExternalResourceSecurity
            getExternalResourceSecurity(ParsedURL resourceURL,
                                        ParsedURL docURL){
            if (svgUserAgent != null){
                return svgUserAgent.getExternalResourceSecurity(resourceURL,
                                                                docURL);
            } else {
                return new RelaxedExternalResourceSecurity(resourceURL,
                                                           docURL);
            }
        }
        public void
            checkLoadExternalResource(ParsedURL resourceURL,
                                      ParsedURL docURL) throws SecurityException {
            if (svgUserAgent != null) {
                svgUserAgent.checkLoadExternalResource(resourceURL,
                                                       docURL);
            } else {
                ExternalResourceSecurity s
                    =  getExternalResourceSecurity(resourceURL, docURL);
                if (s != null) {
                    s.checkLoadExternalResource();
                }
            }
        }
        public SVGDocument getBrokenLinkDocument(Element e,
                                                 String url,
                                                 String message) {
            Class cls = JSVGComponent.class;
            URL blURL = cls.getResource("resources/BrokenLink.svg");
            if (blURL == null)
                throw new BridgeException
                    (bridgeContext, e, ErrorConstants.ERR_URI_IMAGE_BROKEN,
                     new Object[] { url, message });
            DocumentLoader loader  = bridgeContext.getDocumentLoader();
            SVGDocument    doc = null;
            try {
                doc  = (SVGDocument)loader.loadDocument(blURL.toString());
                if (doc == null) return doc;
                DOMImplementation impl;
                impl = SVGDOMImplementation.getDOMImplementation();
                doc  = (SVGDocument)DOMUtilities.deepCloneDocument(doc, impl);
                String title;
                Element infoE, titleE, descE;
                infoE = doc.getElementById("__More_About");
                if (infoE == null) return doc;
                titleE = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI,
                                            SVGConstants.SVG_TITLE_TAG);
                title = Messages.formatMessage(BROKEN_LINK_TITLE, null);
                titleE.appendChild(doc.createTextNode(title));
                descE = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI,
                                           SVGConstants.SVG_DESC_TAG);
                descE.appendChild(doc.createTextNode(message));
                infoE.insertBefore(descE, infoE.getFirstChild());
                infoE.insertBefore(titleE, descE);
            } catch (Exception ex) {
                throw new BridgeException
                    (bridgeContext, e, ex, ErrorConstants.ERR_URI_IMAGE_BROKEN,
                     new Object[] {url, message });
            }
            return doc;
        }
        public void loadDocument(String url) {
            JSVGComponent.this.loadSVGDocument(url);
        }
    }
    protected static final Set FEATURES = new HashSet();
    static {
        SVGFeatureStrings.addSupportedFeatureStrings(FEATURES);
    }
}
