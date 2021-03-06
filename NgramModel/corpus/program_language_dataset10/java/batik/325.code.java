package org.apache.batik.css.engine.value;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.DOMException;
public abstract class AbstractValueFactory {
    public abstract String getPropertyName();
    protected static String resolveURI(ParsedURL base, String value) {
        return new ParsedURL(base, value).toString();
    }
    protected DOMException createInvalidIdentifierDOMException(String ident) {
        Object[] p = new Object[] { getPropertyName(), ident };
        String s = Messages.formatMessage("invalid.identifier", p);
        return new DOMException(DOMException.SYNTAX_ERR, s);
    }
    protected DOMException createInvalidLexicalUnitDOMException(short type) {
        Object[] p = new Object[] { getPropertyName(),
                                    new Integer(type) };
        String s = Messages.formatMessage("invalid.lexical.unit", p);
        return new DOMException(DOMException.NOT_SUPPORTED_ERR, s);
    }
    protected DOMException createInvalidFloatTypeDOMException(short t) {
        Object[] p = new Object[] { getPropertyName(), new Integer(t) };
        String s = Messages.formatMessage("invalid.float.type", p);
        return new DOMException(DOMException.INVALID_ACCESS_ERR, s);
    }
    protected DOMException createInvalidFloatValueDOMException(float f) {
        Object[] p = new Object[] { getPropertyName(), new Float(f) };
        String s = Messages.formatMessage("invalid.float.value", p);
        return new DOMException(DOMException.INVALID_ACCESS_ERR, s);
    }
    protected DOMException createInvalidStringTypeDOMException(short t) {
        Object[] p = new Object[] { getPropertyName(), new Integer(t) };
        String s = Messages.formatMessage("invalid.string.type", p);
        return new DOMException(DOMException.INVALID_ACCESS_ERR, s);
    }
    protected DOMException createMalformedLexicalUnitDOMException() {
        Object[] p = new Object[] { getPropertyName() };
        String s = Messages.formatMessage("malformed.lexical.unit", p);
        return new DOMException(DOMException.INVALID_ACCESS_ERR, s);
    }
    protected DOMException createDOMException() {
        Object[] p = new Object[] { getPropertyName() };
        String s = Messages.formatMessage("invalid.access", p);
        return new DOMException(DOMException.NOT_SUPPORTED_ERR, s);
    }
}
