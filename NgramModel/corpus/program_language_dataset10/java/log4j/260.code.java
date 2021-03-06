package org.apache.log4j.xml.examples;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.Logger;
public class XMLSample {
  static Logger cat = Logger.getLogger(XMLSample.class);
  public
  static
  void main(String argv[]) {
    if(argv.length == 1)
      init(argv[0]);
    else
      Usage("Wrong number of arguments.");
    sample();
  }
  static
  void Usage(String msg) {
    System.err.println(msg);
    System.err.println( "Usage: java " + XMLSample.class.getName() +
			"configFile");
    System.exit(1);
  }
  static
  void init(String configFile) {
    DOMConfigurator.configure(configFile);
  }
  static
  void sample() {
    int i = -1;
    cat.debug("Message " + ++i);
    cat.warn ("Message " + ++i);
    cat.error("Message " + ++i);
    Exception e = new Exception("Just testing");
    cat.debug("Message " + ++i, e);
  }
}
