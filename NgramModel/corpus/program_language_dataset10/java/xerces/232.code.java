package org.apache.xerces.dom;
import org.apache.xerces.util.URI;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.ElementTraversal;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.TypeInfo;
public class ElementImpl
    extends ParentNode
    implements Element, ElementTraversal, TypeInfo {
    static final long serialVersionUID = 3717253516652722278L;
    protected String name;
    protected AttributeMap attributes;
    public ElementImpl(CoreDocumentImpl ownerDoc, String name) {
    	super(ownerDoc);
        this.name = name;
        needsSyncData(true);    
    }
    protected ElementImpl() {}
    void rename(String name) {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (ownerDocument.errorChecking) {
            int colon1 = name.indexOf(':');
            if(colon1 != -1){
                String msg =
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "NAMESPACE_ERR",
                            null);
                throw new DOMException(DOMException.NAMESPACE_ERR, msg);
            }
            if (!CoreDocumentImpl.isXMLName(name, ownerDocument.isXML11Version())) {
                String msg = DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN,
                        "INVALID_CHARACTER_ERR", null);
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR,
                        msg);
            }
        }
        this.name = name;
        reconcileDefaultAttributes();
    }
    public short getNodeType() {
        return Node.ELEMENT_NODE;
    }
    public String getNodeName() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return name;
    }
    public NamedNodeMap getAttributes() {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (attributes == null) {
            attributes = new AttributeMap(this, null);
        }
        return attributes;
    } 
    public Node cloneNode(boolean deep) {
    	ElementImpl newnode = (ElementImpl) super.cloneNode(deep);
        if (attributes != null) {
            newnode.attributes = (AttributeMap) attributes.cloneMap(newnode);
        }
    	return newnode;
    } 
    public String getBaseURI() {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (attributes != null) {
            final Attr attrNode = getXMLBaseAttribute();
            if (attrNode != null) {
                final String uri =  attrNode.getNodeValue();
                if (uri.length() != 0) {
                    try {
                        URI _uri = new URI(uri, true);
                        if (_uri.isAbsoluteURI()) {
                            return _uri.toString();
                        }
                        String parentBaseURI = (this.ownerNode != null) ? this.ownerNode.getBaseURI() : null;
                        if (parentBaseURI != null) {
                            try {
                                URI _parentBaseURI = new URI(parentBaseURI);
                                _uri.absolutize(_parentBaseURI);
                                return _uri.toString();
                            }
                            catch (org.apache.xerces.util.URI.MalformedURIException ex) {
                                return null;
                            }
                        }
                        return null; 
                    }
                    catch (org.apache.xerces.util.URI.MalformedURIException ex) {
                        return null;
                    }
                }
            }
        }
        return (this.ownerNode != null) ? this.ownerNode.getBaseURI() : null;
    } 
    protected Attr getXMLBaseAttribute() {
        return (Attr) attributes.getNamedItem("xml:base");
    } 
    protected void setOwnerDocument(CoreDocumentImpl doc) {
        super.setOwnerDocument(doc);
        if (attributes != null) {
            attributes.setOwnerDocument(doc);
        }
    }
    public String getAttribute(String name) {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (attributes == null) {
            return "";
        }
        Attr attr = (Attr)(attributes.getNamedItem(name));
        return (attr == null) ? "" : attr.getValue();
    } 
    public Attr getAttributeNode(String name) {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (attributes == null) {
            return null;
        }
        return (Attr)attributes.getNamedItem(name);
    } 
    public NodeList getElementsByTagName(String tagname) {
    	return new DeepNodeListImpl(this,tagname);
    }
    public String getTagName() {
        if (needsSyncData()) {
            synchronizeData();
        }
    	return name;
    }
    public void normalize() {
        if (isNormalized()) {
            return;
        }
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        ChildNode kid, next;
        for (kid = firstChild; kid != null; kid = next) {
            next = kid.nextSibling;
            if ( kid.getNodeType() == Node.TEXT_NODE )
            {
                if ( next!=null && next.getNodeType() == Node.TEXT_NODE )
                {
                    ((Text)kid).appendData(next.getNodeValue());
                    removeChild( next );
                    next = kid; 
                }
                else
                {
                    if ( kid.getNodeValue() == null || kid.getNodeValue().length() == 0 ) {
                        removeChild( kid );
                    }
                }
            }
            else if (kid.getNodeType() == Node.ELEMENT_NODE) {
                kid.normalize();
            }
        }
        if ( attributes!=null )
        {
            for( int i=0; i<attributes.getLength(); ++i )
            {
                Node attr = attributes.item(i);
                attr.normalize();
            }
        }
        isNormalized(true);
    } 
    public void removeAttribute(String name) {
    	if (ownerDocument.errorChecking && isReadOnly()) {
            String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, msg);
        }
        if (needsSyncData()) {
            synchronizeData();
        }
        if (attributes == null) {
            return;
        }
        attributes.safeRemoveNamedItem(name);
    } 
    public Attr removeAttributeNode(Attr oldAttr)
        throws DOMException {
    	if (ownerDocument.errorChecking && isReadOnly()) {
            String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, msg);
        }
        if (needsSyncData()) {
            synchronizeData();
        }
        if (attributes == null) {
            String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null);
            throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
        }
        return (Attr) attributes.removeItem(oldAttr, true);
    } 
	public void setAttribute(String name, String value) {
		if (ownerDocument.errorChecking && isReadOnly()) {
			String msg =
				DOMMessageFormatter.formatMessage(
					DOMMessageFormatter.DOM_DOMAIN,
					"NO_MODIFICATION_ALLOWED_ERR",
					null);
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, msg);
		}
		if (needsSyncData()) {
			synchronizeData();
		}
		Attr newAttr = getAttributeNode(name);
		if (newAttr == null) {
			newAttr = getOwnerDocument().createAttribute(name);
			if (attributes == null) {
				attributes = new AttributeMap(this, null);
			}
			newAttr.setNodeValue(value);
			attributes.setNamedItem(newAttr);
		}
		else {
			newAttr.setNodeValue(value);
		}
	} 
    public Attr setAttributeNode(Attr newAttr)
        throws DOMException
        {
        if (needsSyncData()) {
            synchronizeData();
        }
    	if (ownerDocument.errorChecking) {
            if (isReadOnly()) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
                throw new DOMException(
                                     DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     msg);
            }
            if (newAttr.getOwnerDocument() != ownerDocument) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null);
    		    throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
            }
        }
        if (attributes == null) {
            attributes = new AttributeMap(this, null);
        }
    	return (Attr) attributes.setNamedItem(newAttr);
    } 
    public String getAttributeNS(String namespaceURI, String localName) {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (attributes == null) {
            return "";
        }
        Attr attr = (Attr)(attributes.getNamedItemNS(namespaceURI, localName));
        return (attr == null) ? "" : attr.getValue();
    } 
     public void setAttributeNS(String namespaceURI,String qualifiedName,
		                          String value) {
		if (ownerDocument.errorChecking && isReadOnly()) {
			String msg =
				DOMMessageFormatter.formatMessage(
					DOMMessageFormatter.DOM_DOMAIN,
					"NO_MODIFICATION_ALLOWED_ERR",
					null);
			throw new DOMException(
				DOMException.NO_MODIFICATION_ALLOWED_ERR,
				msg);
		}
		if (needsSyncData()) {
			synchronizeData();
		}
		int index = qualifiedName.indexOf(':');
		String prefix, localName;
		if (index < 0) {
			prefix = null;
			localName = qualifiedName;
		}
		else {
			prefix = qualifiedName.substring(0, index);
			localName = qualifiedName.substring(index + 1);
		}
		Attr newAttr = getAttributeNodeNS(namespaceURI, localName);
		if (newAttr == null) {
			newAttr = getOwnerDocument().createAttributeNS(
					namespaceURI,
					qualifiedName);
			if (attributes == null) {
				attributes = new AttributeMap(this, null);
			}
			newAttr.setNodeValue(value);
			attributes.setNamedItemNS(newAttr);
		}
		else {
            if (newAttr instanceof AttrNSImpl){
                ((AttrNSImpl)newAttr).name= (prefix!=null)?(prefix+":"+localName):localName;
            }
            else {
                newAttr = ((CoreDocumentImpl)getOwnerDocument()).createAttributeNS(namespaceURI, qualifiedName, localName);
                attributes.setNamedItemNS(newAttr);
            }
			newAttr.setNodeValue(value);
		}
    } 
    public void removeAttributeNS(String namespaceURI, String localName) {
    	if (ownerDocument.errorChecking && isReadOnly()) {
            String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, msg);
        }
        if (needsSyncData()) {
            synchronizeData();
        }
        if (attributes == null) {
            return;
        }
        attributes.safeRemoveNamedItemNS(namespaceURI, localName);
    } 
    public Attr getAttributeNodeNS(String namespaceURI, String localName){
        if (needsSyncData()) {
            synchronizeData();
        }
        if (attributes == null) {
            return null;
        }
        return (Attr)attributes.getNamedItemNS(namespaceURI, localName);
    } 
    public Attr setAttributeNodeNS(Attr newAttr)
        throws DOMException
        {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (ownerDocument.errorChecking) {
            if (isReadOnly()) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
    		    throw new DOMException(
                                     DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     msg);
            }
            if (newAttr.getOwnerDocument() != ownerDocument) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null);
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
            }
        }
        if (attributes == null) {
            attributes = new AttributeMap(this, null);
        }
    	return (Attr) attributes.setNamedItemNS(newAttr);
    } 
    protected int setXercesAttributeNode (Attr attr){
        if (needsSyncData()) {
            synchronizeData();
        }
        if (attributes == null) {
            attributes = new AttributeMap(this, null);
        }
        return attributes.addItem(attr);
    }
    protected int getXercesAttribute(String namespaceURI, String localName){
        if (needsSyncData()) {
            synchronizeData();
        }
        if (attributes == null) {
            return -1;
        }
        return attributes.getNamedItemIndex(namespaceURI, localName);
    }
    public boolean hasAttributes() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return (attributes != null && attributes.getLength() != 0);
    }
    public boolean hasAttribute(String name) {
        return getAttributeNode(name) != null;
    }
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return getAttributeNodeNS(namespaceURI, localName) != null;
    }
    public NodeList getElementsByTagNameNS(String namespaceURI,
                                           String localName) {
    	return new DeepNodeListImpl(this, namespaceURI, localName);
    }
    public boolean isEqualNode(Node arg) {
        if (!super.isEqualNode(arg)) {
            return false;
        }
        boolean hasAttrs = hasAttributes();
        if (hasAttrs != ((Element) arg).hasAttributes()) {
            return false;
        }
        if (hasAttrs) {
            NamedNodeMap map1 = getAttributes();
            NamedNodeMap map2 = ((Element) arg).getAttributes();
            int len = map1.getLength();
            if (len != map2.getLength()) {
                return false;
            }
            for (int i = 0; i < len; i++) {
                Node n1 = map1.item(i);
                if (n1.getLocalName() == null) { 
                    Node n2 = map2.getNamedItem(n1.getNodeName());
                    if (n2 == null || !((NodeImpl) n1).isEqualNode(n2)) {
                        return false;
                    }
                }
                else {
                    Node n2 = map2.getNamedItemNS(n1.getNamespaceURI(),
                                                  n1.getLocalName());
                    if (n2 == null || !((NodeImpl) n1).isEqualNode(n2)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    public void setIdAttributeNode(Attr at, boolean makeId) {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (ownerDocument.errorChecking) {
            if (isReadOnly()) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
                throw new DOMException(
                                     DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     msg);
            }
            if (at.getOwnerElement() != this) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null);
                throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
            }
        }
        ((AttrImpl) at).isIdAttribute(makeId);
        if (!makeId) {
            ownerDocument.removeIdentifier(at.getValue());
        }
        else {
            ownerDocument.putIdentifier(at.getValue(), this);
        }
    }
    public void setIdAttribute(String name, boolean makeId) {
        if (needsSyncData()) {
            synchronizeData();
        }
        Attr at = getAttributeNode(name);
		if( at == null){
       		String msg = DOMMessageFormatter.formatMessage(
									DOMMessageFormatter.DOM_DOMAIN, 
									"NOT_FOUND_ERR", null);
            throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
		}
		if (ownerDocument.errorChecking) {
            if (isReadOnly()) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
                throw new DOMException(
                                     DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     msg);
            }
            if (at.getOwnerElement() != this) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null);
                throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
            }
        }
        ((AttrImpl) at).isIdAttribute(makeId);
        if (!makeId) {
            ownerDocument.removeIdentifier(at.getValue());
        }
        else {
            ownerDocument.putIdentifier(at.getValue(), this);
        }
    }
    public void setIdAttributeNS(String namespaceURI, String localName,
                                    boolean makeId) {
        if (needsSyncData()) {
            synchronizeData();
        }
        Attr at = getAttributeNodeNS(namespaceURI, localName);
		if( at == null){
       		String msg = DOMMessageFormatter.formatMessage(
									DOMMessageFormatter.DOM_DOMAIN, 
									"NOT_FOUND_ERR", null);
            throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
		}
		if (ownerDocument.errorChecking) {
            if (isReadOnly()) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
                throw new DOMException(
                                     DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     msg);
            }
            if (at.getOwnerElement() != this) {
                String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null);
                throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
            }
        }
        ((AttrImpl) at).isIdAttribute(makeId);
        if (!makeId) {
            ownerDocument.removeIdentifier(at.getValue());
        }
        else {
            ownerDocument.putIdentifier(at.getValue(), this);
        }
   }
     public String getTypeName() {
        return null;
     }
    public String getTypeNamespace() {
        return null;
    }
    public boolean isDerivedFrom(String typeNamespaceArg, 
                                 String typeNameArg, 
                                 int derivationMethod) {
        return false;
    }
    public TypeInfo getSchemaTypeInfo(){
        if(needsSyncData()) {
            synchronizeData();
        }
        return this;
    }
    public void setReadOnly(boolean readOnly, boolean deep) {
    	super.setReadOnly(readOnly,deep);
        if (attributes != null) {
            attributes.setReadOnly(readOnly,true);
        }
    }
    protected void synchronizeData() {
        needsSyncData(false);
        boolean orig = ownerDocument.getMutationEvents();
        ownerDocument.setMutationEvents(false);
        setupDefaultAttributes();
        ownerDocument.setMutationEvents(orig);
    } 
    void moveSpecifiedAttributes(ElementImpl el) {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (el.hasAttributes()) {
            if (attributes == null) {
                attributes = new AttributeMap(this, null);
            }
            attributes.moveSpecifiedAttributes(el.attributes);
        }
    }
    protected void setupDefaultAttributes() {
        NamedNodeMapImpl defaults = getDefaultAttributes();
        if (defaults != null) {
            attributes = new AttributeMap(this, defaults);
        }
    }
    protected void reconcileDefaultAttributes() {
        if (attributes != null) {
            NamedNodeMapImpl defaults = getDefaultAttributes();
            attributes.reconcileDefaults(defaults);
        }
    }
    protected NamedNodeMapImpl getDefaultAttributes() {
    	DocumentTypeImpl doctype =
            (DocumentTypeImpl) ownerDocument.getDoctype();
    	if (doctype == null) {
            return null;
        }
        ElementDefinitionImpl eldef =
            (ElementDefinitionImpl)doctype.getElements()
                                               .getNamedItem(getNodeName());
        if (eldef == null) {
            return null;
        }
        return (NamedNodeMapImpl) eldef.getAttributes();
    } 
    public final int getChildElementCount() {
        int count = 0;
        Element child = getFirstElementChild();
        while (child != null) {
            ++count;
            child = ((ElementImpl) child).getNextElementSibling();
        }
        return count;
    } 
    public final Element getFirstElementChild() {
        Node n = getFirstChild();
        while (n != null) {
            switch (n.getNodeType()) {
                case Node.ELEMENT_NODE:
                    return (Element) n;
                case Node.ENTITY_REFERENCE_NODE:
                    final Element e = getFirstElementChild(n);
                    if (e != null) {
                        return e;
                    }
                    break;
            }
            n = n.getNextSibling();
        }
        return null;
    } 
    public final Element getLastElementChild() {
        Node n = getLastChild();
        while (n != null) {
            switch (n.getNodeType()) {
                case Node.ELEMENT_NODE:
                    return (Element) n;
                case Node.ENTITY_REFERENCE_NODE:
                    final Element e = getLastElementChild(n);
                    if (e != null) {
                        return e;
                    }
                    break;
            }
            n = n.getPreviousSibling();
        }
        return null;
    } 
    public final Element getNextElementSibling() {
        Node n = getNextLogicalSibling(this);
        while (n != null) {
            switch (n.getNodeType()) {
                case Node.ELEMENT_NODE:
                    return (Element) n;
                case Node.ENTITY_REFERENCE_NODE:
                    final Element e = getFirstElementChild(n);
                    if (e != null) {
                        return e;
                    }
                    break;
            }
            n = getNextLogicalSibling(n);
        }
        return null;
    } 
    public final Element getPreviousElementSibling() {
        Node n = getPreviousLogicalSibling(this);
        while (n != null) {
            switch (n.getNodeType()) {
                case Node.ELEMENT_NODE:
                    return (Element) n;
                case Node.ENTITY_REFERENCE_NODE:
                    final Element e = getLastElementChild(n);
                    if (e != null) {
                        return e;
                    }
                    break;
            }
            n = getPreviousLogicalSibling(n);
        }
        return null;
    } 
    private Element getFirstElementChild(Node n) {
        final Node top = n;
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) n;
            }
            Node next = n.getFirstChild();
            while (next == null) {         
                if (top == n) {
                    break;
                }
                next = n.getNextSibling();
                if (next == null) {
                    n = n.getParentNode();
                    if (n == null || top == n) {
                        return null;
                    }
                }
            }
            n = next;
        }
        return null;
    } 
    private Element getLastElementChild(Node n) {
        final Node top = n;
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) n;
            }
            Node next = n.getLastChild();
            while (next == null) {         
                if (top == n) {
                    break;
                }
                next = n.getPreviousSibling();
                if (next == null) {
                    n = n.getParentNode();
                    if (n == null || top == n) {
                        return null;
                    }
                }
            }
            n = next;
        }
        return null;
    } 
    private Node getNextLogicalSibling(Node n) {
        Node next = n.getNextSibling();
        if (next == null) {
            Node parent = n.getParentNode();
            while (parent != null && parent.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
                next = parent.getNextSibling();
                if (next != null) {
                    break;
                }
                parent = parent.getParentNode();
            }
        }
        return next;
    } 
    private Node getPreviousLogicalSibling(Node n) {
        Node prev = n.getPreviousSibling();
        if (prev == null) {
            Node parent = n.getParentNode();
            while (parent != null && parent.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
                prev = parent.getPreviousSibling();
                if (prev != null) {
                    break;
                }
                parent = parent.getParentNode();
            }
        }
        return prev;
    } 
} 
