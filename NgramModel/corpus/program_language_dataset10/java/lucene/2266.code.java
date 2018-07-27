package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
public class RemoveDuplicatesTokenFilterFactory extends BaseTokenFilterFactory {
  public RemoveDuplicatesTokenFilter create(TokenStream input) {
    return new RemoveDuplicatesTokenFilter(input);
  }
}
