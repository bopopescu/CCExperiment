package org.apache.xerces.stax;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
public final class DefaultNamespaceContext implements NamespaceContext {
    private static final DefaultNamespaceContext DEFAULT_NAMESPACE_CONTEXT_INSTANCE 
        = new DefaultNamespaceContext();
    private DefaultNamespaceContext() {}
    public static DefaultNamespaceContext getInstance() {
        return DEFAULT_NAMESPACE_CONTEXT_INSTANCE;
    }
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Prefix cannot be null.");
        }
        else if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
            return XMLConstants.XML_NS_URI;
        }
        else if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        }
        return XMLConstants.NULL_NS_URI;
    } 
    public String getPrefix(String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("Namespace URI cannot be null.");
        }
        else if (XMLConstants.XML_NS_URI.equals(namespaceURI)) {
            return XMLConstants.XML_NS_PREFIX;
        }
        else if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI)) {
            return XMLConstants.XMLNS_ATTRIBUTE;
        }
        return null;
    } 
    public Iterator getPrefixes(String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("Namespace URI cannot be null.");
        }
        else if (XMLConstants.XML_NS_URI.equals(namespaceURI)) {
            return new Iterator() {
                boolean more = true;
                public boolean hasNext() {
                    return more;
                }
                public Object next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    more = false;
                    return XMLConstants.XML_NS_PREFIX;
                }
                public void remove() {
                    throw new UnsupportedOperationException();                   
                }  
            };
        }
        else if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI)) {
            return new Iterator() {
                boolean more = true;
                public boolean hasNext() {
                    return more;
                }
                public Object next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    more = false;
                    return XMLConstants.XMLNS_ATTRIBUTE;
                }
                public void remove() {
                    throw new UnsupportedOperationException();                   
                }  
            };
        }
        return Collections.EMPTY_LIST.iterator();
    } 
}
