package org.apache.batik.dom;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.WeakHashMap;
import org.apache.batik.dom.events.DocumentEventSupport;
import org.apache.batik.dom.events.EventSupport;
import org.apache.batik.dom.traversal.TraversalSupport;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.xbl.GenericXBLManager;
import org.apache.batik.dom.xbl.XBLManager;
import org.apache.batik.i18n.Localizable;
import org.apache.batik.i18n.LocalizableSupport;
import org.apache.batik.util.CleanerThread;
import org.apache.batik.util.DOMConstants;
import org.apache.batik.util.SoftDoublyIndexedTable;
import org.apache.batik.util.XMLConstants;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMLocator;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.Element;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.MutationNameEvent;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.traversal.TreeWalker;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.xpath.XPathEvaluator;
import org.w3c.dom.xpath.XPathException;
import org.w3c.dom.xpath.XPathExpression;
import org.w3c.dom.xpath.XPathNSResolver;
import org.w3c.dom.xpath.XPathResult;
public abstract class AbstractDocument
    extends    AbstractParentNode
    implements Document,
               DocumentEvent,
               DocumentTraversal,
               Localizable,
               XPathEvaluator {
    protected static final String RESOURCES =
        "org.apache.batik.dom.resources.Messages";
    protected transient LocalizableSupport localizableSupport =
        new LocalizableSupport
        (RESOURCES, getClass().getClassLoader());
    protected transient DOMImplementation implementation;
    protected transient TraversalSupport traversalSupport;
    protected transient DocumentEventSupport documentEventSupport;
    protected transient boolean eventsEnabled;
    protected transient WeakHashMap elementsByTagNames;
    protected transient WeakHashMap elementsByTagNamesNS;
    protected String inputEncoding;
    protected String xmlEncoding;
    protected String xmlVersion = XMLConstants.XML_VERSION_10;
    protected boolean xmlStandalone;
    protected String documentURI;
    protected boolean strictErrorChecking = true;
    protected DocumentConfiguration domConfig;
    protected transient XBLManager xblManager = new GenericXBLManager();
    protected transient Map elementsById;
    protected AbstractDocument() {
    }
    public AbstractDocument(DocumentType dt, DOMImplementation impl) {
        implementation = impl;
        if (dt != null) {
            if (dt instanceof GenericDocumentType) {
                GenericDocumentType gdt = (GenericDocumentType)dt;
                if (gdt.getOwnerDocument() == null)
                    gdt.setOwnerDocument(this);
            }
            appendChild(dt);
        }
    }
    public void setDocumentInputEncoding(String ie) {
        inputEncoding = ie;
    }
    public void setDocumentXmlEncoding(String xe) {
        xmlEncoding = xe;
    }
    public void setLocale(Locale l) {
        localizableSupport.setLocale(l);
    }
    public Locale getLocale() {
        return localizableSupport.getLocale();
    }
    public String formatMessage(String key, Object[] args)
        throws MissingResourceException {
        return localizableSupport.formatMessage(key, args);
    }
    public boolean getEventsEnabled() {
        return eventsEnabled;
    }
    public void setEventsEnabled(boolean b) {
        eventsEnabled = b;
    }
    public String getNodeName() {
        return "#document";
    }
    public short getNodeType() {
        return DOCUMENT_NODE;
    }
    public DocumentType getDoctype() {
        for (Node n = getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == DOCUMENT_TYPE_NODE) {
                return (DocumentType)n;
            }
        }
        return null;
    }
    public void setDoctype(DocumentType dt) {
        if (dt != null) {
            appendChild(dt);
            ((ExtendedNode)dt).setReadonly(true);
        }
    }
    public DOMImplementation getImplementation() {
        return implementation;
    }
    public Element getDocumentElement() {
        for (Node n = getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == ELEMENT_NODE) {
                return (Element)n;
            }
        }
        return null;
    }
    public Node importNode(Node importedNode, boolean deep)
        throws DOMException {
        return importNode(importedNode, deep, false);
    }
    public Node importNode(Node importedNode, boolean deep, boolean trimId) {
        Node result;
        switch (importedNode.getNodeType()) {
        case ELEMENT_NODE:
            Element e = createElementNS(importedNode.getNamespaceURI(),
                                        importedNode.getNodeName());
            result = e;
            if (importedNode.hasAttributes()) {
                NamedNodeMap attr = importedNode.getAttributes();
                int len = attr.getLength();
                for (int i = 0; i < len; i++) {
                    Attr a = (Attr)attr.item(i);
                    if (!a.getSpecified()) continue;
                    AbstractAttr aa = (AbstractAttr)importNode(a, true);
                    if (trimId && aa.isId())
                        aa.setIsId(false); 
                    e.setAttributeNodeNS(aa);
                }
            }
            break;
        case ATTRIBUTE_NODE:
            result = createAttributeNS(importedNode.getNamespaceURI(),
                                       importedNode.getNodeName());
            break;
        case TEXT_NODE:
            result = createTextNode(importedNode.getNodeValue());
            deep = false;
            break;
        case CDATA_SECTION_NODE:
            result = createCDATASection(importedNode.getNodeValue());
            deep = false;
            break;
        case ENTITY_REFERENCE_NODE:
            result = createEntityReference(importedNode.getNodeName());
            break;
        case PROCESSING_INSTRUCTION_NODE:
            result = createProcessingInstruction
                (importedNode.getNodeName(),
                 importedNode.getNodeValue());
            deep = false;
            break;
        case COMMENT_NODE:
            result = createComment(importedNode.getNodeValue());
            deep = false;
            break;
        case DOCUMENT_FRAGMENT_NODE:
            result = createDocumentFragment();
            break;
        default:
            throw createDOMException(DOMException.NOT_SUPPORTED_ERR,
                                     "import.node",
                                     new Object[] {});
        }
        if (importedNode instanceof AbstractNode) {
            fireUserDataHandlers(UserDataHandler.NODE_IMPORTED,
                                 importedNode,
                                 result);
        }
        if (deep) {
            for (Node n = importedNode.getFirstChild();
                 n != null;
                 n = n.getNextSibling()) {
                result.appendChild(importNode(n, true));
            }
        }
        return result;
    }
    public Node cloneNode(boolean deep) {
        Document n = (Document)newNode();
        copyInto(n);
        fireUserDataHandlers(UserDataHandler.NODE_CLONED, this, n);
        if (deep) {
            for (Node c = getFirstChild();
                 c != null;
                 c = c.getNextSibling()) {
                n.appendChild(n.importNode(c, deep));
            }
        }
        return n;
    }
    public abstract boolean isId(Attr node);
    public Element getElementById(String id) {
        return getChildElementById(getDocumentElement(), id);
    }
    public Element getChildElementById(Node requestor, String id) {
        if ((id == null) || (id.length()==0)) return null;
        if (elementsById == null) return null;
        Node root = getRoot(requestor);
        Object o = elementsById.get(id);
        if (o == null) return null;
        if (o instanceof IdSoftRef) {
            o = ((IdSoftRef)o).get();
            if (o == null) {
                elementsById.remove(id);
                return null;
            }
            Element e = (Element)o;
            if (getRoot(e) == root)
                return e;
            return null;
        }
        List l = (List)o;
        Iterator li = l.iterator();
        while (li.hasNext()) {
            IdSoftRef sr = (IdSoftRef)li.next();
            o = sr.get();
            if (o == null) {
                li.remove();
            } else {
                Element e = (Element)o;
                if (getRoot(e) == root)
                    return e;
            }
        }
        return null;
    }
    protected Node getRoot(Node n) {
        Node r = n;
        while (n != null) {
            r = n;
            n = n.getParentNode();
        }
        return r;
    }
    protected class IdSoftRef extends CleanerThread.SoftReferenceCleared {
        String id;
        List   list;
        IdSoftRef(Object o, String id) {
            super(o);
            this.id = id;
        }
        IdSoftRef(Object o, String id, List list) {
            super(o);
            this.id = id;
            this.list = list;
        }
        public void setList(List list) {
            this.list = list;
        }
        public void cleared() {
            if (elementsById == null) return;
            synchronized (elementsById) {
                if (list != null)
                    list.remove(this);
                else {
                  Object o = elementsById.remove(id);
                  if (o != this) 
                      elementsById.put(id, o);
                }
            }
        }
    }
    public void removeIdEntry(Element e, String id) {
        if (id == null) return;
        if (elementsById == null) return;
        synchronized (elementsById) {
            Object o = elementsById.get(id);
            if (o == null) return;
            if (o instanceof IdSoftRef) {
                elementsById.remove(id);
                return;
            }
            List l = (List)o;
            Iterator li = l.iterator();
            while (li.hasNext()) {
                IdSoftRef ip = (IdSoftRef)li.next();
                o = ip.get();
                if (o == null) {
                    li.remove();
                } else if (e == o) {
                    li.remove();
                    break;
                }
            }
            if (l.size() == 0)
                elementsById.remove(id);
        }
    }
    public void addIdEntry(Element e, String id) {
        if (id == null) return;
        if (elementsById == null) {
            Map tmp = new HashMap();
            tmp.put(id, new IdSoftRef(e, id));
            elementsById = tmp;
            return;
        }
        synchronized (elementsById) {
            Object o = elementsById.get(id);
            if (o == null) {
                elementsById.put(id, new IdSoftRef(e, id));
                return;
            }
            if (o instanceof IdSoftRef) {
                IdSoftRef ip = (IdSoftRef)o;
                Object r = ip.get();
                if (r == null) { 
                    elementsById.put(id, new IdSoftRef(e, id));
                    return;
                }
                List l = new ArrayList(4);
                ip.setList(l);
                l.add(ip);
                l.add(new IdSoftRef(e, id, l));
                elementsById.put(id, l);
                return;
            }
            List l = (List)o;
            l.add(new IdSoftRef(e, id, l));
        }
    }
    public void updateIdEntry(Element e, String oldId, String newId) {
        if ((oldId == newId) ||
            ((oldId != null) && (oldId.equals(newId))))
            return;
        removeIdEntry(e, oldId);
        addIdEntry(e, newId);
    }
    public ElementsByTagName getElementsByTagName(Node n, String ln) {
        if (elementsByTagNames == null) {
            return null;
        }
        SoftDoublyIndexedTable t;
        t = (SoftDoublyIndexedTable)elementsByTagNames.get(n);
        if (t == null) {
            return null;
        }
        return (ElementsByTagName)t.get(null, ln);
    }
    public void putElementsByTagName(Node n, String ln, ElementsByTagName l) {
        if (elementsByTagNames == null) {
            elementsByTagNames = new WeakHashMap(11);
        }
        SoftDoublyIndexedTable t;
        t = (SoftDoublyIndexedTable)elementsByTagNames.get(n);
        if (t == null) {
            elementsByTagNames.put(n, t = new SoftDoublyIndexedTable());
        }
        t.put(null, ln, l);
    }
    public ElementsByTagNameNS getElementsByTagNameNS(Node n,
                                                      String ns,
                                                      String ln) {
        if (elementsByTagNamesNS == null) {
            return null;
        }
        SoftDoublyIndexedTable t;
        t = (SoftDoublyIndexedTable)elementsByTagNamesNS.get(n);
        if (t == null) {
            return null;
        }
        return (ElementsByTagNameNS)t.get(ns, ln);
    }
    public void putElementsByTagNameNS(Node n, String ns, String ln,
                                       ElementsByTagNameNS l) {
        if (elementsByTagNamesNS == null) {
            elementsByTagNamesNS = new WeakHashMap(11);
        }
        SoftDoublyIndexedTable t;
        t = (SoftDoublyIndexedTable)elementsByTagNamesNS.get(n);
        if (t == null) {
            elementsByTagNamesNS.put(n, t = new SoftDoublyIndexedTable());
        }
        t.put(ns, ln, l);
    }
    public Event createEvent(String eventType) throws DOMException {
        if (documentEventSupport == null) {
            documentEventSupport =
                ((AbstractDOMImplementation)implementation).
                    createDocumentEventSupport();
        }
        return documentEventSupport.createEvent(eventType);
    }
    public boolean canDispatch(String ns, String eventType) {
        if (eventType == null) {
            return false;
        }
        if (ns != null && ns.length() == 0) {
            ns = null;
        }
        if (ns == null || ns.equals(XMLConstants.XML_EVENTS_NAMESPACE_URI)) {
            return eventType.equals("Event")
                || eventType.equals("MutationEvent")
                || eventType.equals("MutationNameEvent")
                || eventType.equals("UIEvent")
                || eventType.equals("MouseEvent")
                || eventType.equals("KeyEvent")
                || eventType.equals("KeyboardEvent")
                || eventType.equals("TextEvent")
                || eventType.equals("CustomEvent");
        }
        return false;
    }
    public NodeIterator createNodeIterator(Node root,
                                           int whatToShow,
                                           NodeFilter filter,
                                           boolean entityReferenceExpansion)
        throws DOMException {
        if (traversalSupport == null) {
            traversalSupport = new TraversalSupport();
        }
        return traversalSupport.createNodeIterator(this, root, whatToShow,
                                                   filter,
                                                   entityReferenceExpansion);
    }
    public TreeWalker createTreeWalker(Node root,
                                       int whatToShow,
                                       NodeFilter filter,
                                       boolean entityReferenceExpansion)
        throws DOMException {
        return TraversalSupport.createTreeWalker(this, root, whatToShow,
                                                 filter,
                                                 entityReferenceExpansion);
    }
    public void detachNodeIterator(NodeIterator it) {
        traversalSupport.detachNodeIterator(it);
    }
    public void nodeToBeRemoved(Node node) {
        if (traversalSupport != null) {
            traversalSupport.nodeToBeRemoved(node);
        }
    }
    protected AbstractDocument getCurrentDocument() {
        return this;
    }
    protected Node export(Node n, Document d) {
        throw createDOMException(DOMException.NOT_SUPPORTED_ERR,
                                 "import.document",
                                 new Object[] {});
    }
    protected Node deepExport(Node n, Document d) {
        throw createDOMException(DOMException.NOT_SUPPORTED_ERR,
                                 "import.document",
                                 new Object[] {});
    }
    protected Node copyInto(Node n) {
        super.copyInto(n);
        AbstractDocument ad = (AbstractDocument)n;
        ad.implementation = implementation;
        ad.localizableSupport = new LocalizableSupport
            (RESOURCES, getClass().getClassLoader());
        ad.inputEncoding = inputEncoding;
        ad.xmlEncoding = xmlEncoding;
        ad.xmlVersion = xmlVersion;
        ad.xmlStandalone = xmlStandalone;
        ad.documentURI = documentURI;
        ad.strictErrorChecking = strictErrorChecking;
        return n;
    }
    protected Node deepCopyInto(Node n) {
        super.deepCopyInto(n);
        AbstractDocument ad = (AbstractDocument)n;
        ad.implementation = implementation;
        ad.localizableSupport = new LocalizableSupport
            (RESOURCES, getClass().getClassLoader());
        return n;
    }
    protected void checkChildType(Node n, boolean replace) {
        short t = n.getNodeType();
        switch (t) {
        case ELEMENT_NODE:
        case PROCESSING_INSTRUCTION_NODE:
        case COMMENT_NODE:
        case DOCUMENT_TYPE_NODE:
        case DOCUMENT_FRAGMENT_NODE:
            break;
        default:
            throw createDOMException(DOMException.HIERARCHY_REQUEST_ERR,
                                     "child.type",
                                     new Object[] { new Integer(getNodeType()),
                                                    getNodeName(),
                                                    new Integer(t),
                                                    n.getNodeName() });
        }
        if (!replace &&
            (t == ELEMENT_NODE && getDocumentElement() != null) ||
            (t == DOCUMENT_TYPE_NODE && getDoctype() != null)) {
            throw createDOMException(DOMException.NOT_SUPPORTED_ERR,
                                     "document.child.already.exists",
                                     new Object[] { new Integer(t),
                                                    n.getNodeName() });
        }
    }
    public String getInputEncoding() {
        return inputEncoding;
    }
    public String getXmlEncoding() {
        return xmlEncoding;
    }
    public boolean getXmlStandalone() {
        return xmlStandalone;
    }
    public void setXmlStandalone(boolean b) throws DOMException {
        xmlStandalone = b;
    }
    public String getXmlVersion() {
        return xmlVersion;
    }
    public void setXmlVersion(String v) throws DOMException {
        if (v == null
                || !v.equals(XMLConstants.XML_VERSION_10)
                    && !v.equals(XMLConstants.XML_VERSION_11)) {
            throw createDOMException(DOMException.NOT_SUPPORTED_ERR,
                                     "xml.version",
                                     new Object[] { v });
        }
        xmlVersion = v;
    }
    public boolean getStrictErrorChecking() {
        return strictErrorChecking;
    }
    public void setStrictErrorChecking(boolean b) {
        strictErrorChecking = b;
    }
    public String getDocumentURI() {
        return documentURI;
    }
    public void setDocumentURI(String uri) {
        documentURI = uri;
    }
    public DOMConfiguration getDomConfig() {
        if (domConfig == null) {
            domConfig = new DocumentConfiguration();
        }
        return domConfig;
    }
    public Node adoptNode(Node n) throws DOMException {
        if (!(n instanceof AbstractNode)) {
            return null;
        }
        switch (n.getNodeType()) {
            case Node.DOCUMENT_NODE:
                throw createDOMException(DOMException.NOT_SUPPORTED_ERR,
                                         "adopt.document",
                                         new Object[] {});
            case Node.DOCUMENT_TYPE_NODE:
                throw createDOMException(DOMException.NOT_SUPPORTED_ERR,
                                         "adopt.document.type",
                                          new Object[] {});
            case Node.ENTITY_NODE:
            case Node.NOTATION_NODE:
                return null;
        }
        AbstractNode an = (AbstractNode) n;
        if (an.isReadonly()) {
            throw createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.node",
                 new Object[] { new Integer(an.getNodeType()),
                               an.getNodeName() });
        }
        Node parent = n.getParentNode();
        if (parent != null) {
            parent.removeChild(n);
        }
        adoptNode1((AbstractNode) n);
        return n;
    }
    protected void adoptNode1(AbstractNode n) {
        n.ownerDocument = this;
        switch (n.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                AbstractAttr attr = (AbstractAttr) n;
                attr.ownerElement = null;
                attr.unspecified = false;
                break;
            case Node.ELEMENT_NODE:
                NamedNodeMap nnm = n.getAttributes();
                int len = nnm.getLength();
                for (int i = 0; i < len; i++) {
                    attr = (AbstractAttr) nnm.item(i);
                    if (attr.getSpecified()) {
                        adoptNode1(attr);
                    }
                }
                break;
            case Node.ENTITY_REFERENCE_NODE:
                while (n.getFirstChild() != null) {
                    n.removeChild(n.getFirstChild());
                }
                break;
        }
        fireUserDataHandlers(UserDataHandler.NODE_ADOPTED, n, null);
        for (Node m = n.getFirstChild(); m != null; m = m.getNextSibling()) {
            switch (m.getNodeType()) {
                case Node.DOCUMENT_TYPE_NODE:
                case Node.ENTITY_NODE:
                case Node.NOTATION_NODE:
                    return;
            }
            adoptNode1((AbstractNode) m);
        }
    }
    public Node renameNode(Node n, String ns, String qn) {
        AbstractNode an = (AbstractNode) n;
        if (an == getDocumentElement()) {
            throw createDOMException(DOMException.NOT_SUPPORTED_ERR,
                                     "rename.document.element",
                                     new Object[] {});
        }
        int nt = n.getNodeType();
        if (nt != Node.ELEMENT_NODE && nt != Node.ATTRIBUTE_NODE) {
            throw createDOMException(DOMException.NOT_SUPPORTED_ERR,
                                     "rename.node",
                                     new Object[] { new Integer(nt),
                                                    n.getNodeName() });
        }
        if (xmlVersion.equals(XMLConstants.XML_VERSION_11)
                && !DOMUtilities.isValidName11(qn)
                || !DOMUtilities.isValidName(qn)) {
            throw createDOMException(DOMException.NOT_SUPPORTED_ERR,
                                     "wf.invalid.name",
                                     new Object[] { qn });
        }
        if (n.getOwnerDocument() != this) {
            throw createDOMException(DOMException.NOT_SUPPORTED_ERR,
                                     "node.from.wrong.document",
                                     new Object[] { new Integer(nt),
                                                    n.getNodeName() });
        }
        int i = qn.indexOf(':');
        if (i == 0 || i == qn.length() - 1) {
            throw createDOMException(DOMException.NAMESPACE_ERR,
                                     "qname",
                                     new Object[] { new Integer(nt),
                                                    n.getNodeName(),
                                                    qn });
        }
        String prefix = DOMUtilities.getPrefix(qn);
        if (ns != null && ns.length() == 0) {
            ns = null;
        }
        if (prefix != null && ns == null) {
            throw createDOMException(DOMException.NAMESPACE_ERR,
                                     "prefix",
                                     new Object[] { new Integer(nt),
                                                    n.getNodeName(),
                                                    prefix });
        }
        if (strictErrorChecking) {
            if (XMLConstants.XML_PREFIX.equals(prefix)
                    && !XMLConstants.XML_NAMESPACE_URI.equals(ns)
                    || XMLConstants.XMLNS_PREFIX.equals(prefix)
                        && !XMLConstants.XMLNS_NAMESPACE_URI.equals(ns)) {
                throw createDOMException(DOMException.NAMESPACE_ERR,
                                         "namespace",
                                         new Object[] { new Integer(nt),
                                                        n.getNodeName(),
                                                        ns });
            }
        }
        String prevNamespaceURI = n.getNamespaceURI();
        String prevNodeName = n.getNodeName();
        if (nt == Node.ELEMENT_NODE) {
            Node parent = n.getParentNode();
            AbstractElement e = (AbstractElement) createElementNS(ns, qn);
            EventSupport es1 = an.getEventSupport();
            if (es1 != null) {
                EventSupport es2 = e.getEventSupport();
                if (es2 == null) {
                    AbstractDOMImplementation di
                        = (AbstractDOMImplementation) implementation;
                    es2 = di.createEventSupport(e);
                    setEventsEnabled(true);
                    e.eventSupport = es2;
                }
                es1.moveEventListeners(e.getEventSupport());
            }
            e.userData = e.userData == null
                ? null
                : (HashMap) an.userData.clone();
            e.userDataHandlers = e.userDataHandlers == null
                ? null
                : (HashMap) an.userDataHandlers.clone();
            Node next = null;
            if (parent != null) {
                n.getNextSibling();
                parent.removeChild(n);
            }
            while (n.getFirstChild() != null) {
                e.appendChild(n.getFirstChild());
            }
            NamedNodeMap nnm = n.getAttributes();
            for (int j = 0; j < nnm.getLength(); j++) {
                Attr a = (Attr) nnm.item(j);
                e.setAttributeNodeNS(a);
            }
            if (parent != null) {
                if (next == null) {
                    parent.appendChild(e);
                } else {
                    parent.insertBefore(next, e);
                }
            }
            fireUserDataHandlers(UserDataHandler.NODE_RENAMED, n, e);
            if (getEventsEnabled()) {
                MutationNameEvent ev =
                    (MutationNameEvent) createEvent("MutationNameEvent");
                ev.initMutationNameEventNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                                           "DOMElementNameChanged",
                                           true,   
                                           false,  
                                           null,   
                                           prevNamespaceURI,
                                           prevNodeName);
                dispatchEvent(ev);
            }
            return e;
        } else {
            if (n instanceof AbstractAttrNS) {
                AbstractAttrNS a = (AbstractAttrNS) n;
                Element e = a.getOwnerElement();
                if (e != null) {
                    e.removeAttributeNode(a);
                }
                a.namespaceURI = ns;
                a.nodeName = qn;
                if (e != null) {
                    e.setAttributeNodeNS(a);
                }
                fireUserDataHandlers(UserDataHandler.NODE_RENAMED, a, null);
                if (getEventsEnabled()) {
                    MutationNameEvent ev =
                        (MutationNameEvent) createEvent("MutationNameEvent");
                    ev.initMutationNameEventNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                                               "DOMAttrNameChanged",
                                               true,   
                                               false,  
                                               a,      
                                               prevNamespaceURI,
                                               prevNodeName);
                    dispatchEvent(ev);
                }
                return a;
            } else {
                AbstractAttr a = (AbstractAttr) n;
                Element e = a.getOwnerElement();
                if (e != null) {
                    e.removeAttributeNode(a);
                }
                AbstractAttr a2 = (AbstractAttr) createAttributeNS(ns, qn);
                a2.setNodeValue(a.getNodeValue());
                a2.userData = a.userData == null
                    ? null
                    : (HashMap) a.userData.clone();
                a2.userDataHandlers = a.userDataHandlers == null
                    ? null
                    : (HashMap) a.userDataHandlers.clone();
                if (e != null) {
                    e.setAttributeNodeNS(a2);
                }
                fireUserDataHandlers(UserDataHandler.NODE_RENAMED, a, a2);
                if (getEventsEnabled()) {
                    MutationNameEvent ev
                        = (MutationNameEvent) createEvent("MutationNameEvent");
                    ev.initMutationNameEventNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                                               "DOMAttrNameChanged",
                                               true,   
                                               false,  
                                               a2,     
                                               prevNamespaceURI,
                                               prevNodeName);
                    dispatchEvent(ev);
                }
                return a2;
            }
        }
    }
    public void normalizeDocument() {
        if (domConfig == null) {
            domConfig = new DocumentConfiguration();
        }
        boolean cdataSections = domConfig.getBooleanParameter
            (DOMConstants.DOM_CDATA_SECTIONS_PARAM);
        boolean comments = domConfig.getBooleanParameter
            (DOMConstants.DOM_COMMENTS_PARAM);
        boolean elementContentWhitespace = domConfig.getBooleanParameter
            (DOMConstants.DOM_ELEMENT_CONTENT_WHITESPACE_PARAM);
        boolean namespaceDeclarations = domConfig.getBooleanParameter
            (DOMConstants.DOM_NAMESPACE_DECLARATIONS_PARAM);
        boolean namespaces = domConfig.getBooleanParameter
            (DOMConstants.DOM_NAMESPACES_PARAM);
        boolean splitCdataSections = domConfig.getBooleanParameter
            (DOMConstants.DOM_SPLIT_CDATA_SECTIONS_PARAM);
        DOMErrorHandler errorHandler = (DOMErrorHandler) domConfig.getParameter
            (DOMConstants.DOM_ERROR_HANDLER_PARAM);
        normalizeDocument(getDocumentElement(),
                          cdataSections,
                          comments,
                          elementContentWhitespace,
                          namespaceDeclarations,
                          namespaces,
                          splitCdataSections,
                          errorHandler);
    }
    protected boolean normalizeDocument(Element e,
                                        boolean cdataSections,
                                        boolean comments,
                                        boolean elementContentWhitepace,
                                        boolean namespaceDeclarations,
                                        boolean namespaces,
                                        boolean splitCdataSections,
                                        DOMErrorHandler errorHandler) {
        AbstractElement ae = (AbstractElement) e;
        Node n = e.getFirstChild();
        while (n != null) {
            int nt = n.getNodeType();
            if (nt == Node.TEXT_NODE
                    || !cdataSections && nt == Node.CDATA_SECTION_NODE) {
                Node t = n;
                StringBuffer sb = new StringBuffer();
                sb.append(t.getNodeValue());
                n = n.getNextSibling();
                while (n != null && (n.getNodeType() == Node.TEXT_NODE
                            || !cdataSections && n.getNodeType() == Node.CDATA_SECTION_NODE) ) {
                    sb.append(n.getNodeValue());
                    Node next = n.getNextSibling();
                    e.removeChild(n);
                    n = next;
                }
                String s = sb.toString();
                if (s.length() == 0) {
                    Node next = n.getNextSibling();       
                    e.removeChild(n);
                    n = next;
                    continue;
                }
                if (!s.equals(t.getNodeValue())) {
                    if (!cdataSections && nt == Node.TEXT_NODE) {
                        n = createTextNode(s);
                        e.replaceChild(n, t);
                    } else {
                        n = t;
                        t.setNodeValue(s);
                    }
                } else {
                    n = t;
                }
                if (!elementContentWhitepace) {
                    nt = n.getNodeType();
                    if (nt == Node.TEXT_NODE) {
                        AbstractText tn = (AbstractText) n;
                        if (tn.isElementContentWhitespace()) {
                            Node next = n.getNextSibling();
                            e.removeChild(n);
                            n = next;
                            continue;
                        }
                    }
                }
                if (nt == Node.CDATA_SECTION_NODE && splitCdataSections) {
                    if (!splitCdata(e, n, errorHandler)) {
                        return false;
                    }
                }
            } else if (nt == Node.CDATA_SECTION_NODE && splitCdataSections) {
                if (!splitCdata(e, n, errorHandler)) {
                    return false;
                }
            } else if (nt == Node.COMMENT_NODE && !comments) {
                Node next = n.getPreviousSibling();
                if (next == null) {
                    next = n.getNextSibling();
                }
                e.removeChild(n);
                n = next;
                continue;
            }
            n = n.getNextSibling();
        }
        NamedNodeMap nnm = e.getAttributes();
        LinkedList toRemove = new LinkedList();
        HashMap names = new HashMap();                    
        for (int i = 0; i < nnm.getLength(); i++) {
            Attr a = (Attr) nnm.item(i);
            String prefix = a.getPrefix();                
            if (a != null && XMLConstants.XMLNS_PREFIX.equals(prefix)
                    || a.getNodeName().equals(XMLConstants.XMLNS_PREFIX)) {
                if (!namespaceDeclarations) {
                    toRemove.add(a);
                } else {
                    String ns = a.getNodeValue();
                    if (a.getNodeValue().equals(XMLConstants.XMLNS_NAMESPACE_URI)
                            || !ns.equals(XMLConstants.XMLNS_NAMESPACE_URI)) {
                    } else {
                        names.put(prefix, ns);
                    }
                }
            }
        }
        if (!namespaceDeclarations) {
            Iterator i = toRemove.iterator();
            while (i.hasNext()) {
                e.removeAttributeNode((Attr) i.next());
            }
        } else {
            if (namespaces) {
                String ens = e.getNamespaceURI();
                if (ens != null) {
                    String eprefix = e.getPrefix();
                    if (!compareStrings(ae.lookupNamespaceURI(eprefix), ens)) {
                        e.setAttributeNS
                            (XMLConstants.XMLNS_NAMESPACE_URI,
                             eprefix == null ? XMLConstants.XMLNS_PREFIX : "xmlns:" + eprefix,
                             ens);
                    }
                } else {
                    if (e.getLocalName() == null) {
                    } else {
                        if (ae.lookupNamespaceURI(null) == null) {
                            e.setAttributeNS
                                (XMLConstants.XMLNS_NAMESPACE_URI,
                                 XMLConstants.XMLNS_PREFIX,
                                 "");
                        }
                    }
                }
                nnm = e.getAttributes();
                for (int i = 0; i < nnm.getLength(); i++) {
                    Attr a = (Attr) nnm.item(i);
                    String ans = a.getNamespaceURI();
                    if (ans != null) {
                        String apre = a.getPrefix();
                        if (apre != null
                                && (apre.equals(XMLConstants.XML_PREFIX)
                                    || apre.equals(XMLConstants.XMLNS_PREFIX))
                                || ans.equals(XMLConstants.XMLNS_NAMESPACE_URI)) {
                            continue;
                        }
                        String aprens = apre == null ? null : ae.lookupNamespaceURI(apre);
                        if (apre == null
                                || aprens == null
                                || !aprens.equals(ans)) {
                            String newpre = ae.lookupPrefix(ans);
                            if (newpre != null) {
                                a.setPrefix(newpre);
                            } else {
                                if (apre != null
                                        && ae.lookupNamespaceURI(apre) == null) {
                                    e.setAttributeNS
                                        (XMLConstants.XMLNS_NAMESPACE_URI,
                                         XMLConstants.XMLNS_PREFIX + ':' + apre,
                                         ans);
                                } else {
                                    int index = 1;
                                    for (;;) {
                                        newpre = "NS" + index;
                                        if (ae.lookupPrefix(newpre) == null) {
                                            e.setAttributeNS
                                                (XMLConstants.XMLNS_NAMESPACE_URI,
                                                 XMLConstants.XMLNS_PREFIX + ':' + newpre,
                                                 ans);
                                            a.setPrefix(newpre);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (a.getLocalName() == null) {
                        }
                    }
                }
            }
        }
        nnm = e.getAttributes();
        for (int i = 0; i < nnm.getLength(); i++) {
            Attr a = (Attr) nnm.item(i);
            if (!checkName(a.getNodeName())) {
                if (errorHandler != null) {
                    if (!errorHandler.handleError(createDOMError(
                            DOMConstants.DOM_INVALID_CHARACTER_IN_NODE_NAME_ERROR,
                            DOMError.SEVERITY_ERROR,
                            "wf.invalid.name",
                            new Object[] { a.getNodeName() },
                            a,
                            null))) {
                        return false;
                    }
                }
            }
            if (!checkChars(a.getNodeValue())) {
                if (errorHandler != null) {
                    if (!errorHandler.handleError(createDOMError(
                            DOMConstants.DOM_INVALID_CHARACTER_ERROR,
                            DOMError.SEVERITY_ERROR,
                            "wf.invalid.character",
                            new Object[] { new Integer(Node.ATTRIBUTE_NODE),
                                           a.getNodeName(),
                                           a.getNodeValue() },
                            a,
                            null))) {
                        return false;
                    }
                }
            }
        }
        for (Node m = e.getFirstChild(); m != null; m = m.getNextSibling()) {
            int nt = m.getNodeType();
            String s;
            switch (nt) {
                case Node.TEXT_NODE:
                    s = m.getNodeValue();
                    if (!checkChars(s)) {
                        if (errorHandler != null) {
                            if (!errorHandler.handleError(createDOMError(
                                    DOMConstants.DOM_INVALID_CHARACTER_ERROR,
                                    DOMError.SEVERITY_ERROR,
                                    "wf.invalid.character",
                                    new Object[] { new Integer(m.getNodeType()),
                                                   m.getNodeName(),
                                                   s },
                                    m,
                                    null))) {
                                return false;
                            }
                        }
                    }
                    break;
                case Node.COMMENT_NODE:
                    s = m.getNodeValue();
                    if (!checkChars(s)
                            || s.indexOf(XMLConstants.XML_DOUBLE_DASH) != -1
                            || s.charAt(s.length() - 1) == '-') {
                        if (errorHandler != null) {
                            if (!errorHandler.handleError(createDOMError(
                                    DOMConstants.DOM_INVALID_CHARACTER_ERROR,
                                    DOMError.SEVERITY_ERROR,
                                    "wf.invalid.character",
                                    new Object[] { new Integer(m.getNodeType()),
                                                   m.getNodeName(),
                                                   s },
                                    m,
                                    null))) {
                                return false;
                            }
                        }
                    }
                    break;
                case Node.CDATA_SECTION_NODE:
                    s = m.getNodeValue();
                    if (!checkChars(s)
                            || s.indexOf(XMLConstants.XML_CDATA_END) != -1) {
                        if (errorHandler != null) {
                            if (!errorHandler.handleError(createDOMError(
                                    DOMConstants.DOM_INVALID_CHARACTER_ERROR,
                                    DOMError.SEVERITY_ERROR,
                                    "wf.invalid.character",
                                    new Object[] { new Integer(m.getNodeType()),
                                                   m.getNodeName(),
                                                   s },
                                    m,
                                    null))) {
                                return false;
                            }
                        }
                    }
                    break;
                case Node.PROCESSING_INSTRUCTION_NODE:
                    if (m.getNodeName().equalsIgnoreCase
                            (XMLConstants.XML_PREFIX)) {
                        if (errorHandler != null) {
                            if (!errorHandler.handleError(createDOMError(
                                    DOMConstants.DOM_INVALID_CHARACTER_IN_NODE_NAME_ERROR,
                                    DOMError.SEVERITY_ERROR,
                                    "wf.invalid.name",
                                    new Object[] { m.getNodeName() },
                                    m,
                                    null))) {
                                return false;
                            }
                        }
                    }
                    s = m.getNodeValue();
                    if (!checkChars(s)
                            || s.indexOf(XMLConstants
                                .XML_PROCESSING_INSTRUCTION_END) != -1) {
                        if (errorHandler != null) {
                            if (!errorHandler.handleError(createDOMError(
                                    DOMConstants.DOM_INVALID_CHARACTER_ERROR,
                                    DOMError.SEVERITY_ERROR,
                                    "wf.invalid.character",
                                    new Object[] { new Integer(m.getNodeType()),
                                                   m.getNodeName(),
                                                   s },
                                    m,
                                    null))) {
                                return false;
                            }
                        }
                    }
                    break;
                case Node.ELEMENT_NODE:
                    if (!checkName(m.getNodeName())) {
                        if (errorHandler != null) {
                            if (!errorHandler.handleError(createDOMError(
                                    DOMConstants.DOM_INVALID_CHARACTER_IN_NODE_NAME_ERROR,
                                    DOMError.SEVERITY_ERROR,
                                    "wf.invalid.name",
                                    new Object[] { m.getNodeName() },
                                    m,
                                    null))) {
                                return false;
                            }
                        }
                    }
                    if (!normalizeDocument((Element) m,
                                           cdataSections,
                                           comments,
                                           elementContentWhitepace,
                                           namespaceDeclarations,
                                           namespaces,
                                           splitCdataSections,
                                           errorHandler)) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }
    protected boolean splitCdata(Element e,
                                 Node n,
                                 DOMErrorHandler errorHandler) {
        String s2 = n.getNodeValue();
        int index = s2.indexOf(XMLConstants.XML_CDATA_END);
        if (index != -1) {
            String before = s2.substring(0, index + 2);
            String after = s2.substring(index + 2);
            n.setNodeValue(before);
            Node next = n.getNextSibling();
            if (next == null) {
                e.appendChild(createCDATASection(after));
            } else {
                e.insertBefore(createCDATASection(after),
                               next);
            }
            if (errorHandler != null) {
                if (!errorHandler.handleError(createDOMError(
                    DOMConstants.DOM_CDATA_SECTIONS_SPLITTED_ERROR,
                    DOMError.SEVERITY_WARNING,
                    "cdata.section.split",
                    new Object[] {},
                    n,
                    null))) {
                    return false;
                }
            }
        }
        return true;
    }
    protected boolean checkChars(String s) {
        int len = s.length();
        if (xmlVersion.equals(XMLConstants.XML_VERSION_11)) {
            for (int i = 0; i < len; i++) {
                if (!DOMUtilities.isXML11Character(s.charAt(i))) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < len; i++) {
                if (!DOMUtilities.isXMLCharacter(s.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }
    protected boolean checkName(String s) {
        if (xmlVersion.equals(XMLConstants.XML_VERSION_11)) {
            return DOMUtilities.isValidName11(s);
        }
        return DOMUtilities.isValidName(s);
    }
    protected DOMError createDOMError(String type,
                                      short severity,
                                      String key,
                                      Object[] args,
                                      Node related,
                                      Exception e) {
        try {
            return new DocumentError(type,
                                     severity,
                                     getCurrentDocument().formatMessage(key, args),
                                     related,
                                     e);
        } catch (Exception ex) {
            return new DocumentError(type,
                                     severity,
                                     key,
                                     related,
                                     e);
        }
    }
    public void setTextContent(String s) throws DOMException {
    }
    public void setXBLManager(XBLManager m) {
        boolean wasProcessing = xblManager.isProcessing();
        xblManager.stopProcessing();
        if (m == null) {
            m = new GenericXBLManager();
        }
        xblManager = m;
        if (wasProcessing) {
            xblManager.startProcessing();
        }
    }
    public XBLManager getXBLManager() {
        return xblManager;
    }
    protected class DocumentError implements DOMError {
        protected String type;
        protected short severity;
        protected String message;
        protected Node relatedNode;
        protected Object relatedException;
        protected DOMLocator domLocator;
        public DocumentError(String type,
                             short severity,
                             String message,
                             Node relatedNode,
                             Exception relatedException) {
            this.type = type;
            this.severity = severity;
            this.message = message;
            this.relatedNode = relatedNode;
            this.relatedException = relatedException;
        }
        public String getType() {
            return type;
        }
        public short getSeverity() {
            return severity;
        }
        public String getMessage() {
            return message;
        }
        public Object getRelatedData() {
            return relatedNode;
        }
        public Object getRelatedException() {
            return relatedException;
        }
        public DOMLocator getLocation() {
            if (domLocator == null) {
                domLocator = new ErrorLocation(relatedNode);
            }
            return domLocator;
        }
        protected class ErrorLocation implements DOMLocator {
            protected Node node;
            public ErrorLocation(Node n) {
                node = n;
            }
            public int getLineNumber() {
                return -1;
            }
            public int getColumnNumber() {
                return -1;
            }
            public int getByteOffset() {
                return -1;
            }
            public int getUtf16Offset() {
                return -1;
            }
            public Node getRelatedNode() {
                return node;
            }
            public String getUri() {
                AbstractDocument doc
                    = (AbstractDocument) node.getOwnerDocument();
                return doc.getDocumentURI();
            }
        }
    }
    protected class DocumentConfiguration implements DOMConfiguration {
        protected String[] booleanParamNames = {
            DOMConstants.DOM_CANONICAL_FORM_PARAM,
            DOMConstants.DOM_CDATA_SECTIONS_PARAM,
            DOMConstants.DOM_CHECK_CHARACTER_NORMALIZATION_PARAM,
            DOMConstants.DOM_COMMENTS_PARAM,
            DOMConstants.DOM_DATATYPE_NORMALIZATION_PARAM,
            DOMConstants.DOM_ELEMENT_CONTENT_WHITESPACE_PARAM,
            DOMConstants.DOM_ENTITIES_PARAM,
            DOMConstants.DOM_INFOSET_PARAM,
            DOMConstants.DOM_NAMESPACES_PARAM,
            DOMConstants.DOM_NAMESPACE_DECLARATIONS_PARAM,
            DOMConstants.DOM_NORMALIZE_CHARACTERS_PARAM,
            DOMConstants.DOM_SPLIT_CDATA_SECTIONS_PARAM,
            DOMConstants.DOM_VALIDATE_PARAM,
            DOMConstants.DOM_VALIDATE_IF_SCHEMA_PARAM,
            DOMConstants.DOM_WELL_FORMED_PARAM
        };
        protected boolean[] booleanParamValues = {
            false,      
            true,       
            false,      
            true,       
            false,      
            false,      
            true,       
            false,      
            true,       
            true,       
            false,      
            true,       
            false,      
            false,      
            true        
        };
        protected boolean[] booleanParamReadOnly = {
            true,       
            false,      
            true,       
            false,      
            true,       
            false,      
            false,      
            false,      
            false,      
            false,      
            true,       
            false,      
            true,       
            true,       
            false       
        };
        protected Map booleanParamIndexes = new HashMap();
        {
            for (int i = 0; i < booleanParamNames.length; i++) {
                booleanParamIndexes.put(booleanParamNames[i], new Integer(i));
            }
        }
        protected Object errorHandler;
        protected ParameterNameList paramNameList;
        public void setParameter(String name, Object value) {
            if (DOMConstants.DOM_ERROR_HANDLER_PARAM.equals(name)) {
                if (value != null && !(value instanceof DOMErrorHandler)) {
                    throw createDOMException
                        ((short) 17 ,
                         "domconfig.param.type",
                         new Object[] { name });
                }
                errorHandler = value;
                return;
            }
            Integer i = (Integer) booleanParamIndexes.get(name);
            if (i == null) {
                throw createDOMException
                    (DOMException.NOT_FOUND_ERR,
                     "domconfig.param.not.found",
                     new Object[] { name });
            }
            if (value == null) {
                throw createDOMException
                    (DOMException.NOT_SUPPORTED_ERR,
                     "domconfig.param.value",
                     new Object[] { name });
            }
            if (!(value instanceof Boolean)) {
                throw createDOMException
                    ((short) 17 ,
                     "domconfig.param.type",
                     new Object[] { name });
            }
            int index = i.intValue();
            boolean val = ((Boolean) value).booleanValue();
            if (booleanParamReadOnly[index]
                    && booleanParamValues[index] != val) {
                throw createDOMException
                    (DOMException.NOT_SUPPORTED_ERR,
                     "domconfig.param.value",
                     new Object[] { name });
            }
            booleanParamValues[index] = val;
            if (name.equals(DOMConstants.DOM_INFOSET_PARAM)) {
                setParameter(DOMConstants.DOM_VALIDATE_IF_SCHEMA_PARAM, Boolean.FALSE);
                setParameter(DOMConstants.DOM_ENTITIES_PARAM, Boolean.FALSE);
                setParameter(DOMConstants.DOM_DATATYPE_NORMALIZATION_PARAM, Boolean.FALSE);
                setParameter(DOMConstants.DOM_CDATA_SECTIONS_PARAM, Boolean.FALSE);
                setParameter(DOMConstants.DOM_WELL_FORMED_PARAM, Boolean.TRUE);
                setParameter(DOMConstants.DOM_ELEMENT_CONTENT_WHITESPACE_PARAM, Boolean.TRUE);
                setParameter(DOMConstants.DOM_COMMENTS_PARAM, Boolean.TRUE);
                setParameter(DOMConstants.DOM_NAMESPACES_PARAM, Boolean.TRUE);
            }
        }
        public Object getParameter(String name) {
            if (DOMConstants.DOM_ERROR_HANDLER_PARAM.equals(name)) {
                return errorHandler;
            }
            Integer index = (Integer) booleanParamIndexes.get(name);
            if (index == null) {
                throw createDOMException
                    (DOMException.NOT_FOUND_ERR,
                     "domconfig.param.not.found",
                     new Object[] { name });
            }
            return booleanParamValues[index.intValue()] ? Boolean.TRUE
                                                        : Boolean.FALSE;
        }
        public boolean getBooleanParameter(String name) {
            Boolean b = (Boolean) getParameter(name);
            return b.booleanValue();
        }
        public boolean canSetParameter(String name, Object value) {
            if (name.equals(DOMConstants.DOM_ERROR_HANDLER_PARAM)) {
                return value == null || value instanceof DOMErrorHandler;
            }
            Integer i = (Integer) booleanParamIndexes.get(name);
            if (i == null || value == null || !(value instanceof Boolean)) {
                return false;
            }
            int index = i.intValue();
            boolean val = ((Boolean) value).booleanValue();
            return !booleanParamReadOnly[index]
                || booleanParamValues[index] == val;
        }
        public DOMStringList getParameterNames() {
            if (paramNameList == null) {
                paramNameList = new ParameterNameList();
            }
            return paramNameList;
        }
        protected class ParameterNameList implements DOMStringList {
            public String item(int index) {
                if (index < 0) {
                    return null;
                }
                if (index < booleanParamNames.length) {
                    return booleanParamNames[index];
                }
                if (index == booleanParamNames.length) {
                    return DOMConstants.DOM_ERROR_HANDLER_PARAM;
                }
                return null;
            }
            public int getLength() {
                return booleanParamNames.length + 1;
            }
            public boolean contains(String s) {
                if (DOMConstants.DOM_ERROR_HANDLER_PARAM.equals(s)) {
                    return true;
                }
                for (int i = 0; i < booleanParamNames.length; i++) {
                    if (booleanParamNames[i].equals(s)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }
    public XPathExpression createExpression(String expression,
                                            XPathNSResolver resolver)
            throws DOMException, XPathException {
        return new XPathExpr(expression, resolver);
    }
    public XPathNSResolver createNSResolver(Node n) {
        return new XPathNodeNSResolver(n);
    }
    public Object evaluate(String expression,
                           Node contextNode,
                           XPathNSResolver resolver,
                           short type,
                           Object result)
            throws XPathException, DOMException {
        XPathExpression xpath = createExpression(expression, resolver);
        return xpath.evaluate(contextNode, type, result);
    }
    public XPathException createXPathException(short type,
                                               String key,
                                               Object[] args) {
        try {
            return new XPathException(type, formatMessage(key, args));
        } catch (Exception e) {
            return new XPathException(type, key);
        }
    }
    protected class XPathExpr implements XPathExpression {
        protected XPath xpath;
        protected XPathNSResolver resolver;
        protected NSPrefixResolver prefixResolver;
        protected XPathContext context;
        public XPathExpr(String expr, XPathNSResolver res)
                throws DOMException, XPathException {
            resolver = res;
            prefixResolver = new NSPrefixResolver();
            try {
                xpath = new XPath(expr, null, prefixResolver, XPath.SELECT);
                context = new XPathContext();
            } catch (javax.xml.transform.TransformerException te) {
                throw createXPathException
                    (XPathException.INVALID_EXPRESSION_ERR,
                     "xpath.invalid.expression",
                     new Object[] { expr, te.getMessage() });
            }
        }
        public Object evaluate(Node contextNode, short type, Object res)
                throws XPathException, DOMException {
            if (contextNode.getNodeType() != DOCUMENT_NODE
                    && contextNode.getOwnerDocument() != AbstractDocument.this
                    || contextNode.getNodeType() == DOCUMENT_NODE
                    && contextNode != AbstractDocument.this) {
                throw createDOMException
                    (DOMException.WRONG_DOCUMENT_ERR,
                     "node.from.wrong.document",
                     new Object[] { new Integer(contextNode.getNodeType()),
                                    contextNode.getNodeName() });
            }
            if (type < 0 || type > 9) {
                throw createDOMException(DOMException.NOT_SUPPORTED_ERR,
                                         "xpath.invalid.result.type",
                                         new Object[] { new Integer(type) });
            }
            switch (contextNode.getNodeType()) {
                case ENTITY_REFERENCE_NODE:
                case ENTITY_NODE:
                case DOCUMENT_TYPE_NODE:
                case DOCUMENT_FRAGMENT_NODE:
                case NOTATION_NODE:
                    throw createDOMException
                        (DOMException.NOT_SUPPORTED_ERR,
                         "xpath.invalid.context.node",
                         new Object[] { new Integer(contextNode.getNodeType()),
                                        contextNode.getNodeName() });
            }
            context.reset();
            XObject result = null;
            try {
                result = xpath.execute(context, contextNode, prefixResolver);
            } catch (javax.xml.transform.TransformerException te) {
                throw createXPathException
                    (XPathException.INVALID_EXPRESSION_ERR,
                     "xpath.error",
                     new Object[] { xpath.getPatternString(),
                                    te.getMessage() });
            }
            try {
                switch (type) {
                    case XPathResult.ANY_UNORDERED_NODE_TYPE:
                    case XPathResult.FIRST_ORDERED_NODE_TYPE:
                        return convertSingleNode(result, type);
                    case XPathResult.BOOLEAN_TYPE:
                        return convertBoolean(result);
                    case XPathResult.NUMBER_TYPE:
                        return convertNumber(result);
                    case XPathResult.ORDERED_NODE_ITERATOR_TYPE:
                    case XPathResult.UNORDERED_NODE_ITERATOR_TYPE:
                    case XPathResult.ORDERED_NODE_SNAPSHOT_TYPE:
                    case XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE:
                        return convertNodeIterator(result, type);
                    case XPathResult.STRING_TYPE:
                        return convertString(result);
                    case XPathResult.ANY_TYPE:
                        switch (result.getType()) {
                            case XObject.CLASS_BOOLEAN:
                                return convertBoolean(result);
                            case XObject.CLASS_NUMBER:
                                return convertNumber(result);
                            case XObject.CLASS_STRING:
                                return convertString(result);
                            case XObject.CLASS_NODESET:
                                return convertNodeIterator
                                    (result,
                                     XPathResult.UNORDERED_NODE_ITERATOR_TYPE);
                        }
                }
            } catch (javax.xml.transform.TransformerException te) {
                throw createXPathException
                    (XPathException.TYPE_ERR,
                     "xpath.cannot.convert.result",
                     new Object[] { new Integer(type),
                                    te.getMessage() });
            }
            return null;
        }
        protected Result convertSingleNode(XObject xo, short type)
                throws javax.xml.transform.TransformerException {
            return new Result(xo.nodelist().item(0), type);
        }
        protected Result convertBoolean(XObject xo)
                throws javax.xml.transform.TransformerException {
            return new Result(xo.bool());
        }
        protected Result convertNumber(XObject xo)
                throws javax.xml.transform.TransformerException {
            return new Result(xo.num());
        }
        protected Result convertString(XObject xo) {
            return new Result(xo.str());
        }
        protected Result convertNodeIterator(XObject xo, short type)
                throws javax.xml.transform.TransformerException {
            return new Result(xo.nodelist(), type);
        }
        public class Result implements XPathResult {
            protected short resultType;
            protected double numberValue;
            protected String stringValue;
            protected boolean booleanValue;
            protected Node singleNodeValue;
            protected NodeList iterator;
            protected int iteratorPosition;
            public Result(Node n, short type) {
                resultType = type;
                singleNodeValue = n;
            }
            public Result(boolean b)
                    throws javax.xml.transform.TransformerException {
                resultType = BOOLEAN_TYPE;
                booleanValue = b;
            }
            public Result(double d)
                    throws javax.xml.transform.TransformerException {
                resultType = NUMBER_TYPE;
                numberValue = d;
            }
            public Result(String s) {
                resultType = STRING_TYPE;
                stringValue = s;
            }
            public Result(NodeList nl, short type) {
                resultType = type;
                iterator = nl;
            }
            public short getResultType() {
                return resultType;
            }
            public boolean getBooleanValue() {
                if (resultType != BOOLEAN_TYPE) {
                    throw createXPathException
                        (XPathException.TYPE_ERR,
                         "xpath.invalid.result.type",
                         new Object[] { new Integer(resultType) });
                }
                return booleanValue;
            }
            public double getNumberValue() {
                if (resultType != NUMBER_TYPE) {
                    throw createXPathException
                        (XPathException.TYPE_ERR,
                         "xpath.invalid.result.type",
                         new Object[] { new Integer(resultType) });
                }
                return numberValue;
            }
            public String getStringValue() {
                if (resultType != STRING_TYPE) {
                    throw createXPathException
                        (XPathException.TYPE_ERR,
                         "xpath.invalid.result.type",
                         new Object[] { new Integer(resultType) });
                }
                return stringValue;
            }
            public Node getSingleNodeValue() {
                if (resultType != ANY_UNORDERED_NODE_TYPE
                        && resultType != FIRST_ORDERED_NODE_TYPE) {
                    throw createXPathException
                        (XPathException.TYPE_ERR,
                         "xpath.invalid.result.type",
                         new Object[] { new Integer(resultType) });
                }
                return singleNodeValue;
            }
            public boolean getInvalidIteratorState() {
                return false;
            }
            public int getSnapshotLength() {
                if (resultType != UNORDERED_NODE_SNAPSHOT_TYPE
                        && resultType != ORDERED_NODE_SNAPSHOT_TYPE) {
                    throw createXPathException
                        (XPathException.TYPE_ERR,
                         "xpath.invalid.result.type",
                         new Object[] { new Integer(resultType) });
                }
                return iterator.getLength();
            }
            public Node iterateNext() {
                if (resultType != UNORDERED_NODE_ITERATOR_TYPE
                        && resultType != ORDERED_NODE_ITERATOR_TYPE) {
                    throw createXPathException
                        (XPathException.TYPE_ERR,
                         "xpath.invalid.result.type",
                         new Object[] { new Integer(resultType) });
                }
                return iterator.item(iteratorPosition++);
            }
            public Node snapshotItem(int i) {
                if (resultType != UNORDERED_NODE_SNAPSHOT_TYPE
                        && resultType != ORDERED_NODE_SNAPSHOT_TYPE) {
                    throw createXPathException
                        (XPathException.TYPE_ERR,
                         "xpath.invalid.result.type",
                         new Object[] { new Integer(resultType) });
                }
                return iterator.item(i);
            }
        }
        protected class NSPrefixResolver implements PrefixResolver {
            public String getBaseIdentifier() {
                return null;
            }
            public String getNamespaceForPrefix(String prefix) {
                if (resolver == null) {
                    return null;
                }
                return resolver.lookupNamespaceURI(prefix);
            }
            public String getNamespaceForPrefix(String prefix, Node context) {
                if (resolver == null) {
                    return null;
                }
                return resolver.lookupNamespaceURI(prefix);
            }
            public boolean handlesNullPrefixes() {
                return false;
            }
        }
    }
    protected class XPathNodeNSResolver implements XPathNSResolver {
        protected Node contextNode;
        public XPathNodeNSResolver(Node n) {
            contextNode = n;
        }
        public String lookupNamespaceURI(String prefix) {
            return ((AbstractNode) contextNode).lookupNamespaceURI(prefix);
        }
    }
    public Node getXblParentNode() {
        return xblManager.getXblParentNode(this);
    }
    public NodeList getXblChildNodes() {
        return xblManager.getXblChildNodes(this);
    }
    public NodeList getXblScopedChildNodes() {
        return xblManager.getXblScopedChildNodes(this);
    }
    public Node getXblFirstChild() {
        return xblManager.getXblFirstChild(this);
    }
    public Node getXblLastChild() {
        return xblManager.getXblLastChild(this);
    }
    public Node getXblPreviousSibling() {
        return xblManager.getXblPreviousSibling(this);
    }
    public Node getXblNextSibling() {
        return xblManager.getXblNextSibling(this);
    }
    public Element getXblFirstElementChild() {
        return xblManager.getXblFirstElementChild(this);
    }
    public Element getXblLastElementChild() {
        return xblManager.getXblLastElementChild(this);
    }
    public Element getXblPreviousElementSibling() {
        return xblManager.getXblPreviousElementSibling(this);
    }
    public Element getXblNextElementSibling() {
        return xblManager.getXblNextElementSibling(this);
    }
    public Element getXblBoundElement() {
        return xblManager.getXblBoundElement(this);
    }
    public Element getXblShadowTree() {
        return xblManager.getXblShadowTree(this);
    }
    public NodeList getXblDefinitions() {
        return xblManager.getXblDefinitions(this);
    }
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeObject(implementation.getClass().getName());
    }
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        localizableSupport = new LocalizableSupport
            (RESOURCES, getClass().getClassLoader());
        Class c = Class.forName((String)s.readObject());
        try {
            Method m = c.getMethod("getDOMImplementation", (Class[])null);
            implementation = (DOMImplementation)m.invoke(null, (Object[])null);
        } catch (Exception e) {
            try {
                implementation = (DOMImplementation)c.newInstance();
            } catch (Exception ex) {
            }
        }
    }
}
