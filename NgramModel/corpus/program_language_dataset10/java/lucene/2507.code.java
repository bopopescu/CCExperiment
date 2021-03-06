package org.apache.solr.search;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
public class SolrIndexReader extends FilterIndexReader {
  private final SolrIndexReader[] subReaders;
  private final SolrIndexReader[] leafReaders;
  private int[] leafOffsets;
  private final SolrIndexReader parent;
  private final int base; 
  private static int[] zeroIntArray = new int[]{0};
  SolrIndexSearcher searcher;
  private SolrReaderInfo info;
  public SolrIndexReader(IndexReader in, SolrIndexReader parent, int base) {
    super(in);
    assert(!(in instanceof SolrIndexReader));
    this.parent = parent;
    this.base = base;
    IndexReader subs[] = in.getSequentialSubReaders();
    if (subs != null) {
      subReaders = new SolrIndexReader[subs.length]; 
      int numLeaves = subs.length;
      leafOffsets = new int[numLeaves];
      int b=0;
      for (int i=0; i<subReaders.length; i++) {
        SolrIndexReader sir = subReaders[i] = new SolrIndexReader(subs[i], this, b);
        leafOffsets[i] = b;
        b += sir.maxDoc();
        IndexReader subLeaves[] = sir.leafReaders;
        numLeaves += subLeaves.length - 1;  
      }
      leafReaders = getLeaves(numLeaves);
    } else {
      subReaders = null;
      leafReaders = new SolrIndexReader[]{this};
      leafOffsets = zeroIntArray;
    }
  }
  private SolrIndexReader[] getLeaves(int numLeaves) {
    if (subReaders==null || numLeaves == subReaders.length) return subReaders;
    SolrIndexReader[] leaves = new SolrIndexReader[numLeaves];
    leafOffsets = new int[numLeaves];
    int i=0;
    int b = 0;
    for (SolrIndexReader sir : subReaders) {
      SolrIndexReader subLeaves[] = sir.leafReaders;
      if (subLeaves == null) {
        leafOffsets[i] = b;
        b += sir.maxDoc();
        leaves[i++] = sir;
      } else {
        for (SolrIndexReader subLeaf : subLeaves) {
          leafOffsets[i] = b;
          b += subLeaf.maxDoc();
          leaves[i++] = subLeaf;
        }
      }
    }
    assert(i == numLeaves && b == maxDoc());
    return leaves;
  }
  public SolrIndexReader[] getLeafReaders() {
    return leafReaders;
  }
  public int[] getLeafOffsets() {
    return leafOffsets;
  }
  public static int readerIndex(int doc, int[] offsets) {    
    int high = offsets.length - 1;
    if (high <= 0 || doc < offsets[1]) return 0;
    int low = 1;
    while (high >= low) {
      int mid = (low + high) >>> 1;
      int offset = offsets[mid];
      if (doc < offset)
        high = mid - 1;
      else if (doc > offset) {
        low = mid + 1;
      }
      else {
        return mid;
      }
    }
    return high;
  }
  static String shortName(Object o) {
    return o.getClass().getSimpleName()+ "@" + Integer.toHexString(o.hashCode());
  }
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("SolrIndexReader{this=").append(Integer.toHexString(this.hashCode()));
    sb.append(",r=").append(shortName(in));
    sb.append(",refCnt=").append(getRefCount());
    sb.append(",segments=");
    sb.append(subReaders == null ? 1 : subReaders.length);
    if (parent != null) {
      sb.append(",parent=").append(parent.toString());
    }
    sb.append('}');
    return sb.toString();
  }
  static void setSearcher(SolrIndexReader sr, SolrIndexSearcher searcher) {
    sr.searcher = searcher;
    SolrIndexReader[] readers = sr.getSequentialSubReaders();
    if (readers == null) return;
    for (SolrIndexReader r : readers) {
      setSearcher(r, searcher);
    }
  }
   private static void buildInfoMap(SolrIndexReader other, HashMap<IndexReader, SolrReaderInfo> map) {
     if (other == null) return;
     map.put(other.getWrappedReader(), other.info);
     SolrIndexReader[] readers = other.getSequentialSubReaders();
     if (readers == null) return;
     for (SolrIndexReader r : readers) {
       buildInfoMap(r, map);
     }     
   }
   private static void setInfo(SolrIndexReader target, HashMap<IndexReader, SolrReaderInfo> map) {
     SolrReaderInfo info = map.get(target.getWrappedReader());
     if (info == null) info = new SolrReaderInfo(target.getWrappedReader());
     target.info = info;
     SolrIndexReader[] readers = target.getSequentialSubReaders();
     if (readers == null) return;
     for (SolrIndexReader r : readers) {
       setInfo(r, map);
     }     
   }
   public void associateInfo(SolrIndexReader source) {
     HashMap<IndexReader, SolrReaderInfo> map = new HashMap<IndexReader, SolrReaderInfo>();
     buildInfoMap(source, map);
     setInfo(this, map);
   }
  public IndexReader getWrappedReader() {
    return in;
  }
  public SolrIndexReader getParent() {
    return parent;
  }
  public int getBase() {
    return base;
  }
  @Override
  public Directory directory() {
    return in.directory();
  }
  @Override
  public TermFreqVector[] getTermFreqVectors(int docNumber) throws IOException {
    return in.getTermFreqVectors(docNumber);
  }
  @Override
  public TermFreqVector getTermFreqVector(int docNumber, String field)
          throws IOException {
    return in.getTermFreqVector(docNumber, field);
  }
  @Override
  public void getTermFreqVector(int docNumber, String field, TermVectorMapper mapper) throws IOException {
    in.getTermFreqVector(docNumber, field, mapper);
  }
  @Override
  public void getTermFreqVector(int docNumber, TermVectorMapper mapper) throws IOException {
    in.getTermFreqVector(docNumber, mapper);
  }
  @Override
  public int numDocs() {
    return in.numDocs();
  }
  @Override
  public int maxDoc() {
    return in.maxDoc();
  }
  @Override
  public Document document(int n, FieldSelector fieldSelector) throws CorruptIndexException, IOException {
    return in.document(n, fieldSelector);
  }
  @Override
  public boolean isDeleted(int n) {
    return in.isDeleted(n);
  }
  @Override
  public boolean hasDeletions() {
    return in.hasDeletions();
  }
  @Override
  protected void doUndeleteAll() throws CorruptIndexException, IOException {in.undeleteAll();}
  @Override
  public boolean hasNorms(String field) throws IOException {
    return in.hasNorms(field);
  }
  @Override
  public byte[] norms(String f) throws IOException {
    return in.norms(f);
  }
  @Override
  public void norms(String f, byte[] bytes, int offset) throws IOException {
    in.norms(f, bytes, offset);
  }
  @Override
  protected void doSetNorm(int d, String f, byte b) throws CorruptIndexException, IOException {
    in.setNorm(d, f, b);
  }
  @Override
  public TermEnum terms() throws IOException {
    return in.terms();
  }
  @Override
  public TermEnum terms(Term t) throws IOException {
    return in.terms(t);
  }
  @Override
  public int docFreq(Term t) throws IOException {
    ensureOpen();
    return in.docFreq(t);
  }
  @Override
  public TermDocs termDocs() throws IOException {
    ensureOpen();
    return in.termDocs();
  }
  @Override
  public TermDocs termDocs(Term term) throws IOException {
    ensureOpen();
    return in.termDocs(term);
  }
  @Override
  public TermPositions termPositions() throws IOException {
    ensureOpen();
    return in.termPositions();
  }
  @Override
  protected void doDelete(int n) throws  CorruptIndexException, IOException { in.deleteDocument(n); }
  @Override
  protected void doClose() throws IOException {
    in.close();
  }
  @Override
  public Collection getFieldNames(IndexReader.FieldOption fieldNames) {
    return in.getFieldNames(fieldNames);
  }
  @Override
  public long getVersion() {
    return in.getVersion();
  }
  @Override
  public boolean isCurrent() throws CorruptIndexException, IOException {
    return in.isCurrent();
  }
  @Override
  public boolean isOptimized() {
    return in.isOptimized();
  }
  @Override
  public SolrIndexReader[] getSequentialSubReaders() {
    return subReaders;
  }
  @Override
  public int hashCode() {
    return in.hashCode();
  }
  @Override
  public boolean equals(Object o) {
    if (o instanceof SolrIndexReader) {
      o = ((SolrIndexReader)o).in;
    }
    return in.equals(o);
  }
  @Override
  public int getRefCount() {
    return in.getRefCount();
  }
  @Override
  public IndexReader reopen(IndexCommit commit) throws CorruptIndexException, IOException {
    return in.reopen(commit);
  }
  @Override
  public Object clone() {
    return super.clone();
  }
  @Override
  public IndexReader clone(boolean openReadOnly) throws CorruptIndexException, IOException {
    return super.clone(openReadOnly);
  }
  @Override
  public Map getCommitUserData() {
    return in.getCommitUserData();
  }
  @Override
  public long getUniqueTermCount() throws IOException {
    return super.getUniqueTermCount();
  }
  @Override
  public SolrIndexReader reopen(boolean openReadOnly) throws IOException {
    IndexReader r = in.reopen(openReadOnly);
    if (r == in) {
      return this;
    }
    SolrIndexReader sr = new SolrIndexReader(r, null, 0);
    sr.associateInfo(this);
    return sr;
  }
  @Override
  public SolrIndexReader reopen() throws CorruptIndexException, IOException {
    return reopen(true);
  }
  @Override
  public void decRef() throws IOException {
    in.decRef();
  }
  @Override
  public void deleteDocument(int docNum) throws StaleReaderException, CorruptIndexException, LockObtainFailedException, IOException {
    in.deleteDocument(docNum);
  }
  @Override
  public int deleteDocuments(Term term) throws StaleReaderException, CorruptIndexException, LockObtainFailedException, IOException {
    return in.deleteDocuments(term);
  }
  @Override
  public Document document(int n) throws CorruptIndexException, IOException {
    return in.document(n);
  }
  @Override
  public IndexCommit getIndexCommit() throws IOException {
    return in.getIndexCommit();
  }
  @Override
  public void incRef() {
    in.incRef();
  }
  @Override
  public int numDeletedDocs() {
    return in.numDeletedDocs();
  }
  @Override
  public void setNorm(int doc, String field, byte value) throws StaleReaderException, CorruptIndexException, LockObtainFailedException, IOException {
    in.setNorm(doc, field, value);
  }
  @Override
  public void setNorm(int doc, String field, float value) throws StaleReaderException, CorruptIndexException, LockObtainFailedException, IOException {
    in.setNorm(doc, field, value);
  }
  @Override
  public TermPositions termPositions(Term term) throws IOException {
    return in.termPositions(term);
  }
  @Override
  public void undeleteAll() throws StaleReaderException, CorruptIndexException, LockObtainFailedException, IOException {
    in.undeleteAll();
  }
  @Override
  public Object getFieldCacheKey() {
    return in.getFieldCacheKey();
  }
  @Override
  public int getTermInfosIndexDivisor() {
    return in.getTermInfosIndexDivisor();
  }
}
class SolrReaderInfo {
  private final IndexReader reader;
  public SolrReaderInfo(IndexReader reader) {
    this.reader = reader;
  }
  public IndexReader getReader() { return reader; }
}