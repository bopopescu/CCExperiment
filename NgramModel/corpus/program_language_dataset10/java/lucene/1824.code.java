package org.apache.lucene.util.cache;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
@Deprecated
public class SimpleMapCache<K,V> extends Cache<K,V> {
  protected Map<K,V> map;
  public SimpleMapCache() {
    this(new HashMap<K,V>());
  }
  public SimpleMapCache(Map<K,V> map) {
    this.map = map;
  }
  @Override
  public V get(Object key) {
    return map.get(key);
  }
  @Override
  public void put(K key, V value) {
    map.put(key, value);
  }
  @Override
  public void close() {
  }
  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }
  public Set<K> keySet() {
    return map.keySet();
  }
  @Override
  Cache<K,V> getSynchronizedCache() {
    return new SynchronizedSimpleMapCache<K,V>(this);
  }
  private static class SynchronizedSimpleMapCache<K,V> extends SimpleMapCache<K,V> {
    private Object mutex;
    private SimpleMapCache<K,V> cache;
    SynchronizedSimpleMapCache(SimpleMapCache<K,V> cache) {
        this.cache = cache;
        this.mutex = this;
    }
    @Override
    public void put(K key, V value) {
        synchronized(mutex) {cache.put(key, value);}
    }
    @Override
    public V get(Object key) {
        synchronized(mutex) {return cache.get(key);}
    }
    @Override
    public boolean containsKey(Object key) {
        synchronized(mutex) {return cache.containsKey(key);}
    }
    @Override
    public void close() {
        synchronized(mutex) {cache.close();}
    }
    @Override
    public Set<K> keySet() {
      synchronized(mutex) {return cache.keySet();}
    }
    @Override
    Cache<K,V> getSynchronizedCache() {
      return this;
    }
  }
}
