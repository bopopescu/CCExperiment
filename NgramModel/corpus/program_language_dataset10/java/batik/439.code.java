package org.apache.batik.dom;
import org.apache.batik.dom.util.DOMUtilities;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.events.MutationEvent;
public abstract class AbstractAttr extends AbstractParentNode implements Attr {
    protected String nodeName;
    protected boolean unspecified;
    protected boolean isIdAttr;
    protected AbstractElement ownerElement;
    protected TypeInfo typeInfo;
    protected AbstractAttr() {
    }
    protected AbstractAttr(String name, AbstractDocument owner)
        throws DOMException {
        ownerDocument = owner;
        if (owner.getStrictErrorChecking() && !DOMUtilities.isValidName(name)) {
            throw createDOMException(DOMException.INVALID_CHARACTER_ERR,
                                     "xml.name",
                                     new Object[] { name });
        }
    }
    public void setNodeName(String v) {
        nodeName = v;
        isIdAttr = ownerDocument.isId(this);
    }
    public String getNodeName() {
        return nodeName;
    }
    public short getNodeType() {
        return ATTRIBUTE_NODE;
    }
    public String getNodeValue() throws DOMException {
        Node first = getFirstChild();
        if (first == null) {
            return "";
        }
        Node n = first.getNextSibling();
        if (n == null) {
            return first.getNodeValue();
        }
        StringBuffer result = new StringBuffer(first.getNodeValue());
        do {
            result.append(n.getNodeValue());
            n = n.getNextSibling();
        } while (n != null);
        return result.toString();
    }
    public void setNodeValue(String nodeValue) throws DOMException {
        if (isReadonly()) {
            throw createDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     "readonly.node",
                                     new Object[] { new Integer(getNodeType()),
                                                    getNodeName() });
        }
        String s = getNodeValue();
        Node n;
        while ((n = getFirstChild()) != null) {
            removeChild(n);
        }
        String val = (nodeValue == null) ? "" : nodeValue;
        n = getOwnerDocument().createTextNode(val);
        appendChild(n);
        if (ownerElement != null) {
            ownerElement.fireDOMAttrModifiedEvent(nodeName,
                                                  this,
                                                  s,
                                                  val,
                                                  MutationEvent.MODIFICATION);
        }
    }
    public String getName() {
        return getNodeName();
    }
    public boolean getSpecified() {
        return !unspecified;
    }
    public void setSpecified(boolean v) {
        unspecified = !v;
    }
    public String getValue() {
        return getNodeValue();
    }
    public void setValue(String value) throws DOMException {
        setNodeValue(value);
    }
    public void setOwnerElement(AbstractElement v) {
        ownerElement = v;
    }
    public Element getOwnerElement() {
        return ownerElement;
    }
    public TypeInfo getSchemaTypeInfo() {
        if (typeInfo == null) {
            typeInfo = new AttrTypeInfo();
        }
        return typeInfo;
    }
    public boolean isId() {
        return isIdAttr;
    }
    public void setIsId(boolean isId) {
        isIdAttr = isId;
    }
    protected void nodeAdded(Node n) {
        setSpecified(true);
    }
    protected void nodeToBeRemoved(Node n) {
        setSpecified(true);
    }
    protected Node export(Node n, AbstractDocument d) {
        super.export(n, d);
        AbstractAttr aa = (AbstractAttr)n;
        aa.nodeName     = nodeName;
        aa.unspecified  = false;
        aa.isIdAttr     = d.isId(aa);
        return n;
    }
    protected Node deepExport(Node n, AbstractDocument d) {
        super.deepExport(n, d);
        AbstractAttr aa = (AbstractAttr)n;
        aa.nodeName     = nodeName;
        aa.unspecified  = false;
        aa.isIdAttr     = d.isId(aa);
        return n;
    }
    protected Node copyInto(Node n) {
        super.copyInto(n);
        AbstractAttr aa = (AbstractAttr)n;
        aa.nodeName     = nodeName;
        aa.unspecified  = unspecified;
        aa.isIdAttr     = isIdAttr;
        return n;
    }
    protected Node deepCopyInto(Node n) {
        super.deepCopyInto(n);
        AbstractAttr aa = (AbstractAttr)n;
        aa.nodeName     = nodeName;
        aa.unspecified  = unspecified;
        aa.isIdAttr     = isIdAttr;
        return n;
    }
    protected void checkChildType(Node n, boolean replace) {
        switch (n.getNodeType()) {
        case TEXT_NODE:
        case ENTITY_REFERENCE_NODE:
        case DOCUMENT_FRAGMENT_NODE:
            break;
        default:
            throw createDOMException
                (DOMException.HIERARCHY_REQUEST_ERR,
                 "child.type",
                 new Object[] { new Integer(getNodeType()),
                                            getNodeName(),
                                new Integer(n.getNodeType()),
                                            n.getNodeName() });
        }
    }
    protected void fireDOMSubtreeModifiedEvent() {
        AbstractDocument doc = getCurrentDocument();
        if (doc.getEventsEnabled()) {
            super.fireDOMSubtreeModifiedEvent();
            if (getOwnerElement() != null) {
                ((AbstractElement)getOwnerElement()).
                    fireDOMSubtreeModifiedEvent();
            }
        }
    }
    public class AttrTypeInfo implements TypeInfo {
        public String getTypeNamespace() {
            return null;
        }
        public String getTypeName() {
            return null;
        }
        public boolean isDerivedFrom(String ns, String name, int method) {
            return false;
        }
    }
}
