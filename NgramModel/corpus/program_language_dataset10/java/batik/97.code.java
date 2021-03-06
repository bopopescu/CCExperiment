package org.apache.batik.apps.svgbrowser;
import java.util.Locale;
import java.util.MissingResourceException;
import org.apache.batik.i18n.LocalizableSupport;
import org.apache.batik.util.resources.ResourceManager;
public class Resources {
    protected Resources() { }
    protected static final String RESOURCES =
        "org.apache.batik.apps.svgbrowser.resources.GUI";
    protected static LocalizableSupport localizableSupport =
        new LocalizableSupport(RESOURCES, Resources.class.getClassLoader());
    protected static ResourceManager resourceManager =
        new ResourceManager(localizableSupport.getResourceBundle());
    public static void setLocale(Locale l) {
        localizableSupport.setLocale(l);
        resourceManager = new ResourceManager(localizableSupport.getResourceBundle());
    }
    public static Locale getLocale() {
        return localizableSupport.getLocale();
    }
    public static String formatMessage(String key, Object[] args)
        throws MissingResourceException {
        return localizableSupport.formatMessage(key, args);
    }
    public static String getString(String key)
        throws MissingResourceException {
        return resourceManager.getString(key);
    }
    public static int getInteger(String key)
        throws MissingResourceException {
        return resourceManager.getInteger(key);
    }
    public static int getCharacter(String key)
        throws MissingResourceException {
        return resourceManager.getCharacter(key);
    }
}
