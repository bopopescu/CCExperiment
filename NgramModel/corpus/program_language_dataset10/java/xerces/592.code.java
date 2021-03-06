package org.apache.xerces.stax.events;
import java.io.IOException;
import java.io.Writer;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EntityDeclaration;
public final class EntityDeclarationImpl extends XMLEventImpl implements
        EntityDeclaration {
    private final String fPublicId;
    private final String fSystemId;
    private final String fName;
    private final String fNotationName;
    public EntityDeclarationImpl(final String publicId, final String systemId, final String name, final String notationName, final Location location) {
        super(ENTITY_DECLARATION, location);
        fPublicId = publicId;
        fSystemId = systemId;
        fName = name;
        fNotationName = notationName;
    }
    public String getPublicId() {
        return fPublicId;
    }
    public String getSystemId() {
        return fSystemId;
    }
    public String getName() {
        return fName;
    }
    public String getNotationName() {
        return fNotationName;
    }
    public String getReplacementText() {
        return null;
    }
    public String getBaseURI() {
        return null;
    }
    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
        try {
            writer.write("<!ENTITY ");
            writer.write(fName);
            if (fPublicId != null) {
                writer.write(" PUBLIC \"");
                writer.write(fPublicId);
                writer.write("\" \"");
                writer.write(fSystemId);
                writer.write('"');
            }
            else {
                writer.write(" SYSTEM \"");
                writer.write(fSystemId);
                writer.write('"');
            }
            if (fNotationName != null) {
                writer.write(" NDATA ");
                writer.write(fNotationName);
            }
            writer.write('>');
        }
        catch (IOException ioe) {
            throw new XMLStreamException(ioe);
        }
    }
}
