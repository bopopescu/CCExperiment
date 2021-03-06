package org.apache.xerces.stax.events;
import java.io.IOException;
import java.io.Writer;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Comment;
public final class CommentImpl extends XMLEventImpl implements Comment {
    private final String fText;
    public CommentImpl(final String text, final Location location) {
        super(COMMENT, location);
        fText = (text != null) ? text : "";
    }
    public String getText() {
        return fText; 
    }
    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
        try {
            writer.write("<!--");
            writer.write(fText);
            writer.write("-->");
        }
        catch (IOException ioe) {
            throw new XMLStreamException(ioe);
        }
    }
}
