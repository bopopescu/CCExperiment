package org.apache.xerces.impl.dtd.models;
import org.apache.xerces.impl.dtd.XMLContentSpec;
import org.apache.xerces.xni.QName;
public class CMLeaf 
    extends CMNode {
    private final QName fElement = new QName();
    private int fPosition = -1;
    public CMLeaf(QName element, int position)  {
        super(XMLContentSpec.CONTENTSPECNODE_LEAF);
        fElement.setValues(element);
        fPosition = position;
    }
    public CMLeaf(QName element)  {
        super(XMLContentSpec.CONTENTSPECNODE_LEAF);
        fElement.setValues(element);
    }
    final QName getElement()
    {
        return fElement;
    }
    final int getPosition()
    {
        return fPosition;
    }
    final void setPosition(int newPosition)
    {
        fPosition = newPosition;
    }
    public boolean isNullable() 
    {
        return (fPosition == -1);
    }
    public String toString()
    {
        StringBuffer strRet = new StringBuffer(fElement.toString());
        strRet.append(" (");
        strRet.append(fElement.uri);
        strRet.append(',');
        strRet.append(fElement.localpart);
        strRet.append(')');
        if (fPosition >= 0) {
            strRet.append(" (Pos:")
                  .append(Integer.toString(fPosition))
                  .append(')');
        }
        return strRet.toString();
    }
    protected void calcFirstPos(CMStateSet toSet) 
    {
        if (fPosition == -1)
            toSet.zeroBits();
        else
            toSet.setBit(fPosition);
    }
    protected void calcLastPos(CMStateSet toSet) 
    {
        if (fPosition == -1)
            toSet.zeroBits();
        else
            toSet.setBit(fPosition);
    }
} 
