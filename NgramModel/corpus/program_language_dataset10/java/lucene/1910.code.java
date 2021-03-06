package org.apache.lucene.index;
import org.apache.lucene.util.LuceneTestCase;
public class TestTerm extends LuceneTestCase {
  public void testEquals() {
    final Term base = new Term("same", "same");
    final Term same = new Term("same", "same");
    final Term differentField = new Term("different", "same");
    final Term differentText = new Term("same", "different");
    final String differentType = "AString";
    assertEquals(base, base);
    assertEquals(base, same);
    assertFalse(base.equals(differentField));
    assertFalse(base.equals(differentText));
    assertFalse(base.equals(differentType));
  }
}
