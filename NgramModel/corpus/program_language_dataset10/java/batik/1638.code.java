package org.apache.batik.transcoder.image;
import java.io.IOException;
import java.net.URL;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.util.SAXDocumentFactory;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.util.XMLResourceDescriptor;
public class GenericDocumentTest extends AbstractImageTranscoderTest {
    protected String inputURI;
    protected String refImageURI;
    public GenericDocumentTest(String inputURI, String refImageURI) {
        this.inputURI    = inputURI;
        this.refImageURI = refImageURI;
    }
    protected TranscoderInput createTranscoderInput() {
        try {
            URL url = resolveURL(inputURI);
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            DOMImplementation impl = 
                GenericDOMImplementation.getDOMImplementation();
            SAXDocumentFactory f = new SAXDocumentFactory(impl, parser);
            Document doc = f.createDocument(url.toString());
            TranscoderInput input = new TranscoderInput(doc);
            input.setURI(url.toString()); 
            return input;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(inputURI);
        }
    }
    protected byte [] getReferenceImageData() {
        return createBufferedImageData(resolveURL(refImageURI));
    }
}
