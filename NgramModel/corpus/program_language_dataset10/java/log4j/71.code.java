package org.apache.log4j;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.RepositorySelector;
import org.apache.log4j.spi.DefaultRepositorySelector;
import org.apache.log4j.spi.RootLogger;
import org.apache.log4j.spi.NOPLoggerRepository;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.helpers.LogLog;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.io.StringWriter;
import java.io.PrintWriter;
public class LogManager {
  static public final String DEFAULT_CONFIGURATION_FILE = "log4j.properties";
  static final String DEFAULT_XML_CONFIGURATION_FILE = "log4j.xml";  
  static final public String DEFAULT_CONFIGURATION_KEY="log4j.configuration";
  static final public String CONFIGURATOR_CLASS_KEY="log4j.configuratorClass";
  public static final String DEFAULT_INIT_OVERRIDE_KEY = 
                                                 "log4j.defaultInitOverride";
  static private Object guard = null;
  static private RepositorySelector repositorySelector;
  static {
    Hierarchy h = new Hierarchy(new RootLogger((Level) Level.DEBUG));
    repositorySelector = new DefaultRepositorySelector(h);
    String override =OptionConverter.getSystemProperty(DEFAULT_INIT_OVERRIDE_KEY,
						       null);
    if(override == null || "false".equalsIgnoreCase(override)) {
      String configurationOptionStr = OptionConverter.getSystemProperty(
							  DEFAULT_CONFIGURATION_KEY, 
							  null);
      String configuratorClassName = OptionConverter.getSystemProperty(
                                                   CONFIGURATOR_CLASS_KEY, 
						   null);
      URL url = null;
      if(configurationOptionStr == null) {	
	url = Loader.getResource(DEFAULT_XML_CONFIGURATION_FILE);
	if(url == null) {
	  url = Loader.getResource(DEFAULT_CONFIGURATION_FILE);
	}
      } else {
	try {
	  url = new URL(configurationOptionStr);
	} catch (MalformedURLException ex) {
	  url = Loader.getResource(configurationOptionStr); 
	}	
      }
      if(url != null) {
	    LogLog.debug("Using URL ["+url+"] for automatic log4j configuration.");
        try {
            OptionConverter.selectAndConfigure(url, configuratorClassName,
					   LogManager.getLoggerRepository());
        } catch (NoClassDefFoundError e) {
            LogLog.warn("Error during default initialization", e);
        }
      } else {
	    LogLog.debug("Could not find resource: ["+configurationOptionStr+"].");
      }
    } else {
        LogLog.debug("Default initialization of overridden by " + 
            DEFAULT_INIT_OVERRIDE_KEY + "property."); 
    }  
  } 
  static
  public
  void setRepositorySelector(RepositorySelector selector, Object guard) 
                                                 throws IllegalArgumentException {
    if((LogManager.guard != null) && (LogManager.guard != guard)) {
      throw new IllegalArgumentException(
           "Attempted to reset the LoggerFactory without possessing the guard.");
    }
    if(selector == null) {
      throw new IllegalArgumentException("RepositorySelector must be non-null.");
    }
    LogManager.guard = guard;
    LogManager.repositorySelector = selector;
  }
  private static boolean isLikelySafeScenario(final Exception ex) {
      StringWriter stringWriter = new StringWriter();
      ex.printStackTrace(new PrintWriter(stringWriter));
      String msg = stringWriter.toString();
      return msg.indexOf("org.apache.catalina.loader.WebappClassLoader.stop") != -1;
  }
  static
  public
  LoggerRepository getLoggerRepository() {
    if (repositorySelector == null) {
        repositorySelector = new DefaultRepositorySelector(new NOPLoggerRepository());
        guard = null;
        Exception ex = new IllegalStateException("Class invariant violation");
        String msg =
                "log4j called after unloading, see http://logging.apache.org/log4j/1.2/faq.html#unload.";
        if (isLikelySafeScenario(ex)) {
            LogLog.debug(msg, ex);
        } else {
            LogLog.error(msg, ex);
        }
    }
    return repositorySelector.getLoggerRepository();
  }
  public
  static 
  Logger getRootLogger() {
    return getLoggerRepository().getRootLogger();
  }
  public
  static 
  Logger getLogger(final String name) {
    return getLoggerRepository().getLogger(name);
  }
  public
  static 
  Logger getLogger(final Class clazz) {
    return getLoggerRepository().getLogger(clazz.getName());
  }
  public
  static 
  Logger getLogger(final String name, final LoggerFactory factory) {
    return getLoggerRepository().getLogger(name, factory);
  }  
  public
  static
  Logger exists(final String name) {
    return getLoggerRepository().exists(name);
  }
  public
  static
  Enumeration getCurrentLoggers() {
    return getLoggerRepository().getCurrentLoggers();
  }
  public
  static
  void shutdown() {
    getLoggerRepository().shutdown();
  }
  public
  static
  void resetConfiguration() {
    getLoggerRepository().resetConfiguration();
  }
}
