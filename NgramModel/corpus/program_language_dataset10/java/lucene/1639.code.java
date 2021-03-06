package org.apache.lucene.search;
import org.apache.lucene.index.IndexReader;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;
public class CachingSpanFilter extends SpanFilter {
  private SpanFilter filter;
  private transient Map<IndexReader,SpanFilterResult> cache;
  private final ReentrantLock lock = new ReentrantLock();
  public CachingSpanFilter(SpanFilter filter) {
    this.filter = filter;
  }
  @Override
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
    SpanFilterResult result = getCachedResult(reader);
    return result != null ? result.getDocIdSet() : null;
  }
  private SpanFilterResult getCachedResult(IndexReader reader) throws IOException {
    lock.lock();
    try {
      if (cache == null) {
        cache = new WeakHashMap<IndexReader,SpanFilterResult>();
      }
      final SpanFilterResult cached = cache.get(reader);
      if (cached != null) return cached;
    } finally {
      lock.unlock();
    }
    final SpanFilterResult result = filter.bitSpans(reader);
    lock.lock();
    try {
      cache.put(reader, result);
    } finally {
      lock.unlock();
    }
    return result;
  }
  @Override
  public SpanFilterResult bitSpans(IndexReader reader) throws IOException {
    return getCachedResult(reader);
  }
  @Override
  public String toString() {
    return "CachingSpanFilter("+filter+")";
  }
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CachingSpanFilter)) return false;
    return this.filter.equals(((CachingSpanFilter)o).filter);
  }
  @Override
  public int hashCode() {
    return filter.hashCode() ^ 0x1117BF25;
  }
}
