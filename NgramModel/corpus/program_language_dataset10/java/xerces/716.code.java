package org.apache.xerces.xs;
import org.apache.xerces.xs.datatypes.ObjectList;
public interface XSMultiValueFacet extends XSObject {
    public short getFacetKind();
    public StringList getLexicalFacetValues();
    public ObjectList getEnumerationValues();
    public XSObjectList getAnnotations();
}
