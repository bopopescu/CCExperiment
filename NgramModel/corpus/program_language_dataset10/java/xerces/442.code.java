package org.apache.xerces.impl.xs;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.xerces.util.MessageFormatter;
public class XSMessageFormatter implements MessageFormatter {
    public static final String SCHEMA_DOMAIN = "http://www.w3.org/TR/xml-schema-1";
    private Locale fLocale = null;
    private ResourceBundle fResourceBundle = null;
    public String formatMessage(Locale locale, String key, Object[] arguments)
        throws MissingResourceException {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        if (locale != fLocale) {
            fResourceBundle = ResourceBundle.getBundle("org.apache.xerces.impl.msg.XMLSchemaMessages", locale);
            fLocale = locale;
        }
        String msg = fResourceBundle.getString(key);
        if (arguments != null) {
            try {
                msg = java.text.MessageFormat.format(msg, arguments);
            } catch (Exception e) {
                msg = fResourceBundle.getString("FormatFailed");
                msg += " " + fResourceBundle.getString(key);
            }
        } 
        if (msg == null) {
            msg = fResourceBundle.getString("BadMessageKey");
            throw new MissingResourceException(msg, "org.apache.xerces.impl.msg.SchemaMessages", key);
        }
        return msg;
    }
}
