package org.apache.batik.dom;
import java.io.Serializable;
import java.util.Locale;
import java.util.MissingResourceException;
import org.apache.batik.dom.events.DocumentEventSupport;
import org.apache.batik.dom.events.EventSupport;
import org.apache.batik.i18n.Localizable;
import org.apache.batik.i18n.LocalizableSupport;
import org.apache.batik.dom.util.HashTable;
import org.w3c.dom.DOMImplementation;
public abstract class AbstractDOMImplementation
        implements DOMImplementation,
                   Localizable,
                   Serializable {
    protected static final String RESOURCES =
        "org.apache.batik.dom.resources.Messages";
    protected LocalizableSupport localizableSupport =
        new LocalizableSupport(RESOURCES, getClass().getClassLoader());
    protected final HashTable features = new HashTable();
    {
        registerFeature("Core",               new String[] { "2.0", "3.0" });
        registerFeature("XML",                new String[] { "1.0", "2.0",
                                                             "3.0" });
        registerFeature("Events",             new String[] { "2.0", "3.0" });
        registerFeature("UIEvents",           new String[] { "2.0", "3.0" });
        registerFeature("MouseEvents",        new String[] { "2.0", "3.0" });
        registerFeature("TextEvents",         "3.0");
        registerFeature("KeyboardEvents",     "3.0");
        registerFeature("MutationEvents",     new String[] { "2.0", "3.0" });
        registerFeature("MutationNameEvents", "3.0");
        registerFeature("Traversal",          "2.0");
        registerFeature("XPath",              "3.0");
    }
    protected void registerFeature(String name, Object value) {
        features.put(name.toLowerCase(), value);
    }
    protected AbstractDOMImplementation() {
    }
    public boolean hasFeature(String feature, String version) {
        if (feature == null || feature.length() == 0) {
            return false;
        }
        if (feature.charAt(0) == '+') {
            feature = feature.substring(1);
        }
        Object v = features.get(feature.toLowerCase());
        if (v == null) {
            return false;
        }
        if (version == null || version.length() == 0) {
            return true;
        }
        if (v instanceof String) {
            return version.equals(v);
        } else {
            String[] va = (String[])v;
            for (int i = 0; i < va.length; i++) {
                if (version.equals(va[i])) {
                    return true;
                }
            }
            return false;
        }
    }
    public Object getFeature(String feature, String version) {
        if (hasFeature(feature, version)) {
            return this;
        }
        return null;
    }
    public DocumentEventSupport createDocumentEventSupport() {
        return new DocumentEventSupport();
    }
    public EventSupport createEventSupport(AbstractNode n) {
        return new EventSupport(n);
    }
    public void setLocale(Locale l) {
        localizableSupport.setLocale(l);
    }
    public Locale getLocale() {
        return localizableSupport.getLocale();
    }
    protected void initLocalizable() {
    }
    public String formatMessage(String key, Object[] args)
        throws MissingResourceException {
        return localizableSupport.formatMessage(key, args);
    }
}
