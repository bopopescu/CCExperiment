package org.apache.log4j.net;
import junit.framework.TestCase;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;
import org.apache.log4j.xml.DOMConfigurator;
public class SMTPAppenderTest extends TestCase {
    public SMTPAppenderTest(final String testName) {
        super(testName);
    }
  public void tearDown() {
      LogManager.resetConfiguration();
  }
  public static final class MockTriggeringEventEvaluator implements TriggeringEventEvaluator {
      public boolean isTriggeringEvent(final LoggingEvent event) {
          return true;
      }
  }
  public void testTrigger() {
      DOMConfigurator.configure("input/xml/smtpAppender1.xml");
      SMTPAppender appender = (SMTPAppender) Logger.getRootLogger().getAppender("A1");
      TriggeringEventEvaluator evaluator = appender.getEvaluator();
      assertTrue(evaluator instanceof MockTriggeringEventEvaluator);
  }
}
