package org.apache.batik.apps.svgbrowser;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.HashTable;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.svg.SVGDocument;
public class XMLInputHandler implements SquiggleInputHandler {
    public static final String[] XVG_MIME_TYPES = 
    { "image/xml+xsl+svg" };
    public static final String[] XVG_FILE_EXTENSIONS =
    { ".xml", ".xsl" };
    public static final String ERROR_NO_XML_STYLESHEET_PROCESSING_INSTRUCTION
        = "XMLInputHandler.error.no.xml.stylesheet.processing.instruction";
    public static final String ERROR_TRANSFORM_OUTPUT_NOT_SVG
        = "XMLInputHandler.error.transform.output.not.svg";
    public static final String ERROR_TRANSFORM_PRODUCED_NO_CONTENT
        = "XMLInputHandler.error.transform.produced.no.content";
    public static final String ERROR_TRANSFORM_OUTPUT_WRONG_NS
        = "XMLInputHandler.error.transform.output.wrong.ns";
    public static final String ERROR_RESULT_GENERATED_EXCEPTION 
        = "XMLInputHandler.error.result.generated.exception";
    public static final String XSL_PROCESSING_INSTRUCTION_TYPE
        = "text/xsl";
    public static final String PSEUDO_ATTRIBUTE_TYPE
        = "type";
    public static final String PSEUDO_ATTRIBUTE_HREF
        = "href";
    public String[] getHandledMimeTypes() {
        return XVG_MIME_TYPES;
    }
    public String[] getHandledExtensions() {
        return XVG_FILE_EXTENSIONS;
    }
    public String getDescription() {
        return "";
    }
    public boolean accept(File f) {
        return f.isFile() && accept(f.getPath());
    }
    public boolean accept(ParsedURL purl) {
        if (purl == null) {
            return false;
        }
        String path = purl.getPath();        
        return accept(path);
    }
    public boolean accept(String path) {
        if (path == null) {
            return false;
        }
        for (int i=0; i<XVG_FILE_EXTENSIONS.length; i++) {
            if (path.endsWith(XVG_FILE_EXTENSIONS[i])) {
                return true;
            }
        }
        return false;
    }
    public void handle(ParsedURL purl, JSVGViewerFrame svgViewerFrame) throws Exception {
        String uri = purl.toString();
        TransformerFactory tFactory 
            = TransformerFactory.newInstance();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document inDoc = db.parse(uri);
        String xslStyleSheetURI 
            = extractXSLProcessingInstruction(inDoc);
        if (xslStyleSheetURI == null) {
            xslStyleSheetURI = uri;
        }
        ParsedURL parsedXSLStyleSheetURI 
            = new ParsedURL(uri, xslStyleSheetURI);
        Transformer transformer
            = tFactory.newTransformer
            (new StreamSource(parsedXSLStyleSheetURI.toString()));
        transformer.setURIResolver
            (new DocumentURIResolver(parsedXSLStyleSheetURI.toString()));
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        transformer.transform(new DOMSource(inDoc),
                              result);
        sw.flush();
        sw.close();
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        SVGDocument outDoc = null;
        try {
            outDoc = f.createSVGDocument
                (uri, new StringReader(sw.toString()));
        } catch (Exception e) {
            System.err.println("======================================");
            System.err.println(sw.toString());
            System.err.println("======================================");
            throw new IllegalArgumentException
                (Resources.getString(ERROR_RESULT_GENERATED_EXCEPTION));
        }
        svgViewerFrame.getJSVGCanvas().setSVGDocument(outDoc);
        svgViewerFrame.setSVGDocument(outDoc,
                                      uri,
                                      outDoc.getTitle());
    }
    protected void checkAndPatch(Document doc) {
        Element root = doc.getDocumentElement();
        Node realRoot = root.getFirstChild();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        if (realRoot == null) {
            throw new IllegalArgumentException
                (Resources.getString(ERROR_TRANSFORM_PRODUCED_NO_CONTENT));
        }
        if (realRoot.getNodeType() != Node.ELEMENT_NODE
            || 
            !SVGConstants.SVG_SVG_TAG.equals(realRoot.getLocalName())) {
            throw new IllegalArgumentException
                (Resources.getString(ERROR_TRANSFORM_OUTPUT_NOT_SVG));
        }
        if (!svgNS.equals(realRoot.getNamespaceURI())) {
            throw new IllegalArgumentException
                (Resources.getString(ERROR_TRANSFORM_OUTPUT_WRONG_NS));
        }
        Node child = realRoot.getFirstChild();
        while ( child != null ) {
            root.appendChild(child);
            child = realRoot.getFirstChild();
        }
        NamedNodeMap attrs = realRoot.getAttributes();
        int n = attrs.getLength();
        for (int i=0; i<n; i++) {
            root.setAttributeNode((Attr)attrs.item(i));
        }
        root.removeChild(realRoot);
    }
    protected String extractXSLProcessingInstruction(Document doc) {
        Node child = doc.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                ProcessingInstruction pi 
                    = (ProcessingInstruction)child;
                HashTable table = new HashTable();
                DOMUtilities.parseStyleSheetPIData(pi.getData(),
                                                   table);
                Object type = table.get(PSEUDO_ATTRIBUTE_TYPE);
                if (XSL_PROCESSING_INSTRUCTION_TYPE.equals(type)) {
                    Object href = table.get(PSEUDO_ATTRIBUTE_HREF);
                    if (href != null) {
                        return href.toString();
                    } else {
                        return null;
                    }
                }
            }
            child = child.getNextSibling();
        }
        return null;
    }
    public class DocumentURIResolver implements URIResolver {
        String documentURI;
        public DocumentURIResolver(String documentURI) {
            this.documentURI = documentURI;
        }
        public Source resolve(String href, String base) {
            if (base == null || "".equals(base)) {
                base = documentURI;
            }
            ParsedURL purl = new ParsedURL(base, href);
            return new StreamSource(purl.toString());
        }
    }
}
