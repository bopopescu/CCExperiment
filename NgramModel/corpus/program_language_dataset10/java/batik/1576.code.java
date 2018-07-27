package org.apache.batik.test;
public class AssertNullException extends AssertException {
    public static final String ASSERTION_TYPE = "assertNull";
    protected Object ref, cmp;
    public AssertNullException(){
    }
    public void addDescription(TestReport report){
    }
    public String getAssertionType(){
        return ASSERTION_TYPE;
    }
}
