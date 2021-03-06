package org.apache.solr.common.util;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class IteratorChain<E> implements Iterator<E> {
  private final List<Iterator<E>> iterators = new ArrayList<Iterator<E>>();
  private Iterator<Iterator<E>> itit;
  private Iterator<E> current;
  public void addIterator(Iterator<E> it) {
    if(itit!=null) throw new RuntimeException("all Iterators must be added before calling hasNext()");
    iterators.add(it);
  }
  public boolean hasNext() {
    if(itit==null) itit = iterators.iterator();
    return recursiveHasNext();
  }
  private boolean recursiveHasNext() {
    if(current==null) {
      if(itit.hasNext()) {
        current=itit.next();
      } else {
        return false;
      }
    }
    boolean result = current.hasNext();
    if(!result) {
      current = null;
      result = recursiveHasNext();
    }
    return result;
  }
  public E next() {
    if(current==null) { 
      throw new RuntimeException("For an IteratorChain, hasNext() MUST be called before calling next()");
    }
    return current.next();
  }
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
