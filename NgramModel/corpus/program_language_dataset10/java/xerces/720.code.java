package org.apache.xerces.xs;
public interface XSNotationDeclaration extends XSObject {
    public String getSystemId();
    public String getPublicId();
    public XSAnnotation getAnnotation();
    public XSObjectList getAnnotations();    
}
