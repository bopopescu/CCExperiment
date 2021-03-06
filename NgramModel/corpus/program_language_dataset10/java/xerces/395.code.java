package org.apache.xerces.impl.io;
import java.io.CharConversionException;
import java.util.Locale;
import org.apache.xerces.util.MessageFormatter;
public final class MalformedByteSequenceException extends CharConversionException {
    static final long serialVersionUID = 8436382245048328739L;
    private MessageFormatter fFormatter;
    private Locale fLocale;
    private String fDomain;
    private String fKey;
    private Object[] fArguments;
    private String fMessage;
    public MalformedByteSequenceException(MessageFormatter formatter,
        Locale locale, String domain, String key, Object[] arguments) {
        fFormatter = formatter;
        fLocale = locale;
        fDomain = domain;
        fKey = key;
        fArguments = arguments;
    } 
    public String getDomain () {
    	return fDomain;
    } 
    public String getKey () {
    	return fKey;
    } 
    public Object[] getArguments () {
    	return fArguments;
    } 
    public synchronized String getMessage() {
        if (fMessage == null) {
            fMessage = fFormatter.formatMessage(fLocale, fKey, fArguments);
            fFormatter = null;
            fLocale = null;
        }
        return fMessage;
     } 
} 
