package org.apache.lucene.store.instantiated;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BitVector;
public class InstantiatedIndexReader extends IndexReader {
  private final InstantiatedIndex index;
  public InstantiatedIndexReader(InstantiatedIndex index) {
    super();
    this.index = index;
  }
  @Override
  public boolean isOptimized() {
    return true;
  }
  @Override
  public long getVersion() {
    return index.getVersion();
  }
  @Override
  public Directory directory() {
    throw new UnsupportedOperationException();
  }
  @Override
  public boolean isCurrent() throws IOException {
    return true;
  }
  public InstantiatedIndex getIndex() {
    return index;
  }
  private BitVector uncommittedDeletedDocuments;
  private Map<String,List<NormUpdate>> uncommittedNormsByFieldNameAndDocumentNumber = null;
  private class NormUpdate {
    private int doc;
    private byte value;
    public NormUpdate(int doc, byte value) {
      this.doc = doc;
      this.value = value;
    }
  }
  @Override
  public int numDocs() {
    int numDocs = getIndex().getDocumentsByNumber().length;
    if (uncommittedDeletedDocuments != null) {
      numDocs -= uncommittedDeletedDocuments.count();
    }
    if (index.getDeletedDocuments() != null) {
      numDocs -= index.getDeletedDocuments().count();
    }
    return numDocs;
  }
  @Override
  public int maxDoc() {
    return getIndex().getDocumentsByNumber().length;
  }
  @Override
  public boolean hasDeletions() {
    return index.getDeletedDocuments() != null || uncommittedDeletedDocuments != null;
  }
  @Override
  public boolean isDeleted(int n) {
    return (index.getDeletedDocuments() != null && index.getDeletedDocuments().get(n))
        || (uncommittedDeletedDocuments != null && uncommittedDeletedDocuments.get(n));
  }
  @Override
  protected void doDelete(int docNum) throws IOException {
    if ((index.getDeletedDocuments() != null && index.getDeletedDocuments().get(docNum))
        || (uncommittedDeletedDocuments != null && uncommittedDeletedDocuments.get(docNum))) {
      return;
    }
    if (uncommittedDeletedDocuments == null) {
      uncommittedDeletedDocuments = new BitVector(maxDoc());
    }
    uncommittedDeletedDocuments.set(docNum);
  }
  @Override
  protected void doUndeleteAll() throws IOException {
    uncommittedDeletedDocuments = null;
  }
  @Override
  protected void doCommit(Map<String,String> commitUserData) throws IOException {
    if (uncommittedNormsByFieldNameAndDocumentNumber != null) {
      for (Map.Entry<String,List<NormUpdate>> e : uncommittedNormsByFieldNameAndDocumentNumber.entrySet()) {
        byte[] norms = getIndex().getNormsByFieldNameAndDocumentNumber().get(e.getKey());
        for (NormUpdate normUpdate : e.getValue()) {
          norms[normUpdate.doc] = normUpdate.value;
        }
      }
      uncommittedNormsByFieldNameAndDocumentNumber = null;
    }
    if (uncommittedDeletedDocuments != null) {
      if (index.getDeletedDocuments() == null) {
        index.setDeletedDocuments(uncommittedDeletedDocuments);
      } else {
        for (int d = 0; d< uncommittedDeletedDocuments.size(); d++) {
          if (uncommittedDeletedDocuments.get(d)) {
            index.getDeletedDocuments().set(d);
          }
        }
      }
      uncommittedDeletedDocuments = null;
    }
  }
  @Override
  protected void doClose() throws IOException {
  }
  @Override
  public Collection<String> getFieldNames(FieldOption fieldOption) {
    Set<String> fieldSet = new HashSet<String>();
    for (FieldSetting fi : index.getFieldSettings().values()) {
      if (fieldOption == IndexReader.FieldOption.ALL) {
        fieldSet.add(fi.fieldName);
      } else if (!fi.indexed && fieldOption == IndexReader.FieldOption.UNINDEXED) {
        fieldSet.add(fi.fieldName);
      } else if (fi.storePayloads && fieldOption == IndexReader.FieldOption.STORES_PAYLOADS) {
        fieldSet.add(fi.fieldName);
      } else if (fi.indexed && fieldOption == IndexReader.FieldOption.INDEXED) {
        fieldSet.add(fi.fieldName);
      } else if (fi.indexed && fi.storeTermVector == false && fieldOption == IndexReader.FieldOption.INDEXED_NO_TERMVECTOR) {
        fieldSet.add(fi.fieldName);
      } else if (fi.storeTermVector == true && fi.storePositionWithTermVector == false && fi.storeOffsetWithTermVector == false
          && fieldOption == IndexReader.FieldOption.TERMVECTOR) {
        fieldSet.add(fi.fieldName);
      } else if (fi.indexed && fi.storeTermVector && fieldOption == IndexReader.FieldOption.INDEXED_WITH_TERMVECTOR) {
        fieldSet.add(fi.fieldName);
      } else if (fi.storePositionWithTermVector && fi.storeOffsetWithTermVector == false
          && fieldOption == IndexReader.FieldOption.TERMVECTOR_WITH_POSITION) {
        fieldSet.add(fi.fieldName);
      } else if (fi.storeOffsetWithTermVector && fi.storePositionWithTermVector == false
          && fieldOption == IndexReader.FieldOption.TERMVECTOR_WITH_OFFSET) {
        fieldSet.add(fi.fieldName);
      } else if ((fi.storeOffsetWithTermVector && fi.storePositionWithTermVector)
          && fieldOption == IndexReader.FieldOption.TERMVECTOR_WITH_POSITION_OFFSET) {
        fieldSet.add(fi.fieldName);
      } 
    }
    return fieldSet;
  }
  @Override
  public Document document(int n, FieldSelector fieldSelector) throws CorruptIndexException, IOException {
    return document(n);
  }
  @Override
  public Document document(int n) throws IOException {
    return isDeleted(n) ? null : getIndex().getDocumentsByNumber()[n].getDocument();
  }
  @Override
  public byte[] norms(String field) throws IOException {
    byte[] norms = getIndex().getNormsByFieldNameAndDocumentNumber().get(field);
    if (norms == null) {
      return new byte[0]; 
    }
    if (uncommittedNormsByFieldNameAndDocumentNumber != null) {
      norms = norms.clone();
      List<NormUpdate> updated = uncommittedNormsByFieldNameAndDocumentNumber.get(field);
      if (updated != null) {
        for (NormUpdate normUpdate : updated) {
          norms[normUpdate.doc] = normUpdate.value;
        }
      }
    }
    return norms;
  }
  @Override
  public void norms(String field, byte[] bytes, int offset) throws IOException {
    byte[] norms = getIndex().getNormsByFieldNameAndDocumentNumber().get(field);
    if (norms == null) {
      return;
    }
    System.arraycopy(norms, 0, bytes, offset, norms.length);
  }
  @Override
  protected void doSetNorm(int doc, String field, byte value) throws IOException {
    if (uncommittedNormsByFieldNameAndDocumentNumber == null) {
      uncommittedNormsByFieldNameAndDocumentNumber = new HashMap<String,List<NormUpdate>>(getIndex().getNormsByFieldNameAndDocumentNumber().size());
    }
    List<NormUpdate> list = uncommittedNormsByFieldNameAndDocumentNumber.get(field);
    if (list == null) {
      list = new LinkedList<NormUpdate>();
      uncommittedNormsByFieldNameAndDocumentNumber.put(field, list);
    }
    list.add(new NormUpdate(doc, value));
  }
  @Override
  public int docFreq(Term t) throws IOException {
    InstantiatedTerm term = getIndex().findTerm(t);
    if (term == null) {
      return 0;
    } else {
      return term.getAssociatedDocuments().length;
    }
  }
  @Override
  public TermEnum terms() throws IOException {
    return new InstantiatedTermEnum(this);
  }
  @Override
  public TermEnum terms(Term t) throws IOException {
    InstantiatedTerm it = getIndex().findTerm(t);
    if (it != null) {
      return new InstantiatedTermEnum(this, it.getTermIndex());
    } else {
      int startPos = Arrays.binarySearch(index.getOrderedTerms(), t, InstantiatedTerm.termComparator);
      if (startPos < 0) {
        startPos = -1 - startPos;
      }
      return new InstantiatedTermEnum(this, startPos);
    }
  }
  @Override
  public TermDocs termDocs() throws IOException {
    return new InstantiatedTermDocs(this);
  }
  @Override
  public TermDocs termDocs(Term term) throws IOException {
    if (term == null) {
      return new InstantiatedAllTermDocs(this);
    } else {
      InstantiatedTermDocs termDocs = new InstantiatedTermDocs(this);
      termDocs.seek(term);
      return termDocs;
    }
  }
  @Override
  public TermPositions termPositions() throws IOException {
    return new InstantiatedTermPositions(this);
  }
  @Override
  public TermFreqVector[] getTermFreqVectors(int docNumber) throws IOException {
    InstantiatedDocument doc = getIndex().getDocumentsByNumber()[docNumber];
    if (doc.getVectorSpace() == null) {
      return null;
    }
    TermFreqVector[] ret = new TermFreqVector[doc.getVectorSpace().size()];
    Iterator<String> it = doc.getVectorSpace().keySet().iterator();
    for (int i = 0; i < ret.length; i++) {
      ret[i] = new InstantiatedTermPositionVector(getIndex().getDocumentsByNumber()[docNumber], it.next());
    }
    return ret;
  }
  @Override
  public TermFreqVector getTermFreqVector(int docNumber, String field) throws IOException {
    InstantiatedDocument doc = getIndex().getDocumentsByNumber()[docNumber];
    if (doc.getVectorSpace() == null || doc.getVectorSpace().get(field) == null) {
      return null;
    } else {
      return new InstantiatedTermPositionVector(doc, field);
    }
  }
  @Override
  public void getTermFreqVector(int docNumber, String field, TermVectorMapper mapper) throws IOException {
    InstantiatedDocument doc = getIndex().getDocumentsByNumber()[docNumber];
    if (doc.getVectorSpace() != null && doc.getVectorSpace().get(field) == null) {
      List<InstantiatedTermDocumentInformation> tv = doc.getVectorSpace().get(field);
      mapper.setExpectations(field, tv.size(), true, true);
      for (InstantiatedTermDocumentInformation tdi : tv) {
        mapper.map(tdi.getTerm().text(), tdi.getTermPositions().length, tdi.getTermOffsets(), tdi.getTermPositions());
      }
    }
  }
  @Override
  public void getTermFreqVector(int docNumber, TermVectorMapper mapper) throws IOException {
    InstantiatedDocument doc = getIndex().getDocumentsByNumber()[docNumber];
    for (Map.Entry<String, List<InstantiatedTermDocumentInformation>> e : doc.getVectorSpace().entrySet()) {
      mapper.setExpectations(e.getKey(), e.getValue().size(), true, true);
      for (InstantiatedTermDocumentInformation tdi : e.getValue()) {
        mapper.map(tdi.getTerm().text(), tdi.getTermPositions().length, tdi.getTermOffsets(), tdi.getTermPositions());
      }
    }
  }
}
