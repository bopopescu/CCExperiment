package org.apache.batik.css.engine.value;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
public class RectValue extends AbstractValue {
    protected Value top;
    protected Value right;
    protected Value bottom;
    protected Value left;
    public RectValue(Value t, Value r, Value b, Value l) {
        top = t;
        right = r;
        bottom = b;
        left = l;
    }
    public short getPrimitiveType() {
        return CSSPrimitiveValue.CSS_RECT;
    }
    public String getCssText() {
        return "rect(" + top.getCssText() + ", "
            +  right.getCssText() + ", "
            +  bottom.getCssText() + ", "
            +  left.getCssText() + ')';
    }
    public Value getTop() throws DOMException {
        return top;
    }
    public Value getRight() throws DOMException {
        return right;
    }
    public Value getBottom() throws DOMException {
        return bottom;
    }
    public Value getLeft() throws DOMException {
        return left;
    }
    public String toString() {
        return getCssText();
    }
}
