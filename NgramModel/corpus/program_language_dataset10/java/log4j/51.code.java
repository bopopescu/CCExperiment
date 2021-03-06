package examples.subclass;
import org.apache.log4j.*;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.LogLog;
public class MyLoggerTest {
  static public void main(String[] args) {
    if(args.length == 0) {
      Logger root = Logger.getRootLogger();
      Layout layout = new PatternLayout("%p [%t] %c (%F:%L) - %m%n");
      root.addAppender(new ConsoleAppender(layout, ConsoleAppender.SYSTEM_OUT));
    }
    else if(args.length == 1) {
      if(args[0].endsWith("xml")) {
	DOMConfigurator.configure(args[0]);
      } else {
	PropertyConfigurator.configure(args[0]);
      }
    } else {
      usage("Incorrect number of parameters.");
    }
    try {
      MyLogger c = (MyLogger) MyLogger.getLogger("some.cat");    
      c.trace("Hello");
      c.debug("Hello");
    } catch(ClassCastException e) {
      LogLog.error("Did you forget to set the factory in the config file?", e);
    }
  }
  static
  void usage(String errMsg) {
    System.err.println(errMsg);
    System.err.println("\nUsage: "+MyLogger.class.getName() + "[configFile]\n"
                + " where *configFile* is an optional configuration file, "+
		       "either in properties or XML format.");
    System.exit(1);
  }
}
