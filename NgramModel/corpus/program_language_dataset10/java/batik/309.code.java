package org.apache.batik.css.engine.sac;
import org.apache.batik.css.engine.CSSStylableElement;
import org.w3c.dom.Element;
public class CSSClassCondition extends CSSAttributeCondition {
    public CSSClassCondition(String localName,
                             String namespaceURI,
                             String value) {
        super(localName, namespaceURI, true, value);
    }
    public short getConditionType() {
        return SAC_CLASS_CONDITION;
    }
    public boolean match(Element e, String pseudoE) {
        if (!(e instanceof CSSStylableElement))
            return false;  
        String attr = ((CSSStylableElement)e).getCSSClass();
        String val = getValue();
        int attrLen = attr.length();
        int valLen = val.length();
        int i = attr.indexOf(val);
        while (i != -1) {
            if (i == 0 || Character.isSpaceChar(attr.charAt(i - 1))) {
                if (i + valLen == attrLen ||
                        Character.isSpaceChar(attr.charAt(i + valLen))) {
                    return true;
                }
            }
            i = attr.indexOf(val, i + valLen);
        }
        return false;
    }
    public String toString() {
        return '.' + getValue();
    }
}
