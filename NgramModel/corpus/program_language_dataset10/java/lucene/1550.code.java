package org.apache.lucene.index;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DocumentsWriter.IndexingChain;
import org.apache.lucene.index.IndexWriter.IndexReaderWarmer;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.util.Version;
public final class IndexWriterConfig implements Cloneable {
  public static final int UNLIMITED_FIELD_LENGTH = Integer.MAX_VALUE;
  public static enum OpenMode { CREATE, APPEND, CREATE_OR_APPEND }
  public static final int DEFAULT_TERM_INDEX_INTERVAL = 128;
  public final static int DISABLE_AUTO_FLUSH = -1;
  public final static int DEFAULT_MAX_BUFFERED_DELETE_TERMS = DISABLE_AUTO_FLUSH;
  public final static int DEFAULT_MAX_BUFFERED_DOCS = DISABLE_AUTO_FLUSH;
  public final static double DEFAULT_RAM_BUFFER_SIZE_MB = 16.0;
  public static long WRITE_LOCK_TIMEOUT = 1000;
  public final static int DEFAULT_MAX_THREAD_STATES = 8;
  public static void setDefaultWriteLockTimeout(long writeLockTimeout) {
    WRITE_LOCK_TIMEOUT = writeLockTimeout;
  }
  public static long getDefaultWriteLockTimeout() {
    return WRITE_LOCK_TIMEOUT;
  }
  private Analyzer analyzer;
  private IndexDeletionPolicy delPolicy;
  private IndexCommit commit;
  private OpenMode openMode;
  private int maxFieldLength;
  private Similarity similarity;
  private int termIndexInterval;
  private MergeScheduler mergeScheduler;
  private long writeLockTimeout;
  private int maxBufferedDeleteTerms;
  private double ramBufferSizeMB;
  private int maxBufferedDocs;
  private IndexingChain indexingChain;
  private IndexReaderWarmer mergedSegmentWarmer;
  private int maxThreadStates;
  private Version matchVersion;
  public IndexWriterConfig(Version matchVersion, Analyzer analyzer) {
    this.matchVersion = matchVersion;
    this.analyzer = analyzer;
    delPolicy = new KeepOnlyLastCommitDeletionPolicy();
    commit = null;
    openMode = OpenMode.CREATE_OR_APPEND;
    maxFieldLength = UNLIMITED_FIELD_LENGTH;
    similarity = Similarity.getDefault();
    termIndexInterval = DEFAULT_TERM_INDEX_INTERVAL;
    mergeScheduler = new ConcurrentMergeScheduler();
    writeLockTimeout = WRITE_LOCK_TIMEOUT;
    maxBufferedDeleteTerms = DEFAULT_MAX_BUFFERED_DELETE_TERMS;
    ramBufferSizeMB = DEFAULT_RAM_BUFFER_SIZE_MB;
    maxBufferedDocs = DEFAULT_MAX_BUFFERED_DOCS;
    indexingChain = DocumentsWriter.defaultIndexingChain;
    mergedSegmentWarmer = null;
    maxThreadStates = DEFAULT_MAX_THREAD_STATES;
  }
  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
  public Analyzer getAnalyzer() {
    return analyzer;
  }
  public IndexWriterConfig setOpenMode(OpenMode openMode) {
    this.openMode = openMode;
    return this;
  }
  public OpenMode getOpenMode() {
    return openMode;
  }
  public IndexWriterConfig setIndexDeletionPolicy(IndexDeletionPolicy delPolicy) {
    this.delPolicy = delPolicy == null ? new KeepOnlyLastCommitDeletionPolicy() : delPolicy;
    return this;
  }
  public IndexDeletionPolicy getIndexDeletionPolicy() {
    return delPolicy;
  }
  public IndexWriterConfig setMaxFieldLength(int maxFieldLength) {
    this.maxFieldLength = maxFieldLength;
    return this;
  }
  public int getMaxFieldLength() {
    return maxFieldLength;
  }
  public IndexWriterConfig setIndexCommit(IndexCommit commit) {
    this.commit = commit;
    return this;
  }
  public IndexCommit getIndexCommit() {
    return commit;
  }
  public IndexWriterConfig setSimilarity(Similarity similarity) {
    this.similarity = similarity == null ? Similarity.getDefault() : similarity;
    return this;
  }
  public Similarity getSimilarity() {
    return similarity;
  }
  public IndexWriterConfig setTermIndexInterval(int interval) {
    this.termIndexInterval = interval;
    return this;
  }
  public int getTermIndexInterval() {
    return termIndexInterval;
  }
  public IndexWriterConfig setMergeScheduler(MergeScheduler mergeScheduler) {
    this.mergeScheduler = mergeScheduler == null ? new ConcurrentMergeScheduler() : mergeScheduler;
    return this;
  }
  public MergeScheduler getMergeScheduler() {
    return mergeScheduler;
  }
  public IndexWriterConfig setWriteLockTimeout(long writeLockTimeout) {
    this.writeLockTimeout = writeLockTimeout;
    return this;
  }
  public long getWriteLockTimeout() {
    return writeLockTimeout;
  }
  public IndexWriterConfig setMaxBufferedDeleteTerms(int maxBufferedDeleteTerms) {
    if (maxBufferedDeleteTerms != DISABLE_AUTO_FLUSH
        && maxBufferedDeleteTerms < 1)
      throw new IllegalArgumentException(
          "maxBufferedDeleteTerms must at least be 1 when enabled");
    this.maxBufferedDeleteTerms = maxBufferedDeleteTerms;
    return this;
  }
  public int getMaxBufferedDeleteTerms() {
    return maxBufferedDeleteTerms;
  }
  public IndexWriterConfig setRAMBufferSizeMB(double ramBufferSizeMB) {
    if (ramBufferSizeMB > 2048.0) {
      throw new IllegalArgumentException("ramBufferSize " + ramBufferSizeMB
          + " is too large; should be comfortably less than 2048");
    }
    if (ramBufferSizeMB != DISABLE_AUTO_FLUSH && ramBufferSizeMB <= 0.0)
      throw new IllegalArgumentException(
          "ramBufferSize should be > 0.0 MB when enabled");
    if (ramBufferSizeMB == DISABLE_AUTO_FLUSH && maxBufferedDocs == DISABLE_AUTO_FLUSH)
      throw new IllegalArgumentException(
          "at least one of ramBufferSize and maxBufferedDocs must be enabled");
    this.ramBufferSizeMB = ramBufferSizeMB;
    return this;
  }
  public double getRAMBufferSizeMB() {
    return ramBufferSizeMB;
  }
  public IndexWriterConfig setMaxBufferedDocs(int maxBufferedDocs) {
    if (maxBufferedDocs != DISABLE_AUTO_FLUSH && maxBufferedDocs < 2)
      throw new IllegalArgumentException(
          "maxBufferedDocs must at least be 2 when enabled");
    if (maxBufferedDocs == DISABLE_AUTO_FLUSH
        && ramBufferSizeMB == DISABLE_AUTO_FLUSH)
      throw new IllegalArgumentException(
          "at least one of ramBufferSize and maxBufferedDocs must be enabled");
    this.maxBufferedDocs = maxBufferedDocs;
    return this;
  }
  public int getMaxBufferedDocs() {
    return maxBufferedDocs;
  }
  public IndexWriterConfig setMergedSegmentWarmer(IndexReaderWarmer mergeSegmentWarmer) {
    this.mergedSegmentWarmer = mergeSegmentWarmer;
    return this;
  }
  public IndexReaderWarmer getMergedSegmentWarmer() {
    return mergedSegmentWarmer;
  }
  public IndexWriterConfig setMaxThreadStates(int maxThreadStates) {
    this.maxThreadStates = maxThreadStates;
    return this;
  }
  public int getMaxThreadStates() {
    return maxThreadStates;
  }
  IndexWriterConfig setIndexingChain(IndexingChain indexingChain) {
    this.indexingChain = indexingChain == null ? DocumentsWriter.defaultIndexingChain : indexingChain;
    return this;
  }
  IndexingChain getIndexingChain() {
    return indexingChain;
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("matchVersion=").append(matchVersion).append("\n");
    sb.append("analyzer=").append(analyzer.getClass().getName()).append("\n");
    sb.append("delPolicy=").append(delPolicy.getClass().getName()).append("\n");
    sb.append("commit=").append(commit == null ? "null" : commit.getClass().getName()).append("\n");
    sb.append("openMode=").append(openMode).append("\n");
    sb.append("maxFieldLength=").append(maxFieldLength).append("\n");
    sb.append("similarity=").append(similarity.getClass().getName()).append("\n");
    sb.append("termIndexInterval=").append(termIndexInterval).append("\n");
    sb.append("mergeScheduler=").append(mergeScheduler.getClass().getName()).append("\n");
    sb.append("default WRITE_LOCK_TIMEOUT=").append(WRITE_LOCK_TIMEOUT).append("\n");
    sb.append("writeLockTimeout=").append(writeLockTimeout).append("\n");
    sb.append("maxBufferedDeleteTerms=").append(maxBufferedDeleteTerms).append("\n");
    sb.append("ramBufferSizeMB=").append(ramBufferSizeMB).append("\n");
    sb.append("maxBufferedDocs=").append(maxBufferedDocs).append("\n");
    sb.append("mergedSegmentWarmer=").append(mergedSegmentWarmer).append("\n");
    sb.append("maxThreadStates=").append(maxThreadStates).append("\n");
    return sb.toString();
  }
}
