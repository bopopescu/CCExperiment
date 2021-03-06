package org.apache.xerces.impl.dtd.models;
public abstract class CMNode
{
    public CMNode(int type) 
    {
        fType = type;
    }
    public abstract boolean isNullable() ;
    public final int type()
    {
        return fType;
    }
    public final CMStateSet firstPos() 
    {
        if (fFirstPos == null)
        {
            fFirstPos = new CMStateSet(fMaxStates);
            calcFirstPos(fFirstPos);
        }
        return fFirstPos;
    }
    public final CMStateSet lastPos() 
    {
        if (fLastPos == null)
        {
            fLastPos = new CMStateSet(fMaxStates);
            calcLastPos(fLastPos);
        }
        return fLastPos;
    }
    final void setFollowPos(CMStateSet setToAdopt)
    {
        fFollowPos = setToAdopt;
    }
    public final void setMaxStates(int maxStates)
    {
        fMaxStates = maxStates;
    }
    public boolean isCompactedForUPA() {
        return fCompactedForUPA;
    }
    public void setIsCompactUPAModel(boolean value) {
        fCompactedForUPA = value;
    }
    protected abstract void calcFirstPos(CMStateSet toSet) ;
    protected abstract void calcLastPos(CMStateSet toSet) ;
    private final int  fType;
    private CMStateSet fFirstPos   = null;
    private CMStateSet fFollowPos  = null;
    private CMStateSet fLastPos    = null;
    private int        fMaxStates  = -1;
    private boolean fCompactedForUPA = false;    
};