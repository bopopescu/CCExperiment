package dom;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ElementTraversal;
public class ElementPrinter {
    public static void main(String[] argv) {
        if (argv.length == 0) {
            printUsage();
            System.exit(1);
        }
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setExpandEntityReferences(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(argv[0]);
            DOMImplementation domImpl = doc.getImplementation();
            if (domImpl.hasFeature("ElementTraversal", "1.0")) {
                print(doc.getDocumentElement(), 0);
            }
            else {
                System.err.println("The DOM implementation does not claim support for ElementTraversal.");
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private static void print(Element e, int depth) {
        do {
            ElementTraversal et = (ElementTraversal) e;
            for (int i = 0; i < depth; ++i) {
                System.out.print("--");
            }
            System.out.print("--> [");
            System.out.print(new QName(e.getNamespaceURI(), e.getLocalName()));
            System.out.println("], Child Element Count = " + et.getChildElementCount());
            Element firstElementChild = et.getFirstElementChild();
            if (firstElementChild != null) {
                print(firstElementChild, depth + 1);
            }
            e = et.getNextElementSibling();
        }
        while (e != null);
    }
    private static void printUsage() {
        System.err.println("usage: java dom.ElementPrinter uri");
    }
}
