package org.apache.xerces.impl.dv.dtd;
import org.apache.xerces.impl.dv.DatatypeValidator;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidationContext;
import org.apache.xerces.util.XMLChar;
public class IDREFDatatypeValidator implements DatatypeValidator {
    public IDREFDatatypeValidator() {
    }
    public void validate(String content, ValidationContext context) throws InvalidDatatypeValueException {
        if(context.useNamespaces()) {
            if (!XMLChar.isValidNCName(content)) {
                throw new InvalidDatatypeValueException("IDREFInvalidWithNamespaces", new Object[]{content});
            }
        }
        else {
            if (!XMLChar.isValidName(content)) {
                throw new InvalidDatatypeValueException("IDREFInvalid", new Object[]{content});
            }
        }
        context.addIdRef(content);
    }
}
