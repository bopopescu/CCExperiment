package org.apache.xerces.xs;
public interface XSValue {
    public String getNormalizedValue();
    public Object getActualValue();
    public XSSimpleTypeDefinition getTypeDefinition();
    public XSSimpleTypeDefinition getMemberTypeDefinition();
    public XSObjectList getMemberTypeDefinitions();
    public short getActualValueType();
    public ShortList getListValueTypes();
}
