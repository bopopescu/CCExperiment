package org.apache.batik.css.engine.value.svg;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.URIValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGTypes;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
public class FilterManager extends AbstractValueManager {
    public boolean isInheritedProperty() {
        return false;
    }
    public String getPropertyName() {
        return CSSConstants.CSS_FILTER_PROPERTY;
    }
    public boolean isAnimatableProperty() {
        return true;
    }
    public boolean isAdditiveProperty() {
        return false;
    }
    public int getPropertyType() {
        return SVGTypes.TYPE_URI_OR_IDENT;
    }
    public Value getDefaultValue() {
        return SVGValueConstants.NONE_VALUE;
    }
    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
        switch (lu.getLexicalUnitType()) {
        case LexicalUnit.SAC_INHERIT:
            return SVGValueConstants.INHERIT_VALUE;
        case LexicalUnit.SAC_URI:
            return new URIValue(lu.getStringValue(),
                                resolveURI(engine.getCSSBaseURI(),
                                           lu.getStringValue()));
        case LexicalUnit.SAC_IDENT:
            if (lu.getStringValue().equalsIgnoreCase
                (CSSConstants.CSS_NONE_VALUE)) {
                return SVGValueConstants.NONE_VALUE;
            }
            throw createInvalidIdentifierDOMException(lu.getStringValue());
        }
        throw createInvalidLexicalUnitDOMException(lu.getLexicalUnitType());
    }
    public Value createStringValue(short type, String value, CSSEngine engine)
        throws DOMException {
        if (type == CSSPrimitiveValue.CSS_IDENT) {
            if (value.equalsIgnoreCase(CSSConstants.CSS_NONE_VALUE)) {
                return SVGValueConstants.NONE_VALUE;
            }
            throw createInvalidIdentifierDOMException(value);
        }
        if (type == CSSPrimitiveValue.CSS_URI) {
            return new URIValue(value, 
                                resolveURI(engine.getCSSBaseURI(), value));
        }
        throw createInvalidStringTypeDOMException(type);
    }
}
