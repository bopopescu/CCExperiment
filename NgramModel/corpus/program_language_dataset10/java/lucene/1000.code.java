package org.apache.lucene.search.highlight;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
public class QueryTermScorer implements Scorer {
  TextFragment currentTextFragment = null;
  HashSet<String> uniqueTermsInFragment;
  float totalScore = 0;
  float maxTermWeight = 0;
  private HashMap<String,WeightedTerm> termsToFind;
  private TermAttribute termAtt;
  public QueryTermScorer(Query query) {
    this(QueryTermExtractor.getTerms(query));
  }
  public QueryTermScorer(Query query, String fieldName) {
    this(QueryTermExtractor.getTerms(query, false, fieldName));
  }
  public QueryTermScorer(Query query, IndexReader reader, String fieldName) {
    this(QueryTermExtractor.getIdfWeightedTerms(query, reader, fieldName));
  }
  public QueryTermScorer(WeightedTerm[] weightedTerms) {
    termsToFind = new HashMap<String,WeightedTerm>();
    for (int i = 0; i < weightedTerms.length; i++) {
      WeightedTerm existingTerm = termsToFind
          .get(weightedTerms[i].term);
      if ((existingTerm == null)
          || (existingTerm.weight < weightedTerms[i].weight)) {
        termsToFind.put(weightedTerms[i].term, weightedTerms[i]);
        maxTermWeight = Math.max(maxTermWeight, weightedTerms[i].getWeight());
      }
    }
  }
  public TokenStream init(TokenStream tokenStream) {
    termAtt = tokenStream.addAttribute(TermAttribute.class);
    return null;
  }
  public void startFragment(TextFragment newFragment) {
    uniqueTermsInFragment = new HashSet<String>();
    currentTextFragment = newFragment;
    totalScore = 0;
  }
  public float getTokenScore() {
    String termText = termAtt.term();
    WeightedTerm queryTerm = termsToFind.get(termText);
    if (queryTerm == null) {
      return 0;
    }
    if (!uniqueTermsInFragment.contains(termText)) {
      totalScore += queryTerm.getWeight();
      uniqueTermsInFragment.add(termText);
    }
    return queryTerm.getWeight();
  }
  public float getFragmentScore() {
    return totalScore;
  }
  public void allFragmentsProcessed() {
  }
  public float getMaxTermWeight() {
    return maxTermWeight;
  }
}
