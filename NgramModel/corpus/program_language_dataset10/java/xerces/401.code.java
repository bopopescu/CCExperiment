package org.apache.xerces.impl.validation;
public interface EntityState {
    public boolean isEntityDeclared (String name);
    public boolean isEntityUnparsed (String name);
}
