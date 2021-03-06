package org.apache.lucene.analysis.de;
import java.io.IOException;
import java.util.Set;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public final class GermanStemFilter extends TokenFilter
{
    private GermanStemmer stemmer = null;
    private Set<?> exclusionSet = null;
    private final TermAttribute termAtt;
    private final KeywordAttribute keywordAttr;
    public GermanStemFilter( TokenStream in )
    {
      super(in);
      stemmer = new GermanStemmer();
      termAtt = addAttribute(TermAttribute.class);
      keywordAttr = addAttribute(KeywordAttribute.class);
    }
    @Deprecated
    public GermanStemFilter( TokenStream in, Set<?> exclusionSet )
    {
      this( in );
      this.exclusionSet = exclusionSet;
    }
    @Override
    public boolean incrementToken() throws IOException {
      if (input.incrementToken()) {
        String term = termAtt.term();
        if (!keywordAttr.isKeyword() && (exclusionSet == null || !exclusionSet.contains(term))) {
          String s = stemmer.stem(term);
          if ((s != null) && !s.equals(term))
            termAtt.setTermBuffer(s);
        }
        return true;
      } else {
        return false;
      }
    }
    public void setStemmer( GermanStemmer stemmer )
    {
      if ( stemmer != null ) {
        this.stemmer = stemmer;
      }
    }
    @Deprecated
    public void setExclusionSet( Set<?> exclusionSet )
    {
      this.exclusionSet = exclusionSet;
    }
}
