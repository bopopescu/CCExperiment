package examples.subclass;
import org.apache.log4j.*;
import examples.customLevel.XLevel;
public class MyLogger extends Logger {
  static String FQCN = MyLogger.class.getName() + ".";
  private static MyLoggerFactory myFactory = new MyLoggerFactory();
  public MyLogger(String name) {
    super(name);
  }
  public 
  void debug(Object message) {
    super.log(FQCN, Level.DEBUG, message + " world.", null);    
  }
  public 
  static
  Logger getLogger(String name) {
    return Logger.getLogger(name, myFactory); 
  }
  public
  void trace(Object message) {
    super.log(FQCN, XLevel.TRACE, message, null); 
  }
}
