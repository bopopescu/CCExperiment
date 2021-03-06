package org.apache.batik.transcoder.image;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage; 
import java.io.ByteArrayOutputStream;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.DOMImplementation;
public class DOMTest extends AbstractImageTranscoderTest {
    public DOMTest() {
    }
    protected TranscoderInput createTranscoderInput() {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        Document doc = impl.createDocument(svgNS, "svg", null);
        Element root = doc.getDocumentElement();
        root.setAttributeNS(null, "width", "400");
        root.setAttributeNS(null, "height", "400");
        Element r = doc.createElementNS(svgNS, "rect");
        r.setAttributeNS(null, "x", "0");
        r.setAttributeNS(null, "y", "0");
        r.setAttributeNS(null, "width", "400");
        r.setAttributeNS(null, "height", "400");
        r.setAttributeNS(null, "style", "fill:black");
        root.appendChild(r);
        r = doc.createElementNS(svgNS, "rect");
        r.setAttributeNS(null, "x", "100");
        r.setAttributeNS(null, "y", "50");
        r.setAttributeNS(null, "width", "100");
        r.setAttributeNS(null, "height", "50");
        r.setAttributeNS(null, "style", "stroke:red; fill:none");
        root.appendChild(r);
        return new TranscoderInput(doc);
    }
    protected byte [] getReferenceImageData() {
        try {
            BufferedImage img = new BufferedImage
                (400, 400, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setColor(Color.black);
            g2d.fillRect(0, 0, 400, 400);
            g2d.setColor(Color.red);
            g2d.drawRect(100, 50, 100, 50);
            ByteArrayOutputStream ostream = new ByteArrayOutputStream();
            PNGTranscoder t = new PNGTranscoder();
            TranscoderOutput output = new TranscoderOutput(ostream);
            t.writeImage(img, output);
            return ostream.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("DOMTest error");
        }
    }
}
