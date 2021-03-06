package org.apache.lucene.search;
import java.io.IOException;
import java.util.Collection;
import java.util.Arrays;
import java.util.Comparator;
class ConjunctionScorer extends Scorer {
  private final Scorer[] scorers;
  private final float coord;
  private int lastDoc = -1;
  public ConjunctionScorer(Similarity similarity, Collection<Scorer> scorers) throws IOException {
    this(similarity, scorers.toArray(new Scorer[scorers.size()]));
  }
  public ConjunctionScorer(Similarity similarity, Scorer... scorers) throws IOException {
    super(similarity);
    this.scorers = scorers;
    coord = similarity.coord(scorers.length, scorers.length);
    for (int i = 0; i < scorers.length; i++) {
      if (scorers[i].nextDoc() == NO_MORE_DOCS) {
        lastDoc = NO_MORE_DOCS;
        return;
      }
    }
    Arrays.sort(scorers, new Comparator<Scorer>() {         
      public int compare(Scorer o1, Scorer o2) {
        return o1.docID() - o2.docID();
      }
    });
    if (doNext() == NO_MORE_DOCS) {
      lastDoc = NO_MORE_DOCS;
      return;
    }
    int end = scorers.length - 1;
    int max = end >> 1;
    for (int i = 0; i < max; i++) {
      Scorer tmp = scorers[i];
      int idx = end - i - 1;
      scorers[i] = scorers[idx];
      scorers[idx] = tmp;
    }
  }
  private int doNext() throws IOException {
    int first = 0;
    int doc = scorers[scorers.length - 1].docID();
    Scorer firstScorer;
    while ((firstScorer = scorers[first]).docID() < doc) {
      doc = firstScorer.advance(doc);
      first = first == scorers.length - 1 ? 0 : first + 1;
    }
    return doc;
  }
  @Override
  public int advance(int target) throws IOException {
    if (lastDoc == NO_MORE_DOCS) {
      return lastDoc;
    } else if (scorers[(scorers.length - 1)].docID() < target) {
      scorers[(scorers.length - 1)].advance(target);
    }
    return lastDoc = doNext();
  }
  @Override
  public int docID() {
    return lastDoc;
  }
  @Override
  public int nextDoc() throws IOException {
    if (lastDoc == NO_MORE_DOCS) {
      return lastDoc;
    } else if (lastDoc == -1) {
      return lastDoc = scorers[scorers.length - 1].docID();
    }
    scorers[(scorers.length - 1)].nextDoc();
    return lastDoc = doNext();
  }
  @Override
  public float score() throws IOException {
    float sum = 0.0f;
    for (int i = 0; i < scorers.length; i++) {
      sum += scorers[i].score();
    }
    return sum * coord;
  }
}
