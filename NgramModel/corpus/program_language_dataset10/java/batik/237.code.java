package org.apache.batik.bridge;
import java.io.IOException;
import java.net.MalformedURLException;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;
public class URIResolver {
    protected SVGOMDocument document;
    protected String documentURI;
    protected DocumentLoader documentLoader;
    public URIResolver(SVGDocument doc, DocumentLoader dl) {
        document = (SVGOMDocument)doc;
        documentLoader = dl;
    }
    public Element getElement(String uri, Element ref)
        throws MalformedURLException, IOException {
        Node n = getNode(uri, ref);
        if (n == null) {
            return null;
        } else if (n.getNodeType() == Node.DOCUMENT_NODE) {
            throw new IllegalArgumentException();
        } else {
            return (Element)n;
        }
    }
    public Node getNode(String uri, Element ref)
        throws MalformedURLException, IOException, SecurityException {
        String baseURI = getRefererBaseURI(ref);
        if (baseURI == null && uri.charAt(0) == '#') {
            return getNodeByFragment(uri.substring(1), ref);
        }
        ParsedURL purl = new ParsedURL(baseURI, uri);
        if (documentURI == null)
            documentURI = document.getURL();
        String    frag  = purl.getRef();
        if ((frag != null) && (documentURI != null)) {
            ParsedURL pDocURL = new ParsedURL(documentURI);
            if (pDocURL.sameFile(purl)) {
                return document.getElementById(frag);
            }
        }
        ParsedURL pDocURL = null;
        if (documentURI != null) {
            pDocURL = new ParsedURL(documentURI);
        }
        UserAgent userAgent = documentLoader.getUserAgent();
        userAgent.checkLoadExternalResource(purl, pDocURL);
        String purlStr = purl.toString();
        if (frag != null) {
            purlStr = purlStr.substring(0, purlStr.length()-(frag.length()+1));
        }
        Document doc = documentLoader.loadDocument(purlStr);
        if (frag != null)
            return doc.getElementById(frag);
        return doc;
    }
    protected String getRefererBaseURI(Element ref) {
        return ((AbstractNode) ref).getBaseURI();
    }
    protected Node getNodeByFragment(String frag, Element ref) {
        return ref.getOwnerDocument().getElementById(frag);
    }
}
