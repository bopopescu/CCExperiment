package org.apache.lucene.store.instantiated;
import org.apache.lucene.index.TermFreqVector;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
public class InstantiatedTermFreqVector
    implements TermFreqVector, Serializable {
  private static final long serialVersionUID = 1l;
  private final List<InstantiatedTermDocumentInformation> termDocumentInformations;
  private final String field;
  private final String terms[];
  private final int termFrequencies[];
  public InstantiatedTermFreqVector(InstantiatedDocument document, String field) {
    this.field = field;
    termDocumentInformations = document.getVectorSpace().get(field);
    terms = new String[termDocumentInformations.size()];
    termFrequencies = new int[termDocumentInformations.size()];
    for (int i = 0; i < termDocumentInformations.size(); i++) {
      InstantiatedTermDocumentInformation termDocumentInformation = termDocumentInformations.get(i);
      terms[i] = termDocumentInformation.getTerm().text();
      termFrequencies[i] = termDocumentInformation.getTermPositions().length;
    }
  }
  public String getField() {
    return field;
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('{');
    sb.append(field).append(": ");
    if (terms != null) {
      for (int i = 0; i < terms.length; i++) {
        if (i > 0) sb.append(", ");
        sb.append(terms[i]).append('/').append(termFrequencies[i]);
      }
    }
    sb.append('}');
    return sb.toString();
  }
  public int size() {
    return terms == null ? 0 : terms.length;
  }
  public String[] getTerms() {
    return terms;
  }
  public int[] getTermFrequencies() {
    return termFrequencies;
  }
  public int indexOf(String termText) {
    if (terms == null)
      return -1;
    int res = Arrays.binarySearch(terms, termText);
    return res >= 0 ? res : -1;
  }
  public int[] indexesOf(String[] termNumbers, int start, int len) {
    int res[] = new int[len];
    for (int i = 0; i < len; i++) {
      res[i] = indexOf(termNumbers[start + i]);
    }
    return res;
  }
  public List<InstantiatedTermDocumentInformation> getTermDocumentInformations() {
    return termDocumentInformations;
  }
}
