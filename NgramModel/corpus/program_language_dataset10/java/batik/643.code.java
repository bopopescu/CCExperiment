package org.apache.batik.dom.svg;
import org.apache.batik.css.engine.CSSNavigableNode;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.AbstractDocumentFragment;
import org.apache.batik.dom.events.NodeEventTarget;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
public class SVGOMUseShadowRoot
        extends AbstractDocumentFragment
        implements CSSNavigableNode, IdContainer {
    protected Element cssParentElement;
    protected boolean isLocal;
    protected SVGOMUseShadowRoot() {
    }
    public SVGOMUseShadowRoot(AbstractDocument owner,
                                       Element parent,
                                       boolean isLocal) {
        ownerDocument = owner;
        cssParentElement = parent;
        this.isLocal = isLocal;
    }
    public boolean isReadonly() {
        return false;
    }
    public void setReadonly(boolean v) {
    }
    public Element getElementById(String id) {
        return ownerDocument.getChildElementById(this, id);
    }
    public Node getCSSParentNode() {
        return cssParentElement;
    }
    public Node getCSSPreviousSibling() {
        return null;
    }
    public Node getCSSNextSibling() {
        return null;
    }
    public Node getCSSFirstChild() {
        return getFirstChild();
    }
    public Node getCSSLastChild() {
        return getLastChild();
    }
    public boolean isHiddenFromSelectors() {
        return false;
    }
    public NodeEventTarget getParentNodeEventTarget() {
        return (NodeEventTarget) getCSSParentNode();
    }
    protected Node newNode() {
        return new SVGOMUseShadowRoot();
    }
}
