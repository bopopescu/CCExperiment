package org.apache.solr.util;
import java.util.concurrent.atomic.AtomicInteger;
public abstract class RefCounted<Type> {
  protected final Type resource;
  protected final AtomicInteger refcount = new AtomicInteger();
  public RefCounted(Type resource) {
    this.resource = resource;
  }
  public int getRefcount() {
    return refcount.get();
  }
  public final RefCounted<Type> incref() {
    refcount.incrementAndGet();
    return this;
  }
  public final Type get() {
    return resource;
  }
  public void decref() {
    if (refcount.decrementAndGet() == 0) {
      close();
    }
  }
  protected abstract void close();
}
