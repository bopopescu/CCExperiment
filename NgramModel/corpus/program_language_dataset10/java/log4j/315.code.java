package org.apache.log4j.net;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import org.apache.log4j.*;
import org.apache.log4j.util.*;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.apache.log4j.xml.XLevel;
public class SocketServerTestCase extends TestCase {
  static String TEMP = "output/temp";
  static String FILTERED = "output/filtered";
  static String PAT1 = "^(TRACE|DEBUG| INFO| WARN|ERROR|FATAL|LETHAL) T1 \\[main]\\ "
                       + ".* Message \\d{1,2}";
  static String PAT2 = "^(TRACE|DEBUG| INFO| WARN|ERROR|FATAL|LETHAL) T2 \\[main]\\ "
                       + "\\? \\(\\?:\\?\\) Message \\d{1,2}";
  static String PAT3 = "^(TRACE|DEBUG| INFO| WARN|ERROR|FATAL|LETHAL) T3 \\[main]\\ "
                       + "org.apache.log4j.net.SocketServerTestCase "
                       + "\\(SocketServerTestCase.java:\\d{3}\\) Message \\d{1,2}";
  static String PAT4 = "^(TRACE|DEBUG| INFO| WARN|ERROR|FATAL|LETHAL) some T4 MDC-TEST4 \\[main]\\"
                       + " (root|SocketServerTestCase) - Message \\d{1,2}";
  static String PAT5 = "^(TRACE|DEBUG| INFO| WARN|ERROR|FATAL|LETHAL) some5 T5 MDC-TEST5 \\[main]\\"
                       + " (root|SocketServerTestCase) - Message \\d{1,2}";
  static String PAT6 = "^(TRACE|DEBUG| INFO| WARN|ERROR|FATAL|LETHAL) some6 T6 client-test6 MDC-TEST6"
                       + " \\[main]\\ (root|SocketServerTestCase) - Message \\d{1,2}";
  static String PAT7 = "^(TRACE|DEBUG| INFO| WARN|ERROR|FATAL|LETHAL) some7 T7 client-test7 MDC-TEST7"
                       + " \\[main]\\ (root|SocketServerTestCase) - Message \\d{1,2}";
  static String PAT8 = "^(TRACE|DEBUG| INFO| WARN|ERROR|FATAL|LETHAL) some8 T8 shortSocketServer"
                       + " MDC-TEST8 \\[main]\\ (root|SocketServerTestCase) - Message \\d{1,2}";
  static String EXCEPTION1 = "java.lang.Exception: Just testing";
  static String EXCEPTION2 = "\\s*at .*\\(.*\\)";
  static String EXCEPTION3 = "\\s*at .*\\(Native Method\\)";
  static String EXCEPTION4 = "\\s*at .*\\(.*Compiled Code\\)";
  static String EXCEPTION5 = "\\s*at .*\\(.*libgcj.*\\)";
  static Logger logger = Logger.getLogger(SocketServerTestCase.class);
  static public final int PORT = 12345;  
  static Logger rootLogger = Logger.getRootLogger();
  SocketAppender socketAppender;
  public SocketServerTestCase(String name) {
    super(name);
  }
  public void setUp() {
    System.out.println("Setting up test case.");
  }
  public void tearDown() {
    System.out.println("Tearing down test case.");
    socketAppender = null;
    rootLogger.removeAllAppenders();
  }
  public void test1() throws Exception {
    socketAppender = new SocketAppender("localhost", PORT);
    rootLogger.addAppender(socketAppender);
    common("T1", "key1", "MDC-TEST1");
    delay(1);
    ControlFilter cf = new ControlFilter(new String[]{PAT1, EXCEPTION1, 
						       EXCEPTION2, EXCEPTION3, EXCEPTION4, EXCEPTION5});
    Transformer.transform(
      TEMP, FILTERED,
      new Filter[] { cf, new LineNumberFilter(), 
          new JunitTestRunnerFilter(),
          new SunReflectFilter() });
    assertTrue(Compare.compare(FILTERED, "witness/socketServer.1"));
  }
  public void test2() throws Exception {
    socketAppender = new SocketAppender("localhost", PORT);
    rootLogger.addAppender(socketAppender);
    common("T2", "key2", "MDC-TEST2");
    delay(1);
    ControlFilter cf = new ControlFilter(new String[]{PAT2, EXCEPTION1, 
						       EXCEPTION2, EXCEPTION3, EXCEPTION4, EXCEPTION5});
    Transformer.transform(
      TEMP, FILTERED,
      new Filter[] { cf, new LineNumberFilter(), 
          new JunitTestRunnerFilter(),
          new SunReflectFilter() });
    assertTrue(Compare.compare(FILTERED, "witness/socketServer.2"));
  }
  public void test3() throws Exception {
    socketAppender = new SocketAppender("localhost", PORT);
    socketAppender.setLocationInfo(true);
    rootLogger.addAppender(socketAppender);
    common("T3", "key3", "MDC-TEST3");
    delay(1);
    ControlFilter cf = new ControlFilter(new String[]{PAT3, EXCEPTION1, 
						       EXCEPTION2, EXCEPTION3, EXCEPTION4, EXCEPTION5});
    Transformer.transform(
      TEMP, FILTERED,
      new Filter[] { cf, new LineNumberFilter(), 
          new JunitTestRunnerFilter(),
          new SunReflectFilter() });
    assertTrue(Compare.compare(FILTERED, "witness/socketServer.3"));
  }
  public void test4() throws Exception {
    socketAppender = new SocketAppender("localhost", PORT);
    socketAppender.setLocationInfo(true);
    rootLogger.addAppender(socketAppender);
    NDC.push("some");
    common("T4", "key4", "MDC-TEST4");
    NDC.pop();
    delay(1);
    if(!System.getProperty("java.version").startsWith("1.1.")) {
        ControlFilter cf = new ControlFilter(new String[]{PAT4, EXCEPTION1, 
						           EXCEPTION2, EXCEPTION3, EXCEPTION4, EXCEPTION5});
        Transformer.transform(
          TEMP, FILTERED,
          new Filter[] { cf, new LineNumberFilter(), 
              new JunitTestRunnerFilter(),
              new SunReflectFilter() });
        assertTrue(Compare.compare(FILTERED, "witness/socketServer.4"));
    }
  }
  public void test5() throws Exception {
    socketAppender = new SocketAppender("localhost", PORT);
    socketAppender.setLocationInfo(true);
    AsyncAppender asyncAppender = new AsyncAppender();
    asyncAppender.setLocationInfo(true);
    asyncAppender.addAppender(socketAppender);
    rootLogger.addAppender(asyncAppender);
    NDC.push("some5");
    common("T5", "key5", "MDC-TEST5");
    NDC.pop();
    delay(2);
    if(!System.getProperty("java.version").startsWith("1.1.")) {
        ControlFilter cf = new ControlFilter(new String[]{PAT5, EXCEPTION1, 
						           EXCEPTION2, EXCEPTION3, EXCEPTION4, EXCEPTION5});
        Transformer.transform(
          TEMP, FILTERED,
          new Filter[] { cf, new LineNumberFilter(), 
              new JunitTestRunnerFilter(),
              new SunReflectFilter() });
        assertTrue(Compare.compare(FILTERED, "witness/socketServer.5"));
    }
  }
  public void test6() throws Exception {
    socketAppender = new SocketAppender("localhost", PORT);
    socketAppender.setLocationInfo(true);
    AsyncAppender asyncAppender = new AsyncAppender();
    asyncAppender.setLocationInfo(true);
    asyncAppender.addAppender(socketAppender);
    rootLogger.addAppender(asyncAppender);
    NDC.push("some6");
    MDC.put("hostID", "client-test6");
    common("T6", "key6", "MDC-TEST6");
    NDC.pop();
    MDC.remove("hostID");
    delay(2);
    if(!System.getProperty("java.version").startsWith("1.1.")) {
        ControlFilter cf = new ControlFilter(new String[]{PAT6, EXCEPTION1, 
						           EXCEPTION2, EXCEPTION3, EXCEPTION4, EXCEPTION5});
        Transformer.transform(
          TEMP, FILTERED,
          new Filter[] { cf, new LineNumberFilter(), 
              new JunitTestRunnerFilter(),
              new SunReflectFilter() });
        assertTrue(Compare.compare(FILTERED, "witness/socketServer.6"));
    }
  }
  public void test7() throws Exception {
    socketAppender = new SocketAppender("localhost", PORT);
    socketAppender.setLocationInfo(true);
    rootLogger.addAppender(socketAppender);
    NDC.push("some7");
    MDC.put("hostID", "client-test7");
    common("T7", "key7", "MDC-TEST7");
    NDC.pop();
    MDC.remove("hostID"); 
    delay(2);
    if(!System.getProperty("java.version").startsWith("1.1.")) {
        ControlFilter cf = new ControlFilter(new String[]{PAT7, EXCEPTION1, 
						           EXCEPTION2, EXCEPTION3, EXCEPTION4, EXCEPTION5});
        Transformer.transform(
          TEMP, FILTERED,
          new Filter[] { cf, new LineNumberFilter(), 
              new JunitTestRunnerFilter(),
              new SunReflectFilter() });
        assertTrue(Compare.compare(FILTERED, "witness/socketServer.7"));
    }
  }
  public void test8() throws Exception {
    socketAppender = new SocketAppender("localhost", PORT);
    socketAppender.setLocationInfo(true);
    rootLogger.addAppender(socketAppender);
    NDC.push("some8");
    if (System.getProperty("java.vendor").indexOf("Apache") != -1) {
        MDC.put("hostID", "shortSocketServer");
    }
    common("T8", "key8", "MDC-TEST8");
    NDC.pop();
    delay(2);
    if(!System.getProperty("java.version").startsWith("1.1.")) {
        ControlFilter cf = new ControlFilter(new String[]{PAT8, EXCEPTION1, 
						           EXCEPTION2, EXCEPTION3, EXCEPTION4, EXCEPTION5});
        Transformer.transform(
          TEMP, FILTERED,
          new Filter[] { cf, new LineNumberFilter(), 
              new JunitTestRunnerFilter(),
              new SunReflectFilter() });
        assertTrue(Compare.compare(FILTERED, "witness/socketServer.8"));
    }
  }
  static 
  void common(String dc, String key, Object o) {
    String oldThreadName = Thread.currentThread().getName();
    Thread.currentThread().setName("main");
    int i = -1; 
    NDC.push(dc); 
    MDC.put(key, o);
    Logger root = Logger.getRootLogger();
    logger.setLevel(Level.DEBUG);
    rootLogger.setLevel(Level.DEBUG);
    logger.log(XLevel.TRACE, "Message " + ++i);
    logger.setLevel(Level.TRACE);
    rootLogger.setLevel(Level.TRACE);
    logger.trace("Message " + ++i);
    root.trace("Message " + ++i);
    logger.debug("Message " + ++i);
    root.debug("Message " + ++i);
    logger.info("Message " + ++i);
    logger.warn("Message " + ++i);
    logger.log(XLevel.LETHAL, "Message " + ++i); 
    Exception e = new Exception("Just testing");
    logger.debug("Message " + ++i, e);
    root.error("Message " + ++i, e);
    NDC.pop();
    MDC.remove(key);
    Thread.currentThread().setName(oldThreadName);
  }
  public void delay(int secs) {
    try {Thread.sleep(secs*1000);} catch(Exception e) {}
  }
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new SocketServerTestCase("test1"));
    suite.addTest(new SocketServerTestCase("test2"));
    suite.addTest(new SocketServerTestCase("test3"));
    suite.addTest(new SocketServerTestCase("test4"));
    suite.addTest(new SocketServerTestCase("test5"));
    suite.addTest(new SocketServerTestCase("test6"));
    suite.addTest(new SocketServerTestCase("test7"));
    suite.addTest(new SocketServerTestCase("test8"));
    return suite;
  }
}
