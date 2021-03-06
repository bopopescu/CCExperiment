package org.apache.batik.bridge;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.batik.dom.AbstractElement;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.svg.SVGOMScriptElement;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.SAXDocumentFactory;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.bridge.Location;
import org.apache.batik.script.Interpreter;
import org.apache.batik.script.InterpreterException;
import org.apache.batik.script.ScriptEventWrapper;
import org.apache.batik.util.EncodingUtilities;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.RunnableQueue;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.MutationEvent;
import org.w3c.dom.svg.SVGDocument;
public class ScriptingEnvironment extends BaseScriptingEnvironment {
    public static final String [] SVG_EVENT_ATTRS = {
        "onabort",     
        "onerror",     
        "onresize",    
        "onscroll",    
        "onunload",    
        "onzoom",      
        "onbegin",     
        "onend",       
        "onrepeat",    
        "onfocusin",   
        "onfocusout",  
        "onactivate",  
        "onclick",     
        "onmousedown", 
        "onmouseup",   
        "onmouseover", 
        "onmouseout",  
        "onmousemove", 
        "onkeypress",  
        "onkeydown",   
        "onkeyup"      
    };
    public static final String [] SVG_DOM_EVENT = {
        "SVGAbort",    
        "SVGError",    
        "SVGResize",   
        "SVGScroll",   
        "SVGUnload",   
        "SVGZoom",     
        "beginEvent",  
        "endEvent",    
        "repeatEvent", 
        "DOMFocusIn",  
        "DOMFocusOut", 
        "DOMActivate", 
        "click",       
        "mousedown",   
        "mouseup",     
        "mouseover",   
        "mouseout",    
        "mousemove",   
        "keypress",    
        "keydown",     
        "keyup"        
    };
    protected Timer timer = new Timer(true);
    protected UpdateManager updateManager;
    protected RunnableQueue updateRunnableQueue;
    protected EventListener domNodeInsertedListener;
    protected EventListener domNodeRemovedListener;
    protected EventListener domAttrModifiedListener;
    protected EventListener svgAbortListener =
        new ScriptingEventListener("onabort");
    protected EventListener svgErrorListener =
        new ScriptingEventListener("onerror");
    protected EventListener svgResizeListener =
        new ScriptingEventListener("onresize");
    protected EventListener svgScrollListener =
        new ScriptingEventListener("onscroll");
    protected EventListener svgUnloadListener =
        new ScriptingEventListener("onunload");
    protected EventListener svgZoomListener =
        new ScriptingEventListener("onzoom");
    protected EventListener beginListener =
        new ScriptingEventListener("onbegin");
    protected EventListener endListener =
        new ScriptingEventListener("onend");
    protected EventListener repeatListener =
        new ScriptingEventListener("onrepeat");
    protected EventListener focusinListener =
        new ScriptingEventListener("onfocusin");
    protected EventListener focusoutListener =
        new ScriptingEventListener("onfocusout");
    protected EventListener activateListener =
        new ScriptingEventListener("onactivate");
    protected EventListener clickListener =
        new ScriptingEventListener("onclick");
    protected EventListener mousedownListener =
        new ScriptingEventListener("onmousedown");
    protected EventListener mouseupListener =
        new ScriptingEventListener("onmouseup");
    protected EventListener mouseoverListener =
        new ScriptingEventListener("onmouseover");
    protected EventListener mouseoutListener =
        new ScriptingEventListener("onmouseout");
    protected EventListener mousemoveListener =
        new ScriptingEventListener("onmousemove");
    protected EventListener keypressListener =
        new ScriptingEventListener("onkeypress");
    protected EventListener keydownListener =
        new ScriptingEventListener("onkeydown");
    protected EventListener keyupListener =
        new ScriptingEventListener("onkeyup");
    protected EventListener [] listeners = {
        svgAbortListener,
        svgErrorListener,
        svgResizeListener,
        svgScrollListener,
        svgUnloadListener,
        svgZoomListener,
        beginListener,
        endListener,
        repeatListener,
        focusinListener,
        focusoutListener,
        activateListener,
        clickListener,
        mousedownListener,
        mouseupListener,
        mouseoverListener,
        mouseoutListener,
        mousemoveListener,
        keypressListener,
        keydownListener,
        keyupListener
    };
    Map attrToDOMEvent = new HashMap(SVG_EVENT_ATTRS.length);
    Map attrToListener = new HashMap(SVG_EVENT_ATTRS.length);
    {
        for (int i = 0; i < SVG_EVENT_ATTRS.length; i++) {
            attrToDOMEvent.put(SVG_EVENT_ATTRS[i], SVG_DOM_EVENT[i]);
            attrToListener.put(SVG_EVENT_ATTRS[i], listeners[i]);
        }
    }
    public ScriptingEnvironment(BridgeContext ctx) {
        super(ctx);
        updateManager = ctx.getUpdateManager();
        updateRunnableQueue = updateManager.getUpdateRunnableQueue();
        addScriptingListeners(document.getDocumentElement());
        addDocumentListeners();
    }
    protected void addDocumentListeners() {
        domNodeInsertedListener = new DOMNodeInsertedListener();
        domNodeRemovedListener = new DOMNodeRemovedListener();
        domAttrModifiedListener = new DOMAttrModifiedListener();
        NodeEventTarget et = (NodeEventTarget) document;
        et.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeInserted",
             domNodeInsertedListener, false, null);
        et.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeRemoved",
             domNodeRemovedListener, false, null);
        et.addEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMAttrModified",
             domAttrModifiedListener, false, null);
    }
    protected void removeDocumentListeners() {
        NodeEventTarget et = (NodeEventTarget) document;
        et.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeInserted",
             domNodeInsertedListener, false);
        et.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMNodeRemoved",
             domNodeRemovedListener, false);
        et.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMAttrModified",
             domAttrModifiedListener, false);
    }
    protected org.apache.batik.script.Window createWindow(Interpreter interp,
                                                          String lang) {
        return new Window(interp, lang);
    }
    public void runEventHandler(String script, Event evt,
                                String lang, String desc) {
        Interpreter interpreter = getInterpreter(lang);
        if (interpreter == null)
            return;
        try {
            checkCompatibleScriptURL(lang, docPURL);
            Object event;
            if (evt instanceof ScriptEventWrapper) {
                event = ((ScriptEventWrapper) evt).getEventObject();
            } else {
                event = evt;
            }
            interpreter.bindObject(EVENT_NAME, event);
            interpreter.bindObject(ALTERNATE_EVENT_NAME, event);
            interpreter.evaluate(new StringReader(script), desc);
        } catch (IOException ioe) {
        } catch (InterpreterException ie) {
            handleInterpreterException(ie);
        } catch (SecurityException se) {
            handleSecurityException(se);
        }
    }
    public void interrupt() {
        timer.cancel();
        removeScriptingListeners(document.getDocumentElement());
        removeDocumentListeners();
    }
    public void addScriptingListeners(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            addScriptingListenersOn((Element) node);
        }
        for (Node n = node.getFirstChild();
             n != null;
             n = n.getNextSibling()) {
            addScriptingListeners(n);
        }
    }
    protected void addScriptingListenersOn(Element elt) {
        NodeEventTarget target = (NodeEventTarget)elt;
        if (SVGConstants.SVG_NAMESPACE_URI.equals(elt.getNamespaceURI())) {
            if (SVGConstants.SVG_SVG_TAG.equals(elt.getLocalName())) {
                if (elt.hasAttributeNS(null, "onabort")) {
                    target.addEventListenerNS
                        (XMLConstants.XML_EVENTS_NAMESPACE_URI, "SVGAbort",
                         svgAbortListener, false, null);
                }
                if (elt.hasAttributeNS(null, "onerror")) {
                    target.addEventListenerNS
                        (XMLConstants.XML_EVENTS_NAMESPACE_URI, "SVGError",
                         svgErrorListener, false, null);
                }
                if (elt.hasAttributeNS(null, "onresize")) {
                    target.addEventListenerNS
                        (XMLConstants.XML_EVENTS_NAMESPACE_URI, "SVGResize",
                         svgResizeListener, false, null);
                }
                if (elt.hasAttributeNS(null, "onscroll")) {
                    target.addEventListenerNS
                        (XMLConstants.XML_EVENTS_NAMESPACE_URI, "SVGScroll",
                         svgScrollListener, false, null);
                }
                if (elt.hasAttributeNS(null, "onunload")) {
                    target.addEventListenerNS
                        (XMLConstants.XML_EVENTS_NAMESPACE_URI, "SVGUnload",
                         svgUnloadListener, false, null);
                }
                if (elt.hasAttributeNS(null, "onzoom")) {
                    target.addEventListenerNS
                        (XMLConstants.XML_EVENTS_NAMESPACE_URI, "SVGZoom",
                         svgZoomListener, false, null);
                }
            } else {
                String name = elt.getLocalName();
                if (name.equals(SVGConstants.SVG_SET_TAG) ||
                    name.startsWith("animate")) {
                    if (elt.hasAttributeNS(null, "onbegin")) {
                        target.addEventListenerNS
                            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "beginEvent",
                             beginListener, false, null);
                    }
                    if (elt.hasAttributeNS(null, "onend")) {
                        target.addEventListenerNS
                            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "endEvent",
                             endListener, false, null);
                    }
                    if (elt.hasAttributeNS(null, "onrepeat")) {
                        target.addEventListenerNS
                            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "repeatEvent",
                             repeatListener, false, null);
                    }
                    return;
                }
            }
        }
        if (elt.hasAttributeNS(null, "onfocusin")) {
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMFocusIn",
                 focusinListener, false, null);
        }
        if (elt.hasAttributeNS(null, "onfocusout")) {
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMFocusOut",
                 focusoutListener, false, null);
        }
        if (elt.hasAttributeNS(null, "onactivate")) {
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMActivate",
                 activateListener, false, null);
        }
        if (elt.hasAttributeNS(null, "onclick")) {
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "click",
                 clickListener, false, null);
        }
        if (elt.hasAttributeNS(null, "onmousedown")) {
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "mousedown",
                 mousedownListener, false, null);
        }
        if (elt.hasAttributeNS(null, "onmouseup")) {
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "mouseup",
                 mouseupListener, false, null);
        }
        if (elt.hasAttributeNS(null, "onmouseover")) {
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "mouseover",
                 mouseoverListener, false, null);
        }
        if (elt.hasAttributeNS(null, "onmouseout")) {
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "mouseout",
                 mouseoutListener, false, null);
        }
        if (elt.hasAttributeNS(null, "onmousemove")) {
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "mousemove",
                 mousemoveListener, false, null);
        }
        if (elt.hasAttributeNS(null, "onkeypress")) {
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "keypress",
                 keypressListener, false, null);
        }
        if (elt.hasAttributeNS(null, "onkeydown")) {
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "keydown",
                 keydownListener, false, null);
        }
        if (elt.hasAttributeNS(null, "onkeyup")) {
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "keyup",
                 keyupListener, false, null);
        }
    }
    protected void removeScriptingListeners(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            removeScriptingListenersOn((Element) node);
        }
        for (Node n = node.getFirstChild();
             n != null;
             n = n.getNextSibling()) {
            removeScriptingListeners(n);
        }
    }
    protected void removeScriptingListenersOn(Element elt) {
        NodeEventTarget target = (NodeEventTarget)elt;
        if (SVGConstants.SVG_NAMESPACE_URI.equals(elt.getNamespaceURI())) {
            if (SVGConstants.SVG_SVG_TAG.equals(elt.getLocalName())) {
                target.removeEventListenerNS
                    (XMLConstants.XML_EVENTS_NAMESPACE_URI, "SVGAbort",
                     svgAbortListener, false);
                target.removeEventListenerNS
                    (XMLConstants.XML_EVENTS_NAMESPACE_URI, "SVGError",
                     svgErrorListener, false);
                target.removeEventListenerNS
                    (XMLConstants.XML_EVENTS_NAMESPACE_URI, "SVGResize",
                     svgResizeListener, false);
                target.removeEventListenerNS
                    (XMLConstants.XML_EVENTS_NAMESPACE_URI, "SVGScroll",
                     svgScrollListener, false);
                target.removeEventListenerNS
                    (XMLConstants.XML_EVENTS_NAMESPACE_URI, "SVGUnload",
                     svgUnloadListener, false);
                target.removeEventListenerNS
                    (XMLConstants.XML_EVENTS_NAMESPACE_URI, "SVGZoom",
                     svgZoomListener, false);
            } else {
                String name = elt.getLocalName();
                if (name.equals(SVGConstants.SVG_SET_TAG) ||
                    name.startsWith("animate")) {
                    target.removeEventListenerNS
                        (XMLConstants.XML_EVENTS_NAMESPACE_URI, "beginEvent",
                         beginListener, false);
                    target.removeEventListenerNS
                        (XMLConstants.XML_EVENTS_NAMESPACE_URI, "endEvent",
                         endListener, false);
                    target.removeEventListenerNS
                        (XMLConstants.XML_EVENTS_NAMESPACE_URI, "repeatEvent",
                         repeatListener , false);
                    return;
                }
            }
        }
        target.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMFocusIn",
             focusinListener, false);
        target.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMFocusOut",
             focusoutListener, false);
        target.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMActivate",
             activateListener, false);
        target.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "click",
             clickListener, false);
        target.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "mousedown",
             mousedownListener, false);
        target.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "mouseup",
             mouseupListener, false);
        target.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "mouseover",
             mouseoverListener, false);
        target.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "mouseout",
             mouseoutListener, false);
        target.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "mousemove",
             mousemoveListener, false);
        target.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "keypress",
             keypressListener, false);
        target.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "keydown",
             keydownListener, false);
        target.removeEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI, "keyup",
             keyupListener, false);
    }
    protected void updateScriptingListeners(Element elt, String attr) {
        String domEvt = (String) attrToDOMEvent.get(attr);
        if (domEvt == null) {
            return;  
        }
        EventListener listener = (EventListener) attrToListener.get(attr);
        NodeEventTarget target = (NodeEventTarget) elt;
        if (elt.hasAttributeNS(null, attr)) {
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, domEvt,
                 listener, false, null);
        } else {
            target.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, domEvt,
                 listener, false);
        }
    }
    protected class EvaluateRunnable implements Runnable {
        protected Interpreter interpreter;
        protected String script;
        public EvaluateRunnable(String s, Interpreter interp) {
            interpreter = interp;
            script = s;
        }
        public void run() {
            try {
                interpreter.evaluate(script);
            } catch (InterpreterException ie) {
                handleInterpreterException(ie);
            }
        }
    }
    protected class EvaluateIntervalRunnable implements Runnable {
        public int count;
        public boolean error;
        protected Interpreter interpreter;
        protected String script;
        public EvaluateIntervalRunnable(String s, Interpreter interp) {
            interpreter = interp;
            script = s;
        }
        public void run() {
            synchronized (this) {
                if (error)
                    return;
                count--;
            }
            try {
                interpreter.evaluate(script);
            } catch (InterpreterException ie) {
                handleInterpreterException(ie);
                synchronized (this) {
                    error = true;
                }
            } catch (Exception e) {
                if (userAgent != null) {
                    userAgent.displayError(e);
                } else {
                    e.printStackTrace(); 
                }
                synchronized (this) {
                    error = true;
                }
            }
        }
    }
    protected class EvaluateRunnableRunnable implements Runnable {
        public int count;
        public boolean error;
        protected Runnable runnable;
        public EvaluateRunnableRunnable(Runnable r) {
            runnable = r;
        }
        public void run() {
            synchronized (this) {
                if (error)
                    return;
                count--;
            }
            try {
                runnable.run();
            } catch (Exception e) {
                if (userAgent != null) {
                    userAgent.displayError(e);
                } else {
                    e.printStackTrace(); 
                }
                synchronized (this) {
                    error = true;
                }
            }
        }
    }
    protected class Window implements org.apache.batik.script.Window {
        protected class IntervalScriptTimerTask extends TimerTask {
            protected EvaluateIntervalRunnable eir;
            public IntervalScriptTimerTask(String script) {
                eir = new EvaluateIntervalRunnable(script, interpreter);
            }
            public void run() {
                synchronized (eir) {
                    if (eir.count > 1)
                        return;
                    eir.count++;
                }
                synchronized (updateRunnableQueue.getIteratorLock()) {
                    if (updateRunnableQueue.getThread() == null) {
                        cancel();
                        return;
                    }
                    updateRunnableQueue.invokeLater(eir);
                }
                synchronized (eir) {
                    if (eir.error)
                        cancel();
                }
            }
        }
        protected class IntervalRunnableTimerTask extends TimerTask {
            protected EvaluateRunnableRunnable eihr;
            public IntervalRunnableTimerTask(Runnable r) {
                eihr = new EvaluateRunnableRunnable(r);
            }
            public void run() {
                synchronized (eihr) {
                    if (eihr.count > 1)
                        return;
                    eihr.count++;
                }
                updateRunnableQueue.invokeLater(eihr);
                synchronized (eihr) {
                    if (eihr.error)
                        cancel();
                }
            }
        }
        protected class TimeoutScriptTimerTask extends TimerTask {
            private String script;
            public TimeoutScriptTimerTask(String script) {
                this.script = script;
            }
            public void run() {
                updateRunnableQueue.invokeLater
                    (new EvaluateRunnable(script, interpreter));
            }
        }
        protected class TimeoutRunnableTimerTask extends TimerTask {
            private Runnable r;
            public TimeoutRunnableTimerTask(Runnable r) {
                this.r = r;
            }
            public void run() {
                updateRunnableQueue.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                r.run();
                            } catch (Exception e) {
                                if (userAgent != null) {
                                    userAgent.displayError(e);
                                }
                            }
                        }
                    });
            }
        }
        protected Interpreter interpreter;
        protected String language;
        protected Location location;
        public Window(Interpreter interp, String lang) {
            interpreter = interp;
            language = lang;
        }
        public Object setInterval(final String script, long interval) {
            IntervalScriptTimerTask tt = new IntervalScriptTimerTask(script);
            timer.schedule(tt, interval, interval);
            return tt;
        }
        public Object setInterval(final Runnable r, long interval) {
            IntervalRunnableTimerTask tt = new IntervalRunnableTimerTask(r);
            timer.schedule(tt, interval, interval);
            return tt;
        }
        public void clearInterval(Object interval) {
            if (interval == null) return;
            ((TimerTask)interval).cancel();
        }
        public Object setTimeout(final String script, long timeout) {
            TimeoutScriptTimerTask tt = new TimeoutScriptTimerTask(script);
            timer.schedule(tt, timeout);
            return tt;
        }
        public Object setTimeout(final Runnable r, long timeout) {
            TimeoutRunnableTimerTask tt = new TimeoutRunnableTimerTask(r);
            timer.schedule(tt, timeout);
            return tt;
        }
        public void clearTimeout(Object timeout) {
            if (timeout == null) return;
            ((TimerTask)timeout).cancel();
        }
        public Node parseXML(String text, Document doc) {
            SAXSVGDocumentFactory df = new SAXSVGDocumentFactory
                (XMLResourceDescriptor.getXMLParserClassName());
            URL urlObj = null;
            if (doc instanceof SVGOMDocument) {
                urlObj = ((SVGOMDocument) doc).getURLObject();
            }
            if (urlObj == null) {
                urlObj = ((SVGOMDocument) bridgeContext.getDocument())
                        .getURLObject();
            }
            String uri = (urlObj == null) ? "" : urlObj.toString();
            Node res = DOMUtilities.parseXML(text, doc, uri, null, null, df);
            if (res != null) {
                return res;
            }
            if (doc instanceof SVGOMDocument) {
                Map prefixes = new HashMap();
                prefixes.put(XMLConstants.XMLNS_PREFIX,
                        XMLConstants.XMLNS_NAMESPACE_URI);
                prefixes.put(XMLConstants.XMLNS_PREFIX + ':'
                        + XMLConstants.XLINK_PREFIX,
                        XLinkSupport.XLINK_NAMESPACE_URI);
                res = DOMUtilities.parseXML(text, doc, uri, prefixes,
                        SVGConstants.SVG_SVG_TAG, df);
                if (res != null) {
                    return res;
                }
            }
            SAXDocumentFactory sdf;
            if (doc != null) {
                sdf = new SAXDocumentFactory(doc.getImplementation(),
                        XMLResourceDescriptor.getXMLParserClassName());
            } else {
                sdf = new SAXDocumentFactory(new GenericDOMImplementation(),
                        XMLResourceDescriptor.getXMLParserClassName());
            }
            return DOMUtilities.parseXML(text, doc, uri, null, null, sdf);
        }
        public String printNode(Node n) {
            try {
                Writer writer = new StringWriter();
                DOMUtilities.writeNode(n, writer);
                writer.close();
                return writer.toString();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        public void getURL(String uri, org.apache.batik.script.Window.URLResponseHandler h) {
            getURL(uri, h, null);
        }
        static final String DEFLATE="deflate";
        static final String GZIP   ="gzip";
        static final String UTF_8  ="UTF-8";
        public void getURL(final String uri,
                           final org.apache.batik.script.Window.URLResponseHandler h,
                           final String enc) {
            Thread t = new Thread() {
                    public void run() {
                        try {
                            ParsedURL burl;
                            burl = ((SVGOMDocument)document).getParsedURL();
                            final ParsedURL purl = new ParsedURL(burl, uri);
                            String e = null;
                            if (enc != null) {
                                e = EncodingUtilities.javaEncoding(enc);
                                e = ((e == null) ? enc : e);
                            }
                            InputStream is = purl.openStream();
                            Reader r;
                            if (e == null) {
                                r = new InputStreamReader(is);
                            } else {
                                try {
                                    r = new InputStreamReader(is, e);
                                } catch (UnsupportedEncodingException uee) {
                                    r = new InputStreamReader(is);
                                }
                            }
                            r = new BufferedReader(r);
                            final StringBuffer sb = new StringBuffer();
                            int read;
                            char[] buf = new char[4096];
                            while ((read = r.read(buf, 0, buf.length)) != -1) {
                                sb.append(buf, 0, read);
                            }
                            r.close();
                            updateRunnableQueue.invokeLater(new Runnable() {
                                    public void run() {
                                        try {
                                            h.getURLDone(true,
                                                         purl.getContentType(),
                                                         sb.toString());
                                        } catch (Exception e){
                                            if (userAgent != null) {
                                                userAgent.displayError(e);
                                            }
                                        }
                                    }
                                });
                        } catch (Exception e) {
                            if (e instanceof SecurityException) {
                                userAgent.displayError(e);
                            }
                            updateRunnableQueue.invokeLater(new Runnable() {
                                    public void run() {
                                        try {
                                            h.getURLDone(false, null, null);
                                        } catch (Exception e){
                                            if (userAgent != null) {
                                                userAgent.displayError(e);
                                            }
                                        }
                                    }
                                });
                        }
                    }
                };
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
        }
        public void postURL(String uri, String content,
                            org.apache.batik.script.Window.URLResponseHandler h) {
            postURL(uri, content, h, "text/plain", null);
        }
        public void postURL(String uri, String content,
                            org.apache.batik.script.Window.URLResponseHandler h,
                     String mimeType) {
            postURL(uri, content, h, mimeType, null);
        }
        public void postURL(final String uri,
                            final String content,
                            final org.apache.batik.script.Window.URLResponseHandler h,
                            final String mimeType,
                            final String fEnc) {
            Thread t = new Thread() {
                    public void run() {
                        try {
                            String base =
                                ((SVGOMDocument)document).getDocumentURI();
                            URL url;
                            if (base == null) {
                                url = new URL(uri);
                            } else {
                                url = new URL(new URL(base), uri);
                            }
                            final URLConnection conn = url.openConnection();
                            conn.setDoOutput(true);
                            conn.setDoInput(true);
                            conn.setUseCaches(false);
                            conn.setRequestProperty("Content-Type", mimeType);
                            OutputStream os = conn.getOutputStream();
                            String e=null, enc = fEnc;
                            if (enc != null) {
                                if (enc.startsWith(DEFLATE)) {
                                    os = new DeflaterOutputStream(os);
                                    if (enc.length() > DEFLATE.length())
                                        enc = enc.substring(DEFLATE.length()+1);
                                    else
                                        enc = "";
                                    conn.setRequestProperty("Content-Encoding",
                                                            DEFLATE);
                                }
                                if (enc.startsWith(GZIP)) {
                                    os = new GZIPOutputStream(os);
                                    if (enc.length() > GZIP.length())
                                        enc = enc.substring(GZIP.length()+1);
                                    else
                                        enc ="";
                                    conn.setRequestProperty("Content-Encoding",
                                                            DEFLATE);
                                }
                                if (enc.length() != 0) {
                                    e = EncodingUtilities.javaEncoding(enc);
                                    if (e == null) e = UTF_8;
                                } else {
                                    e = UTF_8;
                                }
                            }
                            Writer w;
                            if (e == null)
                                w = new OutputStreamWriter(os);
                            else
                                w = new OutputStreamWriter(os, e);
                            w.write(content);
                            w.flush();
                            w.close();
                            os.close();
                            InputStream is = conn.getInputStream();
                            Reader r;
                            e = UTF_8;
                            if (e == null)
                                r = new InputStreamReader(is);
                            else
                                r = new InputStreamReader(is, e);
                            r = new BufferedReader(r);
                            final StringBuffer sb = new StringBuffer();
                            int read;
                            char[] buf = new char[4096];
                            while ((read = r.read(buf, 0, buf.length)) != -1) {
                                sb.append(buf, 0, read);
                            }
                            r.close();
                            updateRunnableQueue.invokeLater(new Runnable() {
                                    public void run() {
                                        try {
                                            h.getURLDone(true,
                                                         conn.getContentType(),
                                                         sb.toString());
                                        } catch (Exception e){
                                            if (userAgent != null) {
                                                userAgent.displayError(e);
                                            }
                                        }
                                    }
                                });
                        } catch (Exception e) {
                            if (e instanceof SecurityException) {
                                userAgent.displayError(e);
                            }
                            updateRunnableQueue.invokeLater(new Runnable() {
                                    public void run() {
                                        try {
                                            h.getURLDone(false, null, null);
                                        } catch (Exception e){
                                            if (userAgent != null) {
                                                userAgent.displayError(e);
                                            }
                                        }
                                    }
                                });
                        }
                    }
                };
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
        }
        public void alert(String message) {
            if (userAgent != null) {
                userAgent.showAlert(message);
            }
        }
        public boolean confirm(String message) {
            if (userAgent != null) {
                return userAgent.showConfirm(message);
            }
            return false;
        }
        public String prompt(String message) {
            if (userAgent != null) {
                return userAgent.showPrompt(message);
            }
            return null;
        }
        public String prompt(String message, String defVal) {
            if (userAgent != null) {
                return userAgent.showPrompt(message, defVal);
            }
            return null;
        }
        public BridgeContext getBridgeContext() {
            return bridgeContext;
        }
        public Interpreter getInterpreter() {
            return interpreter;
        }
        public org.w3c.dom.Window getParent() {
            return null;
        }
        public org.w3c.dom.Location getLocation() {
            if (location == null) {
                location = new Location(bridgeContext);
            }
            return location;
        }
    }
    protected class DOMNodeInsertedListener implements EventListener {
        protected LinkedList toExecute = new LinkedList();
        public void handleEvent(Event evt) {
            Node n = (Node) evt.getTarget();
            addScriptingListeners(n);
            gatherScriptElements(n);
            while (!toExecute.isEmpty()) {
                loadScript((AbstractElement) toExecute.removeFirst());
            }
        }
        protected void gatherScriptElements(Node n) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n instanceof SVGOMScriptElement) {
                    toExecute.add(n);
                } else {
                    n = n.getFirstChild();
                    while (n != null) {
                        gatherScriptElements(n);
                        n = n.getNextSibling();
                    }
                }
            }
        }
    }
    protected class DOMNodeRemovedListener implements EventListener {
        public void handleEvent(Event evt) {
            removeScriptingListeners((Node)evt.getTarget());
        }
    }
    protected class DOMAttrModifiedListener implements EventListener {
        public void handleEvent (Event evt) {
            MutationEvent me = (MutationEvent)evt;
            if (me.getAttrChange() != MutationEvent.MODIFICATION)
                updateScriptingListeners((Element)me.getTarget(),
                                         me.getAttrName());
        }
    }
    protected class ScriptingEventListener implements EventListener {
        protected String attribute;
        public ScriptingEventListener(String attr) {
            attribute = attr;
        }
        public void handleEvent(Event evt) {
            Element elt = (Element)evt.getCurrentTarget();
            String script = elt.getAttributeNS(null, attribute);
            if (script.length() == 0)
                return;
            DocumentLoader dl = bridgeContext.getDocumentLoader();
            SVGDocument d = (SVGDocument)elt.getOwnerDocument();
            int line = dl.getLineNumber(elt);
            final String desc = Messages.formatMessage
                (EVENT_SCRIPT_DESCRIPTION,
                 new Object [] {d.getURL(), attribute, new Integer(line)});
            Element e = elt;
            while (e != null &&
                   (!SVGConstants.SVG_NAMESPACE_URI.equals
                    (e.getNamespaceURI()) ||
                    !SVGConstants.SVG_SVG_TAG.equals(e.getLocalName()))) {
                e = SVGUtilities.getParentElement(e);
            }
            if (e == null)
                return;
            String lang = e.getAttributeNS
                (null, SVGConstants.SVG_CONTENT_SCRIPT_TYPE_ATTRIBUTE);
            runEventHandler(script, evt, lang, desc);
        }
    }
}
