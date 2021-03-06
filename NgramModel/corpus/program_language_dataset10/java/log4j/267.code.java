package org.apache.log4j;
import junit.framework.TestCase;
import java.lang.reflect.Method;
public class CategoryTest extends TestCase {
  public CategoryTest(final String name) {
    super(name);
  }
  public void testForcedLog() {
    MockCategory category = new MockCategory("org.example.foo");
    category.setAdditivity(false);
    category.addAppender(new VectorAppender());
    category.info("Hello, World");
  }
  public void testGetChainedPriorityReturnType() throws Exception {
    Method method = Category.class.getMethod("getChainedPriority", (Class[]) null);
    assertTrue(method.getReturnType() == Priority.class);
  }
  public void testL7dlog() {
    Logger logger = Logger.getLogger("org.example.foo");
    logger.setLevel(Level.ERROR);
    Priority debug = Level.DEBUG;
    logger.l7dlog(debug, "Hello, World", null);
  }
  public void testL7dlog4Param() {
    Logger logger = Logger.getLogger("org.example.foo");
    logger.setLevel(Level.ERROR);
    Priority debug = Level.DEBUG;
    logger.l7dlog(debug, "Hello, World", new Object[0], null);
  }
  public void testSetPriority() {
    Logger logger = Logger.getLogger("org.example.foo");
    Priority debug = Level.DEBUG;
    logger.setPriority(debug);
  }
  private static class MockCategory extends Logger {
    public MockCategory(final String name) {
      super(name);
      repository = new Hierarchy(this);
    }
    public void info(final String msg) {
      Priority info = Level.INFO;
      forcedLog(MockCategory.class.toString(), info, msg, null);
    }
  }
}
