package org.apache.batik.dom;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
public class GenericElementNS extends AbstractElementNS {
    protected String nodeName;
    protected boolean readonly;
    protected GenericElementNS() {
    }
    public GenericElementNS(String nsURI, String name,
                            AbstractDocument owner) {
        super(nsURI, name, owner);
        nodeName = name;
    }
    public void setNodeName(String v) {
        nodeName = v;
    }
    public String getNodeName() {
        return nodeName;
    }
    public boolean isReadonly() {
        return readonly;
    }
    public void setReadonly(boolean v) {
        readonly = v;
    }
    protected Node export(Node n, AbstractDocument d) {
        GenericElementNS ge = (GenericElementNS)super.export(n, d);
        ge.nodeName = nodeName;
        return n;
    }
    protected Node deepExport(Node n, AbstractDocument d) {
        GenericElementNS ge = (GenericElementNS)super.deepExport(n, d);
        ge.nodeName = nodeName;
        return n;
    }
    protected Node copyInto(Node n) {
        GenericElementNS ge = (GenericElementNS)super.copyInto(n);
        ge.nodeName = nodeName;
        return n;
    }
    protected Node deepCopyInto(Node n) {
        GenericElementNS ge = (GenericElementNS)super.deepCopyInto(n);
        ge.nodeName = nodeName;
        return n;
    }
    protected Node newNode() {
        return new GenericElementNS();
    }
}
