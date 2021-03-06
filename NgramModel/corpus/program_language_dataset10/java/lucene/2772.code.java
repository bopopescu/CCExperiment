package org.apache.solr.common.util;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.solr.common.util.IteratorChain;
import junit.framework.TestCase;
public class IteratorChainTest extends TestCase {
  private Iterator<String> makeIterator(String marker,int howMany) {
    final List<String> c = new ArrayList<String>();
    for(int i = 1; i <= howMany; i++) {
      c.add(marker + i);
    }
    return c.iterator();
  }
  public void testNoIterator() {
    final IteratorChain<String> c = new IteratorChain<String>();
    assertFalse("Empty IteratorChain.hastNext() is false",c.hasNext());
    assertEquals("",getString(c));
  }
  public void testCallNextTooEarly() {
    final IteratorChain<String> c = new IteratorChain<String>();
    c.addIterator(makeIterator("a",3));
    try {
      c.next();
      fail("Calling next() before hasNext() should throw RuntimeException");
    } catch(RuntimeException asExpected) {
    }
  }
  public void testCallAddTooLate() {
    final IteratorChain<String> c = new IteratorChain<String>();
    c.hasNext();
    try {
      c.addIterator(makeIterator("a",3));
      fail("Calling addIterator after hasNext() should throw RuntimeException");
    } catch(RuntimeException asExpected) {
    }
  }
  public void testRemove() {
    final IteratorChain<String> c = new IteratorChain<String>();
    try {
      c.remove();
      fail("Calling remove should throw UnsupportedOperationException");
    } catch(UnsupportedOperationException asExpected) {
    }
  }
  public void testOneIterator() {
    final IteratorChain<String> c = new IteratorChain<String>();
    c.addIterator(makeIterator("a",3));
    assertEquals("a1a2a3",getString(c));
  }
  public void testTwoIterators() {
    final IteratorChain<String> c = new IteratorChain<String>();
    c.addIterator(makeIterator("a",3));
    c.addIterator(makeIterator("b",2));
    assertEquals("a1a2a3b1b2",getString(c));
  }
  public void testEmptyIteratorsInTheMiddle() {
    final IteratorChain<String> c = new IteratorChain<String>();
    c.addIterator(makeIterator("a",3));
    c.addIterator(makeIterator("b",0));
    c.addIterator(makeIterator("c",1));
    assertEquals("a1a2a3c1",getString(c));
  }
  private String getString(Iterator<String> it) {
    final StringBuilder sb = new StringBuilder();
    sb.append("");
    while(it.hasNext()) {
      sb.append(it.next());
    }
    return sb.toString();
  }
}
