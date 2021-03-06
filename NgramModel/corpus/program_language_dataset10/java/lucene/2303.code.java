package org.apache.solr.core;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.store.Directory;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
public class IndexDeletionPolicyWrapper implements IndexDeletionPolicy {
  private final IndexDeletionPolicy deletionPolicy;
  private volatile Map<Long, IndexCommit> solrVersionVsCommits = new ConcurrentHashMap<Long, IndexCommit>();
  private final Map<Long, Long> reserves = new ConcurrentHashMap<Long,Long>();
  private volatile IndexCommit latestCommit;
  private final ConcurrentHashMap<Long, AtomicInteger> savedCommits = new ConcurrentHashMap<Long, AtomicInteger>();
  public IndexDeletionPolicyWrapper(IndexDeletionPolicy deletionPolicy) {
    this.deletionPolicy = deletionPolicy;
  }
  public IndexCommit getLatestCommit() {
    return latestCommit;
  }
  public IndexDeletionPolicy getWrappedDeletionPolicy() {
    return deletionPolicy;
  }
  public void setReserveDuration(Long indexVersion, long reserveTime) {
    long timeToSet = System.currentTimeMillis() + reserveTime;
    for(;;) {
      Long previousTime = reserves.put(indexVersion, timeToSet);
      if (previousTime == null || previousTime <= timeToSet) break;
      timeToSet = previousTime;      
    }
  }
  private void cleanReserves() {
    long currentTime = System.currentTimeMillis();
    for (Map.Entry<Long, Long> entry : reserves.entrySet()) {
      if (entry.getValue() < currentTime) {
        reserves.remove(entry.getKey());
      }
    }
  }
  private List<IndexCommitWrapper> wrap(List<IndexCommit> list) {
    List<IndexCommitWrapper> result = new ArrayList<IndexCommitWrapper>();
    for (IndexCommit indexCommit : list) result.add(new IndexCommitWrapper(indexCommit));
    return result;
  }
  public synchronized void saveCommitPoint(Long indexCommitVersion) {
    AtomicInteger reserveCount = savedCommits.get(indexCommitVersion);
    if (reserveCount == null) reserveCount = new AtomicInteger();
    reserveCount.incrementAndGet();
  }
  public synchronized void releaseCommitPoint(Long indexCommitVersion) {
    AtomicInteger reserveCount = savedCommits.get(indexCommitVersion);
    if (reserveCount == null) return;
    if (reserveCount.decrementAndGet() <= 0) {
      savedCommits.remove(indexCommitVersion);
    }
  }
  public void onInit(List list) throws IOException {
    List<IndexCommitWrapper> wrapperList = wrap(list);
    deletionPolicy.onInit(wrapperList);
    updateCommitPoints(wrapperList);
    cleanReserves();
  }
  public void onCommit(List list) throws IOException {
    List<IndexCommitWrapper> wrapperList = wrap(list);
    deletionPolicy.onCommit(wrapperList);
    updateCommitPoints(wrapperList);
    cleanReserves();
  }
  private class IndexCommitWrapper extends IndexCommit {
    IndexCommit delegate;
    IndexCommitWrapper(IndexCommit delegate) {
      this.delegate = delegate;
    }
    @Override
    public String getSegmentsFileName() {
      return delegate.getSegmentsFileName();
    }
    @Override
    public Collection getFileNames() throws IOException {
      return delegate.getFileNames();
    }
    @Override
    public Directory getDirectory() {
      return delegate.getDirectory();
    }
    @Override
    public void delete() {
      Long version = delegate.getVersion();
      Long reserve = reserves.get(version);
      if (reserve != null && System.currentTimeMillis() < reserve) return;
      if(savedCommits.contains(version)) return;
      delegate.delete();
    }
    @Override
    public boolean isOptimized() {
      return delegate.isOptimized();
    }
    @Override
    public boolean equals(Object o) {
      return delegate.equals(o);
    }
    @Override
    public int hashCode() {
      return delegate.hashCode();
    }
    @Override
    public long getVersion() {
      return delegate.getVersion();
    }
    @Override
    public long getGeneration() {
      return delegate.getGeneration();
    }
    @Override
    public boolean isDeleted() {
      return delegate.isDeleted();
    }
    @Override
    public long getTimestamp() throws IOException {
      return delegate.getTimestamp();
    }
    @Override
    public Map getUserData() throws IOException {
      return delegate.getUserData();
    }    
  }
  public IndexCommit getCommitPoint(Long version) {
    return solrVersionVsCommits.get(version);
  }
  public Map<Long, IndexCommit> getCommits() {
    return solrVersionVsCommits;
  }
  private void updateCommitPoints(List<IndexCommitWrapper> list) {
    Map<Long, IndexCommit> map = new ConcurrentHashMap<Long, IndexCommit>();
    for (IndexCommitWrapper wrapper : list) {
      if (!wrapper.isDeleted())
        map.put(wrapper.getVersion(), wrapper.delegate);
    }
    solrVersionVsCommits = map;
    latestCommit = ((list.get(list.size() - 1)).delegate);
  }
}
