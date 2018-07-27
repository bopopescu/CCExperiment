package org.apache.lucene.search;
public class TestComplexExplanationsOfNonMatches
  extends TestComplexExplanations {
  @Override
  public void qtest(Query q, int[] expDocNrs) throws Exception {
    CheckHits.checkNoMatchExplanations(q, FIELD, searcher, expDocNrs);
  }
}
