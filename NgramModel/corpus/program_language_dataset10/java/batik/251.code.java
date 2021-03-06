package org.apache.batik.bridge.svg12;
import java.util.Iterator;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeUpdateHandler;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.ScriptingEnvironment;
import org.apache.batik.bridge.URIResolver;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.events.EventSupport;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.svg12.XBLEventSupport;
import org.apache.batik.dom.svg12.XBLOMShadowTreeElement;
import org.apache.batik.dom.xbl.NodeXBL;
import org.apache.batik.dom.xbl.XBLManager;
import org.apache.batik.script.Interpreter;
import org.apache.batik.script.InterpreterPool;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;
public class SVG12BridgeContext extends BridgeContext {
    protected XBLBindingListener bindingListener;
    protected XBLContentListener contentListener;
    protected EventTarget mouseCaptureTarget;
    protected boolean mouseCaptureSendAll;
    protected boolean mouseCaptureAutoRelease;
    public SVG12BridgeContext(UserAgent userAgent) {
        super(userAgent);
    }
    public SVG12BridgeContext(UserAgent userAgent,
                              DocumentLoader loader) {
        super(userAgent, loader);
    }
    public SVG12BridgeContext(UserAgent userAgent,
                              InterpreterPool interpreterPool,
                              DocumentLoader documentLoader) {
        super(userAgent, interpreterPool, documentLoader);
    }
    public URIResolver createURIResolver(SVGDocument doc, DocumentLoader dl) {
        return new SVG12URIResolver(doc, dl);
    }
    public void addGVTListener(Document doc) {
        SVG12BridgeEventSupport.addGVTListener(this, doc);
    }
    public void dispose() {
        clearChildContexts();
        synchronized (eventListenerSet) {
            Iterator iter = eventListenerSet.iterator();
            while (iter.hasNext()) {
                EventListenerMememto m = (EventListenerMememto)iter.next();
                NodeEventTarget et = m.getTarget();
                EventListener   el = m.getListener();
                boolean         uc = m.getUseCapture();
                String          t  = m.getEventType();
                boolean         in = m.getNamespaced();
                if (et == null || el == null || t == null) {
                    continue;
                }
                if (m instanceof ImplementationEventListenerMememto) {
                    String ns = m.getNamespaceURI();
                    Node nde = (Node)et;
                    AbstractNode n = (AbstractNode)nde.getOwnerDocument();
                    if (n != null) {
                        XBLEventSupport es;
                        es = (XBLEventSupport) n.initializeEventSupport();
                        es.removeImplementationEventListenerNS(ns, t, el, uc);
                    }
                } else if (in) {
                    String ns = m.getNamespaceURI();
                    et.removeEventListenerNS(ns, t, el, uc);
                } else {
                    et.removeEventListener(t, el, uc);
                }
            }
        }
        if (document != null) {
            removeDOMListeners();
            removeBindingListener();
        }
        if (animationEngine != null) {
            animationEngine.dispose();
            animationEngine = null;
        }
        Iterator iter = interpreterMap.values().iterator();
        while (iter.hasNext()) {
            Interpreter interpreter = (Interpreter)iter.next();
            if (interpreter != null)
                interpreter.dispose();
        }
        interpreterMap.clear();
        if (focusManager != null) {
            focusManager.dispose();
        }
    }
    public void addBindingListener() {
        AbstractDocument doc = (AbstractDocument) document;
        DefaultXBLManager xm = (DefaultXBLManager) doc.getXBLManager();
        if (xm != null) {
            bindingListener = new XBLBindingListener();
            xm.addBindingListener(bindingListener);
            contentListener = new XBLContentListener();
            xm.addContentSelectionChangedListener(contentListener);
        }
    }
    public void removeBindingListener() {
        AbstractDocument doc = (AbstractDocument) document;
        XBLManager xm = doc.getXBLManager();
        if (xm instanceof DefaultXBLManager) {
            DefaultXBLManager dxm = (DefaultXBLManager) xm;
            dxm.removeBindingListener(bindingListener);
            dxm.removeContentSelectionChangedListener(contentListener);
        }
    }
    public void addDOMListeners() {
        SVGOMDocument doc = (SVGOMDocument)document;
        XBLEventSupport evtSupport
            = (XBLEventSupport) doc.initializeEventSupport();
        domAttrModifiedEventListener
            = new EventListenerWrapper(new DOMAttrModifiedEventListener());
        evtSupport.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMAttrModified",
             domAttrModifiedEventListener, true);
        domNodeInsertedEventListener
            = new EventListenerWrapper(new DOMNodeInsertedEventListener());
        evtSupport.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeInserted",
             domNodeInsertedEventListener, true);
        domNodeRemovedEventListener
            = new EventListenerWrapper(new DOMNodeRemovedEventListener());
        evtSupport.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeRemoved",
             domNodeRemovedEventListener, true);
        domCharacterDataModifiedEventListener = 
            new EventListenerWrapper(new DOMCharacterDataModifiedEventListener());
        evtSupport.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMCharacterDataModified",
             domCharacterDataModifiedEventListener, true);
        animatedAttributeListener = new AnimatedAttrListener();
        doc.addAnimatedAttributeListener(animatedAttributeListener);
        focusManager = new SVG12FocusManager(document);
        CSSEngine cssEngine = doc.getCSSEngine();
        cssPropertiesChangedListener = new CSSPropertiesChangedListener();
        cssEngine.addCSSEngineListener(cssPropertiesChangedListener);
    }
    public void addUIEventListeners(Document doc) {
        EventTarget evtTarget = (EventTarget)doc.getDocumentElement();
        AbstractNode n = (AbstractNode) evtTarget;
        XBLEventSupport evtSupport
            = (XBLEventSupport) n.initializeEventSupport();
        EventListener domMouseOverListener
            = new EventListenerWrapper(new DOMMouseOverEventListener());
        evtSupport.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             SVGConstants.SVG_EVENT_MOUSEOVER,
             domMouseOverListener, true);
        storeImplementationEventListenerNS
            (evtTarget,
             XMLConstants.XML_EVENTS_NAMESPACE_URI,
             SVGConstants.SVG_EVENT_MOUSEOVER,
             domMouseOverListener, true);
        EventListener domMouseOutListener
            = new EventListenerWrapper(new DOMMouseOutEventListener());
        evtSupport.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             SVGConstants.SVG_EVENT_MOUSEOUT,
             domMouseOutListener, true);
        storeImplementationEventListenerNS
            (evtTarget,
             XMLConstants.XML_EVENTS_NAMESPACE_URI,
             SVGConstants.SVG_EVENT_MOUSEOUT,
             domMouseOutListener, true);
    }
    public void removeUIEventListeners(Document doc) {
        EventTarget evtTarget = (EventTarget)doc.getDocumentElement();
        AbstractNode n = (AbstractNode) evtTarget;
        XBLEventSupport es = (XBLEventSupport) n.initializeEventSupport();
        synchronized (eventListenerSet) {
            Iterator i = eventListenerSet.iterator();
            while (i.hasNext()) {
                EventListenerMememto elm = (EventListenerMememto)i.next();
                NodeEventTarget et = elm.getTarget();
                if (et == evtTarget) {
                    EventListener el = elm.getListener();
                    boolean       uc = elm.getUseCapture();
                    String        t  = elm.getEventType();
                    boolean       in = elm.getNamespaced();
                    if (et == null || el == null || t == null) {
                        continue;
                    }
                    if (elm instanceof ImplementationEventListenerMememto) {
                        String ns = elm.getNamespaceURI();
                        es.removeImplementationEventListenerNS(ns, t, el, uc);
                    } else if (in) {
                        String ns = elm.getNamespaceURI();
                        et.removeEventListenerNS(ns, t, el, uc);
                    } else {
                        et.removeEventListener(t, el, uc);
                    }
                }
            }
        }
    }
    protected void removeDOMListeners() {
        SVGOMDocument doc = (SVGOMDocument)document;
        doc.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMAttrModified",
             domAttrModifiedEventListener, true);
        doc.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeInserted",
             domNodeInsertedEventListener, true);
        doc.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeRemoved",
             domNodeRemovedEventListener, true);
        doc.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMCharacterDataModified",
             domCharacterDataModifiedEventListener, true);
        doc.removeAnimatedAttributeListener(animatedAttributeListener);
        CSSEngine cssEngine = doc.getCSSEngine();
        if (cssEngine != null) {
            cssEngine.removeCSSEngineListener
                (cssPropertiesChangedListener);
            cssEngine.dispose();
            doc.setCSSEngine(null);
        }
    }
    protected void storeImplementationEventListenerNS(EventTarget t,
                                                      String ns,
                                                      String s,
                                                      EventListener l,
                                                      boolean b) {
        synchronized (eventListenerSet) {
            ImplementationEventListenerMememto m
                = new ImplementationEventListenerMememto(t, ns, s, l, b, this);
            eventListenerSet.add(m);
        }
    }
    public BridgeContext createSubBridgeContext(SVGOMDocument newDoc) {
        CSSEngine eng = newDoc.getCSSEngine();
        if (eng != null) {
            return (BridgeContext)newDoc.getCSSEngine().getCSSContext();
        }
        BridgeContext subCtx = super.createSubBridgeContext(newDoc);
        if (isDynamic() && subCtx.isDynamic()) {
            setUpdateManager(subCtx, updateManager);
            if (updateManager != null) {
                ScriptingEnvironment se;
                if (newDoc.isSVG12()) {
                    se = new SVG12ScriptingEnvironment(subCtx);
                } else {
                    se = new ScriptingEnvironment(subCtx);
                }
                se.loadScripts();
                se.dispatchSVGLoadEvent();
                if (newDoc.isSVG12()) {
                    DefaultXBLManager xm =
                        new DefaultXBLManager(newDoc, subCtx);
                    setXBLManager(subCtx, xm);
                    newDoc.setXBLManager(xm);
                    xm.startProcessing();
                }
            }
        }
        return subCtx;
    }
    public void startMouseCapture(EventTarget target, boolean sendAll,
                                  boolean autoRelease) {
        mouseCaptureTarget = target;
        mouseCaptureSendAll = sendAll;
        mouseCaptureAutoRelease = autoRelease;
    }
    public void stopMouseCapture() {
        mouseCaptureTarget = null;
    }
    protected static class ImplementationEventListenerMememto
            extends EventListenerMememto {
        public ImplementationEventListenerMememto(EventTarget t,
                                                  String s,
                                                  EventListener l,
                                                  boolean b,
                                                  BridgeContext c) {
            super(t, s, l, b, c);
        }
        public ImplementationEventListenerMememto(EventTarget t,
                                                  String n,
                                                  String s,
                                                  EventListener l,
                                                  boolean b,
                                                  BridgeContext c) {
            super(t, n, s, l, b, c);
        }
    }
    protected class EventListenerWrapper implements EventListener {
        protected EventListener listener;
        public EventListenerWrapper(EventListener l) {
            listener = l;
        }
        public void handleEvent(Event evt) {
            listener.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
        }
        public String toString() {
            return super.toString() + " [wrapping " + listener.toString() + "]";
        }
    }
    protected class XBLBindingListener implements BindingListener {
        public void bindingChanged(Element bindableElement,
                                   Element shadowTree) {
            BridgeUpdateHandler h = getBridgeUpdateHandler(bindableElement);
            if (h instanceof SVG12BridgeUpdateHandler) {
                SVG12BridgeUpdateHandler h12 = (SVG12BridgeUpdateHandler) h;
                try {
                    h12.handleBindingEvent(bindableElement, shadowTree);
                } catch (Exception e) {
                    userAgent.displayError(e);
                }
            }
        }
    }
    protected class XBLContentListener
            implements ContentSelectionChangedListener {
        public void contentSelectionChanged(ContentSelectionChangedEvent csce) {
            Element e = (Element) csce.getContentElement().getParentNode();
            if (e instanceof XBLOMShadowTreeElement) {
                e = ((NodeXBL) e).getXblBoundElement();
            }
            BridgeUpdateHandler h = getBridgeUpdateHandler(e);
            if (h instanceof SVG12BridgeUpdateHandler) {
                SVG12BridgeUpdateHandler h12 = (SVG12BridgeUpdateHandler) h;
                try {
                    h12.handleContentSelectionChangedEvent(csce);
                } catch (Exception ex) {
                    userAgent.displayError(ex);
                }
            }
        }
    }
}
