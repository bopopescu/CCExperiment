package org.apache.lucene.store.instantiated;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
public class InstantiatedTermDocs
    implements TermDocs {
  private final InstantiatedIndexReader reader;
  public InstantiatedTermDocs(InstantiatedIndexReader reader) {
    this.reader = reader;
  }
  private int currentDocumentIndex;
  protected InstantiatedTermDocumentInformation currentDocumentInformation;
  protected InstantiatedTerm currentTerm;
  public void seek(Term term) {
    currentTerm = reader.getIndex().findTerm(term);
    currentDocumentIndex = -1;
  }
  public void seek(org.apache.lucene.index.TermEnum termEnum) {
    seek(termEnum.term());
  }
  public int doc() {
    return currentDocumentInformation.getDocument().getDocumentNumber();
  }
  public int freq() {
    return currentDocumentInformation.getTermPositions().length;
  }
  public boolean next() {
    if (currentTerm != null) {
      currentDocumentIndex++;
      if (currentDocumentIndex < currentTerm.getAssociatedDocuments().length) {
        currentDocumentInformation = currentTerm.getAssociatedDocuments()[currentDocumentIndex];
        if (reader.isDeleted(currentDocumentInformation.getDocument().getDocumentNumber())) {
          return next();
        } else {
          return true;
        }
      } else {
        currentDocumentIndex = currentTerm.getAssociatedDocuments().length -1;
      }
    }
    return false;
  }
  public int read(int[] docs, int[] freqs) {
    int i;
    for (i = 0; i < docs.length; i++) {
      if (!next()) {
        break;
      }
      docs[i] = doc();
      freqs[i] = freq();
    }
    return i;
  }
  public boolean skipTo(int target) {
    if (currentTerm == null) {
      return false;
    }
    if (currentDocumentIndex >= target) {
      return next();
    }
    int startOffset = currentDocumentIndex >= 0 ? currentDocumentIndex : 0;
    int pos = currentTerm.seekCeilingDocumentInformationIndex(target, startOffset);
    if (pos == -1) {
      currentDocumentIndex = currentTerm.getAssociatedDocuments().length -1;
      return false;
    }
    currentDocumentInformation = currentTerm.getAssociatedDocuments()[pos];
    currentDocumentIndex = pos;
    if (reader.hasDeletions() && reader.isDeleted(currentDocumentInformation.getDocument().getDocumentNumber())) {
      return next();
    } else {
      return true;
    }
  }
  public void close() {
  }
}
