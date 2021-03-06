package org.apache.lucene.analysis.br;
import java.io.IOException;
import java.util.Set;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter; 
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public final class BrazilianStemFilter extends TokenFilter {
  private BrazilianStemmer stemmer = null;
  private Set<?> exclusions = null;
  private final TermAttribute termAtt;
  private final KeywordAttribute keywordAttr;
  public BrazilianStemFilter(TokenStream in) {
    super(in);
    stemmer = new BrazilianStemmer();
    termAtt = addAttribute(TermAttribute.class);
    keywordAttr = addAttribute(KeywordAttribute.class);
  }
  @Deprecated
  public BrazilianStemFilter(TokenStream in, Set<?> exclusiontable) {
    this(in);
    this.exclusions = exclusiontable;
  }
  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      final String term = termAtt.term();
      if (!keywordAttr.isKeyword() && (exclusions == null || !exclusions.contains(term))) {
        final String s = stemmer.stem(term);
        if ((s != null) && !s.equals(term))
          termAtt.setTermBuffer(s);
      }
      return true;
    } else {
      return false;
    }
  }
}
