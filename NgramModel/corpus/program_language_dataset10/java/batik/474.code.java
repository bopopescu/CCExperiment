package org.apache.batik.dom;
import org.w3c.dom.Node;
public class GenericProcessingInstruction
    extends AbstractProcessingInstruction {
    protected String target;
    protected boolean readonly;
    protected GenericProcessingInstruction() {
    }
    public GenericProcessingInstruction(String           target,
                                        String           data,
                                        AbstractDocument owner) {
        ownerDocument = owner;
        setTarget(target);
        setData(data);
    }
    public void setNodeName(String v) {
        setTarget(v);
    }
    public boolean isReadonly() {
        return readonly;
    }
    public void setReadonly(boolean v) {
        readonly = v;
    }
    public String getTarget() {
        return target;
    }
    public void setTarget(String v) {
        target = v;
    }
    protected Node export(Node n, AbstractDocument d) {
        GenericProcessingInstruction p;
        p = (GenericProcessingInstruction)super.export(n, d);
        p.setTarget(getTarget());
        return p;
    }
    protected Node deepExport(Node n, AbstractDocument d) {
        GenericProcessingInstruction p;
        p = (GenericProcessingInstruction)super.deepExport(n, d);
        p.setTarget(getTarget());
        return p;
    }
    protected Node copyInto(Node n) {
        GenericProcessingInstruction p;
        p = (GenericProcessingInstruction)super.copyInto(n);
        p.setTarget(getTarget());
        return p;
    }
    protected Node deepCopyInto(Node n) {
        GenericProcessingInstruction p;
        p = (GenericProcessingInstruction)super.deepCopyInto(n);
        p.setTarget(getTarget());
        return p;
    }
    protected Node newNode() {
        return new GenericProcessingInstruction();
    }
}
