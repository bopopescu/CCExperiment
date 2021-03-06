package org.apache.batik.dom;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
public class NodeTextContentTest extends DOM3Test {
    public boolean runImplBasic() throws Exception {
        Document doc = newSVGDoc();
        AbstractElement e = (AbstractElement) doc.getDocumentElement();
        e.appendChild(doc.createTextNode("abc"));
        Element e2 = doc.createElementNS(SVG_NAMESPACE_URI, "text");
        e2.appendChild(doc.createTextNode("def"));
        e.appendChild(e2);
        e.appendChild(doc.createCDATASection("ghi"));
        String s = e.getTextContent();
        e.setTextContent("blah");
        return s.equals("abcdefghi")
                && e.getFirstChild().getNodeType() == Node.TEXT_NODE
                && e.getFirstChild().getNodeValue().equals("blah")
                && e.getLastChild() == e.getFirstChild();
    }
}
