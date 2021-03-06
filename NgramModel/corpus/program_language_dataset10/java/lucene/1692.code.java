package org.apache.lucene.search;
import java.io.IOException;
class ReqOptSumScorer extends Scorer {
  private Scorer reqScorer;
  private Scorer optScorer;
  public ReqOptSumScorer(
      Scorer reqScorer,
      Scorer optScorer)
  {
    super(null); 
    this.reqScorer = reqScorer;
    this.optScorer = optScorer;
  }
  @Override
  public int nextDoc() throws IOException {
    return reqScorer.nextDoc();
  }
  @Override
  public int advance(int target) throws IOException {
    return reqScorer.advance(target);
  }
  @Override
  public int docID() {
    return reqScorer.docID();
  }
  @Override
  public float score() throws IOException {
    int curDoc = reqScorer.docID();
    float reqScore = reqScorer.score();
    if (optScorer == null) {
      return reqScore;
    }
    int optScorerDoc = optScorer.docID();
    if (optScorerDoc < curDoc && (optScorerDoc = optScorer.advance(curDoc)) == NO_MORE_DOCS) {
      optScorer = null;
      return reqScore;
    }
    return optScorerDoc == curDoc ? reqScore + optScorer.score() : reqScore;
  }
}
