package org.apache.lucene.store.instantiated;
import org.apache.lucene.index.TermVectorOffsetInfo;
import java.io.Serializable;
import java.util.Comparator;
public class InstantiatedTermDocumentInformation
    implements Serializable {
  private static final long serialVersionUID = 1l;
  public static final Comparator<InstantiatedTermDocumentInformation> termComparator = new Comparator<InstantiatedTermDocumentInformation>() {
    public int compare(InstantiatedTermDocumentInformation instantiatedTermDocumentInformation, InstantiatedTermDocumentInformation instantiatedTermDocumentInformation1) {
      return instantiatedTermDocumentInformation.getTerm().getTerm().compareTo(instantiatedTermDocumentInformation1.getTerm().getTerm());
    }
  };
  public static final Comparator<InstantiatedTermDocumentInformation> documentNumberComparator = new Comparator<InstantiatedTermDocumentInformation>() {
    public int compare(InstantiatedTermDocumentInformation instantiatedTermDocumentInformation, InstantiatedTermDocumentInformation instantiatedTermDocumentInformation1) {
      return instantiatedTermDocumentInformation.getDocument().getDocumentNumber().compareTo(instantiatedTermDocumentInformation1.getDocument().getDocumentNumber());
    }
  };
  public static final Comparator doumentNumberIntegerComparator = new Comparator() {
    public int compare(Object o1, Object o2) {
      InstantiatedTermDocumentInformation di = (InstantiatedTermDocumentInformation) o1;
      Integer i = (Integer) o2;
      return di.getDocument().getDocumentNumber().compareTo(i);
    }
  };
  private byte[][] payloads;
  private int[] termPositions;
  private InstantiatedTerm term;
  private InstantiatedDocument document;
  private TermVectorOffsetInfo[] termOffsets;
  public InstantiatedTermDocumentInformation(InstantiatedTerm term, InstantiatedDocument document, int[] termPositions, byte[][] payloads) {
    this.term = term;
    this.document = document;
    this.termPositions = termPositions;
    this.payloads = payloads;
  }
  public int[] getTermPositions() {
    return termPositions;
  }
  public byte[][] getPayloads() {
    return payloads;
  }
  public InstantiatedDocument getDocument() {
    return document;
  }
  public InstantiatedTerm getTerm() {
    return term;
  }
  void setTermPositions(int[] termPositions) {
    this.termPositions = termPositions;
  }
  void setTerm(InstantiatedTerm term) {
    this.term = term;
  }
  void setDocument(InstantiatedDocument document) {
    this.document = document;
  }
  public TermVectorOffsetInfo[] getTermOffsets() {
    return termOffsets;
  }
  void setTermOffsets(TermVectorOffsetInfo[] termOffsets) {
    this.termOffsets = termOffsets;
  }
}
