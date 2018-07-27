package org.apache.lucene.search.highlight;
import org.apache.lucene.analysis.TokenStream;
public interface Fragmenter {
  public void start(String originalText, TokenStream tokenStream);
  public boolean isNewFragment();
}
