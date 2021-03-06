package org.apache.batik.bridge.svg12;
import javax.swing.event.EventListenerList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.xbl.XBLManager;
import org.apache.batik.dom.svg12.XBLEventSupport;
import org.apache.batik.dom.svg12.XBLOMContentElement;
import org.apache.batik.dom.svg12.XBLOMShadowTreeElement;
import org.apache.batik.util.XBLConstants;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;
public class ContentManager {
    protected XBLOMShadowTreeElement shadowTree;
    protected Element boundElement;
    protected DefaultXBLManager xblManager;
    protected HashMap selectors = new HashMap();
    protected HashMap selectedNodes = new HashMap();
    protected LinkedList contentElementList = new LinkedList();
    protected Node removedNode;
    protected HashMap listeners = new HashMap();
    protected ContentElementDOMAttrModifiedEventListener
        contentElementDomAttrModifiedEventListener;
    protected DOMAttrModifiedEventListener domAttrModifiedEventListener;
    protected DOMNodeInsertedEventListener domNodeInsertedEventListener;
    protected DOMNodeRemovedEventListener domNodeRemovedEventListener;
    protected DOMSubtreeModifiedEventListener domSubtreeModifiedEventListener;
    protected ShadowTreeNodeInsertedListener shadowTreeNodeInsertedListener;
    protected ShadowTreeNodeRemovedListener shadowTreeNodeRemovedListener;
    protected ShadowTreeSubtreeModifiedListener
        shadowTreeSubtreeModifiedListener;
    public ContentManager(XBLOMShadowTreeElement s, XBLManager xm) {
        shadowTree = s;
        xblManager = (DefaultXBLManager) xm;
        xblManager.setContentManager(s, this);
        boundElement = xblManager.getXblBoundElement(s);
        contentElementDomAttrModifiedEventListener =
            new ContentElementDOMAttrModifiedEventListener();
        XBLEventSupport es = (XBLEventSupport)
            shadowTree.initializeEventSupport();
        shadowTreeNodeInsertedListener = new ShadowTreeNodeInsertedListener();
        shadowTreeNodeRemovedListener = new ShadowTreeNodeRemovedListener();
        shadowTreeSubtreeModifiedListener
            = new ShadowTreeSubtreeModifiedListener();
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeInserted",
             shadowTreeNodeInsertedListener, true);
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeRemoved",
             shadowTreeNodeRemovedListener, true);
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMSubtreeModified",
             shadowTreeSubtreeModifiedListener, true);
        es = (XBLEventSupport)
            ((AbstractNode) boundElement).initializeEventSupport();
        domAttrModifiedEventListener = new DOMAttrModifiedEventListener();
        domNodeInsertedEventListener = new DOMNodeInsertedEventListener();
        domNodeRemovedEventListener = new DOMNodeRemovedEventListener();
        domSubtreeModifiedEventListener = new DOMSubtreeModifiedEventListener();
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMAttrModified",
             domAttrModifiedEventListener, true);
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeInserted",
             domNodeInsertedEventListener, true);
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeRemoved",
             domNodeRemovedEventListener, true);
        es.addImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMSubtreeModified",
             domSubtreeModifiedEventListener, false);
        update(true);
    }
    public void dispose() {
        xblManager.setContentManager(shadowTree, null);
        Iterator i = selectedNodes.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            NodeList nl = (NodeList) e.getValue();
            for (int j = 0; j < nl.getLength(); j++) {
                Node n = nl.item(j);
                xblManager.getRecord(n).contentElement = null;
            }
        }
        i = contentElementList.iterator();
        while (i.hasNext()) {
            NodeEventTarget n = (NodeEventTarget) i.next();
            n.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMAttrModified",
                 contentElementDomAttrModifiedEventListener, false);
        }
        contentElementList.clear();
        selectedNodes.clear();
        XBLEventSupport es
            = (XBLEventSupport) ((AbstractNode) boundElement).getEventSupport();
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMAttrModified",
             domAttrModifiedEventListener, true);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeInserted",
             domNodeInsertedEventListener, true);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMNodeRemoved",
             domNodeRemovedEventListener, true);
        es.removeImplementationEventListenerNS
            (XMLConstants.XML_EVENTS_NAMESPACE_URI,
             "DOMSubtreeModified",
             domSubtreeModifiedEventListener, false);
    }
    public NodeList getSelectedContent(XBLOMContentElement e) {
        return (NodeList) selectedNodes.get(e);
    }
    protected XBLOMContentElement getContentElement(Node n) {
        return xblManager.getXblContentElement(n);
    }
    public void addContentSelectionChangedListener
            (XBLOMContentElement e, ContentSelectionChangedListener l) {
        EventListenerList ll = (EventListenerList) listeners.get(e);
        if (ll == null) {
            ll = new EventListenerList();
            listeners.put(e, ll);
        }
        ll.add(ContentSelectionChangedListener.class, l);
    }
    public void removeContentSelectionChangedListener
            (XBLOMContentElement e, ContentSelectionChangedListener l) {
        EventListenerList ll = (EventListenerList) listeners.get(e);
        if (ll != null) {
            ll.remove(ContentSelectionChangedListener.class, l);
        }
    }
    protected void dispatchContentSelectionChangedEvent(XBLOMContentElement e) {
        xblManager.invalidateChildNodes(e.getXblParentNode());
        ContentSelectionChangedEvent evt =
            new ContentSelectionChangedEvent(e);
        EventListenerList ll = (EventListenerList) listeners.get(e);
        if (ll != null) {
            Object[] ls = ll.getListenerList();
            for (int i = ls.length - 2; i >= 0; i -= 2) {
                ContentSelectionChangedListener l =
                    (ContentSelectionChangedListener) ls[i + 1];
                l.contentSelectionChanged(evt);
            }
        }
        Object[] ls = xblManager.getContentSelectionChangedListeners();
        for (int i = ls.length - 2; i >= 0; i -= 2) {
            ContentSelectionChangedListener l =
                (ContentSelectionChangedListener) ls[i + 1];
            l.contentSelectionChanged(evt);
        }
    }
    protected void update(boolean first) {
        HashSet previouslySelectedNodes = new HashSet();
        Iterator i = selectedNodes.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            NodeList nl = (NodeList) e.getValue();
            for (int j = 0; j < nl.getLength(); j++) {
                Node n = nl.item(j);
                xblManager.getRecord(n).contentElement = null;
                previouslySelectedNodes.add(n);
            }
        }
        i = contentElementList.iterator();
        while (i.hasNext()) {
            NodeEventTarget n = (NodeEventTarget) i.next();
            n.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMAttrModified",
                 contentElementDomAttrModifiedEventListener, false);
        }
        contentElementList.clear();
        selectedNodes.clear();
        boolean updated = false;
        for (Node n = shadowTree.getFirstChild();
                n != null;
                n = n.getNextSibling()) {
            if (update(first, n)) {
                updated = true;
            }
        }
        if (updated) {
            HashSet newlySelectedNodes = new HashSet();
            i = selectedNodes.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry) i.next();
                NodeList nl = (NodeList) e.getValue();
                for (int j = 0; j < nl.getLength(); j++) {
                    Node n = nl.item(j);
                    newlySelectedNodes.add(n);
                }
            }
            HashSet removed = new HashSet();
            removed.addAll(previouslySelectedNodes);
            removed.removeAll(newlySelectedNodes);
            HashSet added = new HashSet();
            added.addAll(newlySelectedNodes);
            added.removeAll(previouslySelectedNodes);
            if (!first) {
                xblManager.shadowTreeSelectedContentChanged(removed, added);
            }
        }
    }
    protected boolean update(boolean first, Node n) {
        boolean updated = false;
        for (Node m = n.getFirstChild(); m != null; m = m.getNextSibling()) {
            if (update(first, m)) {
                updated = true;
            }
        }
        if (n instanceof XBLOMContentElement) {
            contentElementList.add(n);
            XBLOMContentElement e = (XBLOMContentElement) n;
            e.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "DOMAttrModified",
                 contentElementDomAttrModifiedEventListener, false, null);
            AbstractContentSelector s =
                (AbstractContentSelector) selectors.get(n);
            boolean changed;
            if (s == null) {
                if (e.hasAttributeNS(null,
                                     XBLConstants.XBL_INCLUDES_ATTRIBUTE)) {
                    String lang = getContentSelectorLanguage(e);
                    String selector = e.getAttributeNS
                        (null, XBLConstants.XBL_INCLUDES_ATTRIBUTE);
                    s = AbstractContentSelector.createSelector
                        (lang, this, e, boundElement, selector);
                } else {
                    s = new DefaultContentSelector(this, e, boundElement);
                }
                selectors.put(n, s);
                changed = true;
            } else {
                changed = s.update();
            }
            NodeList selectedContent = s.getSelectedContent();
            selectedNodes.put(n, selectedContent);
            for (int i = 0; i < selectedContent.getLength(); i++) {
                Node m = selectedContent.item(i);
                xblManager.getRecord(m).contentElement = e;
            }
            if (changed) {
                updated = true;
                dispatchContentSelectionChangedEvent(e);
            }
        }
        return updated;
    }
    protected String getContentSelectorLanguage(Element e) {
        String lang = e.getAttributeNS("http://xml.apache.org/batik/ext",
                                       "selectorLanguage");
        if (lang.length() != 0) {
            return lang;
        }
        lang = e.getOwnerDocument().getDocumentElement().getAttributeNS
            ("http://xml.apache.org/batik/ext", "selectorLanguage");
        if (lang.length() != 0) {
            return lang;
        }
        return null;
    }
    protected class ContentElementDOMAttrModifiedEventListener
            implements EventListener {
        public void handleEvent(Event evt) {
            MutationEvent me = (MutationEvent) evt;
            Attr a = (Attr) me.getRelatedNode();
            Element e = (Element) evt.getTarget();
            if (e instanceof XBLOMContentElement) {
                String ans = a.getNamespaceURI();
                String aln = a.getLocalName();
                if (aln == null) {
                    aln = a.getNodeName();
                }
                if (ans == null && XBLConstants.XBL_INCLUDES_ATTRIBUTE.equals(aln)
                        || "http://xml.apache.org/batik/ext".equals(ans)
                            && "selectorLanguage".equals(aln)) {
                    selectors.remove(e);
                    update(false);
                }
            }
        }
    }
    protected class DOMAttrModifiedEventListener implements EventListener {
        public void handleEvent(Event evt) {
            if (evt.getTarget() != boundElement) {
                update(false);
            }
        }
    }
    protected class DOMNodeInsertedEventListener implements EventListener {
        public void handleEvent(Event evt) {
            update(false);
        }
    }
    protected class DOMNodeRemovedEventListener implements EventListener {
        public void handleEvent(Event evt) {
            removedNode = (Node) evt.getTarget();
        }
    }
    protected class DOMSubtreeModifiedEventListener implements EventListener {
        public void handleEvent(Event evt) {
            if (removedNode != null) {
                removedNode = null;
                update(false);
            }
        }
    }
    protected class ShadowTreeNodeInsertedListener implements EventListener {
        public void handleEvent(Event evt) {
            if (evt.getTarget() instanceof XBLOMContentElement) {
                update(false);
            }
        }
    }
    protected class ShadowTreeNodeRemovedListener implements EventListener {
        public void handleEvent(Event evt) {
            EventTarget target = evt.getTarget();
            if (target instanceof XBLOMContentElement) {
                removedNode = (Node) evt.getTarget();
            }
        }
    }
    protected class ShadowTreeSubtreeModifiedListener implements EventListener {
        public void handleEvent(Event evt) {
            if (removedNode != null) {
                removedNode = null;
                update(false);
            }
        }
    }
}
