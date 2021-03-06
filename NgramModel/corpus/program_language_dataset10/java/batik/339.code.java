package org.apache.batik.css.engine.value;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
public class StringValue extends AbstractValue {
    public static String getCssText(short type, String value) {
        switch (type) {
        case CSSPrimitiveValue.CSS_URI:
            return "url(" + value + ')';
        case CSSPrimitiveValue.CSS_STRING:
            char q = (value.indexOf('"') != -1) ? '\'' : '"';
            return q + value + q;
        }
        return value;
    }
    protected String value;
    protected short unitType;
    public StringValue(short type, String s) {
        unitType = type;
        value = s;
    }
    public short getPrimitiveType() {
        return unitType;
    }
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof StringValue)) {
            return false;
        }
        StringValue v = (StringValue)obj;
        if (unitType != v.unitType) {
            return false;
        }
        return value.equals(v.value);
    }
    public String getCssText() {
        return getCssText(unitType, value);
    }
    public String getStringValue() throws DOMException {
        return value;
    }
    public String toString() {
        return getCssText();
    }
}
