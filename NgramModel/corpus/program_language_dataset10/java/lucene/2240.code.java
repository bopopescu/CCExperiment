package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.in.IndicNormalizationFilter;
public class IndicNormalizationFilterFactory extends BaseTokenFilterFactory {
  public TokenStream create(TokenStream input) {
    return new IndicNormalizationFilter(input);
  }
}
