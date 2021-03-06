package org.apache.xerces.impl.xs;
import org.apache.xerces.impl.dv.ValidatedInfo;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xs.ShortList;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSValue;
public class XSAttributeDecl implements XSAttributeDeclaration {
    public final static short     SCOPE_ABSENT        = 0;
    public final static short     SCOPE_GLOBAL        = 1;
    public final static short     SCOPE_LOCAL         = 2;
    String fName = null;
    String fTargetNamespace = null;
    XSSimpleType fType = null;
    public QName fUnresolvedTypeName = null;
    short fConstraintType = XSConstants.VC_NONE;
    short fScope = XSConstants.SCOPE_ABSENT;
    XSComplexTypeDecl fEnclosingCT = null;
    XSObjectList fAnnotations = null;    
    ValidatedInfo fDefault = null;
    private XSNamespaceItem fNamespaceItem = null;
    public void setValues(String name, String targetNamespace,
            XSSimpleType simpleType, short constraintType, short scope,
            ValidatedInfo valInfo, XSComplexTypeDecl enclosingCT,
            XSObjectList annotations) {
        fName = name;
        fTargetNamespace = targetNamespace;
        fType = simpleType;
        fConstraintType = constraintType;
        fScope = scope;
        fDefault = valInfo;
        fEnclosingCT = enclosingCT;
        fAnnotations = annotations;
    }
    public void reset(){
        fName = null;
        fTargetNamespace = null;
        fType = null;
        fUnresolvedTypeName = null;
        fConstraintType = XSConstants.VC_NONE;
        fScope = XSConstants.SCOPE_ABSENT;
        fDefault = null;
        fAnnotations = null;
    }
    public short getType() {
        return XSConstants.ATTRIBUTE_DECLARATION;
    }
    public String getName() {
        return fName;
    }
    public String getNamespace() {
        return fTargetNamespace;
    }
    public XSSimpleTypeDefinition getTypeDefinition() {
        return fType;
    }
    public short getScope() {
        return fScope;
    }
    public XSComplexTypeDefinition getEnclosingCTDefinition() {
        return fEnclosingCT;
    }
    public short getConstraintType() {
        return fConstraintType;
    }
    public String getConstraintValue() {
        return getConstraintType() == XSConstants.VC_NONE ?
               null :
               fDefault.stringValue();
    }
    public XSAnnotation getAnnotation() {
        return (fAnnotations != null) ? (XSAnnotation) fAnnotations.item(0) : null;
    }
    public XSObjectList getAnnotations() {
        return (fAnnotations != null) ? fAnnotations : XSObjectListImpl.EMPTY_LIST;
    }
    public ValidatedInfo getValInfo() {
        return fDefault;
    }
    public XSNamespaceItem getNamespaceItem() {
        return fNamespaceItem;
    }
    void setNamespaceItem(XSNamespaceItem namespaceItem) {
        fNamespaceItem = namespaceItem;
    }
    public Object getActualVC() {
        return getConstraintType() == XSConstants.VC_NONE ?
               null :
               fDefault.actualValue;
    }
    public short getActualVCType() {
        return getConstraintType() == XSConstants.VC_NONE ?
               XSConstants.UNAVAILABLE_DT :
               fDefault.actualValueType;
    }
    public ShortList getItemValueTypes() {
        return getConstraintType() == XSConstants.VC_NONE ?
               null :
               fDefault.itemValueTypes;
    }
    public XSValue getValueConstraintValue() {
        return fDefault;
    }
} 
