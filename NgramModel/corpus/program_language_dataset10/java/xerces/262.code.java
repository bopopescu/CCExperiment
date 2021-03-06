package org.apache.xerces.dom3.as;
public interface ASEntityDeclaration extends ASObject {
    public static final short INTERNAL_ENTITY           = 1;
    public static final short EXTERNAL_ENTITY           = 2;
    public short getEntityType();
    public void setEntityType(short entityType);
    public String getEntityValue();
    public void setEntityValue(String entityValue);
    public String getSystemId();
    public void setSystemId(String systemId);
    public String getPublicId();
    public void setPublicId(String publicId);
}
