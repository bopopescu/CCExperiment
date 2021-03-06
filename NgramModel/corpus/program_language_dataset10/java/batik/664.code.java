package org.apache.batik.dom.svg12;
import org.apache.batik.css.engine.CSSNavigableDocumentListener;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.svg.SVGDocument;
public class SVG12OMDocument extends SVGOMDocument {
    protected SVG12OMDocument() {
    }
    public SVG12OMDocument(DocumentType dt, DOMImplementation impl) {
        super(dt, impl);
    }
    protected Node newNode() {
        return new SVG12OMDocument();
    }
    public void addCSSNavigableDocumentListener
            (CSSNavigableDocumentListener l) {
        if (cssNavigableDocumentListeners.containsKey(l)) {
            return;
        }
        DOMNodeInsertedListenerWrapper nodeInserted
            = new DOMNodeInsertedListenerWrapper(l);
        DOMNodeRemovedListenerWrapper nodeRemoved
            = new DOMNodeRemovedListenerWrapper(l);
        DOMSubtreeModifiedListenerWrapper subtreeModified
            = new DOMSubtreeModifiedListenerWrapper(l);
        DOMCharacterDataModifiedListenerWrapper cdataModified
            = new DOMCharacterDataModifiedListenerWrapper(l);
        DOMAttrModifiedListenerWrapper attrModified
            = new DOMAttrModifiedListenerWrapper(l);
        cssNavigableDocumentListeners.put
            (l, new EventListener[] { nodeInserted,
                                      nodeRemoved,
                                      subtreeModified,
                                      cdataModified,
                                      attrModified });
        XBLEventSupport es = (XBLEventSupport) initializeEventSupport();
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeInserted",
             nodeInserted, false);
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeRemoved",
             nodeRemoved, false);
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMSubtreeModified",
             subtreeModified, false);
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMCharacterDataModified",
             cdataModified, false);
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMAttrModified",
             attrModified, false);
    }
    public void removeCSSNavigableDocumentListener
            (CSSNavigableDocumentListener l) {
        EventListener[] listeners
            = (EventListener[]) cssNavigableDocumentListeners.get(l);
        if (listeners == null) {
            return;
        }
        XBLEventSupport es = (XBLEventSupport) initializeEventSupport();
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeInserted",
             listeners[0], false);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeRemoved",
             listeners[1], false);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMSubtreeModified",
             listeners[2], false);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMCharacterDataModified",
             listeners[3], false);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMAttrModified",
             listeners[4], false);
        cssNavigableDocumentListeners.remove(l);
    }
}
