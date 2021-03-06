package org.apache.xerces.stax.events;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.events.Namespace;
public final class NamespaceImpl extends AttributeImpl implements Namespace {
    private final String fPrefix;
    private final String fNamespaceURI;
    public NamespaceImpl(final String prefix, final String namespaceURI, final Location location) {
        super(NAMESPACE, makeAttributeQName(prefix), namespaceURI, null, true, location);
        fPrefix = (prefix == null) ? XMLConstants.DEFAULT_NS_PREFIX : prefix;
        fNamespaceURI = namespaceURI;
    }
    private static QName makeAttributeQName(String prefix) {
        if (prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            return new QName(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.DEFAULT_NS_PREFIX);
        }
        return new QName(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, prefix, XMLConstants.XMLNS_ATTRIBUTE);
    }
    public String getPrefix() {
        return fPrefix;
    }
    public String getNamespaceURI() {
        return fNamespaceURI;
    }
    public boolean isDefaultNamespaceDeclaration() {
        return fPrefix.length() == 0;
    }
}
