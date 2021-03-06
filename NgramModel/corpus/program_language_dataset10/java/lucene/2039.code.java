package org.apache.lucene.util;
public class TestVersion extends LuceneTestCase {
  public void test() {
    for (Version v : Version.values()) {
      assertTrue("LUCENE_CURRENT must be always onOrAfter("+v+")", Version.LUCENE_CURRENT.onOrAfter(v));
    }
    assertTrue(Version.LUCENE_30.onOrAfter(Version.LUCENE_29));
    assertTrue(Version.LUCENE_30.onOrAfter(Version.LUCENE_30));
    assertFalse(Version.LUCENE_29.onOrAfter(Version.LUCENE_30));
  }
}
