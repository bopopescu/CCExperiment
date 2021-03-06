package org.apache.batik.bridge.svg12;
import java.util.ArrayList;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg12.XBLOMContentElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.xpath.XPathException;
public class XPathPatternContentSelector extends AbstractContentSelector {
    protected NSPrefixResolver prefixResolver = new NSPrefixResolver();
    protected XPath xpath;
    protected XPathContext context;
    protected SelectedNodes selectedContent;
    protected String expression;
    public XPathPatternContentSelector(ContentManager cm,
                                       XBLOMContentElement content,
                                       Element bound,
                                       String selector) {
        super(cm, content, bound);
        expression = selector;
        parse();
    }
    protected void parse() {
        context = new XPathContext();
        try {
            xpath = new XPath(expression, null, prefixResolver, XPath.MATCH);
        } catch (javax.xml.transform.TransformerException te) {
            AbstractDocument doc
                = (AbstractDocument) contentElement.getOwnerDocument();
            throw doc.createXPathException
                (XPathException.INVALID_EXPRESSION_ERR,
                 "xpath.invalid.expression",
                 new Object[] { expression, te.getMessage() });
        }
    }
    public NodeList getSelectedContent() {
        if (selectedContent == null) {
            selectedContent = new SelectedNodes();
        }
        return selectedContent;
    }
    boolean update() {
        if (selectedContent == null) {
            selectedContent = new SelectedNodes();
            return true;
        }
        parse();
        return selectedContent.update();
    }
    protected class SelectedNodes implements NodeList {
        protected ArrayList nodes = new ArrayList(10);
        public SelectedNodes() {
            update();
        }
        protected boolean update() {
            ArrayList oldNodes = (ArrayList) nodes.clone();
            nodes.clear();
            for (Node n = boundElement.getFirstChild();
                    n != null;
                    n = n.getNextSibling()) {
                update(n);
            }
            int nodesSize = nodes.size();
            if (oldNodes.size() != nodesSize) {
                return true;
            }
            for (int i = 0; i < nodesSize; i++) {
                if (oldNodes.get(i) != nodes.get(i)) {
                    return true;
                }
            }
            return false;
        }
        protected boolean descendantSelected(Node n) {
            n = n.getFirstChild();
            while (n != null) {
                if (isSelected(n) || descendantSelected(n)) {
                    return true;
                }
                n = n.getNextSibling();
            }
            return false;
        }
        protected void update(Node n) {
            if (!isSelected(n)) {
                try {
                    double matchScore
                        = xpath.execute(context, n, prefixResolver).num();
                    if (matchScore != XPath.MATCH_SCORE_NONE) {
                        if (!descendantSelected(n)) {
                            nodes.add(n);
                        }
                    } else {
                        n = n.getFirstChild();
                        while (n != null) {
                            update(n);
                            n = n.getNextSibling();
                        }
                    }
                } catch (javax.xml.transform.TransformerException te) {
                    AbstractDocument doc
                        = (AbstractDocument) contentElement.getOwnerDocument();
                    throw doc.createXPathException
                        (XPathException.INVALID_EXPRESSION_ERR,
                         "xpath.error",
                         new Object[] { expression, te.getMessage() });
                }
            }
        }
        public Node item(int index) {
            if (index < 0 || index >= nodes.size()) {
                return null;
            }
            return (Node) nodes.get(index);
        }
        public int getLength() {
            return nodes.size();
        }
    }
    protected class NSPrefixResolver implements PrefixResolver {
        public String getBaseIdentifier() {
            return null;
        }
        public String getNamespaceForPrefix(String prefix) {
            return contentElement.lookupNamespaceURI(prefix);
        }
        public String getNamespaceForPrefix(String prefix, Node context) {
            return contentElement.lookupNamespaceURI(prefix);
        }
        public boolean handlesNullPrefixes() {
            return false;
        }
    }
}
