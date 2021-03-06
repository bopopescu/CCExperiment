package org.apache.xerces.stax.events;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
public final class EndElementImpl extends ElementImpl implements EndElement {
    public EndElementImpl(final QName name, final Iterator namespaces, final Location location) {
        super(name, false, namespaces, location);
    }
    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
        try {
            writer.write("</");
            QName name = getName();
            String prefix = name.getPrefix();
            if (prefix != null && prefix.length() > 0) {
                writer.write(prefix);
                writer.write(':');
            }
            writer.write(name.getLocalPart());
            writer.write('>');
        }
        catch (IOException ioe) {
            throw new XMLStreamException(ioe);
        }
    }
}
