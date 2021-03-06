package org.apache.xerces.dom3.as;
public interface ASElementDeclaration extends ASObject {
    public static final short EMPTY_CONTENTTYPE         = 1;
    public static final short ANY_CONTENTTYPE           = 2;
    public static final short MIXED_CONTENTTYPE         = 3;
    public static final short ELEMENTS_CONTENTTYPE      = 4;
    public boolean getStrictMixedContent();
    public void setStrictMixedContent(boolean strictMixedContent);
    public ASDataType getElementType();
    public void setElementType(ASDataType elementType);
    public boolean getIsPCDataOnly();
    public void setIsPCDataOnly(boolean isPCDataOnly);
    public short getContentType();
    public void setContentType(short contentType);
    public String getSystemId();
    public void setSystemId(String systemId);
    public ASContentModel getAsCM();
    public void setAsCM(ASContentModel asCM);
    public ASNamedObjectMap getASAttributeDecls();
    public void setASAttributeDecls(ASNamedObjectMap ASAttributeDecls);
    public void addASAttributeDecl(ASAttributeDeclaration attributeDecl);
    public ASAttributeDeclaration removeASAttributeDecl(ASAttributeDeclaration attributeDecl);
}
