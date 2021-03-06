package org.apache.lucene.util;
import junit.framework.TestCase;
public class TestRamUsageEstimator extends TestCase {
  public void testBasic() {
    RamUsageEstimator rue = new RamUsageEstimator();
    rue.estimateRamUsage("test str");
    rue.estimateRamUsage("test strin");
    Holder holder = new Holder();
    holder.holder = new Holder("string2", 5000L);
    rue.estimateRamUsage(holder);
    String[] strings = new String[]{new String("test strin"), new String("hollow"), new String("catchmaster")};
    rue.estimateRamUsage(strings);
  }
  private static final class Holder {
    long field1 = 5000L;
    String name = "name";
    Holder holder;
    Holder() {
    }
    Holder(String name, long field1) {
      this.name = name;
      this.field1 = field1;
    }
  }
}
