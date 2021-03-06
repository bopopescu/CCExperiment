package org.apache.lucene.analysis.fr;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
@Deprecated
public final class FrenchStemFilter extends TokenFilter {
	private FrenchStemmer stemmer = null;
	private Set<?> exclusions = null;
	private final TermAttribute termAtt;
  private final KeywordAttribute keywordAttr;
	public FrenchStemFilter( TokenStream in ) {
          super(in);
		stemmer = new FrenchStemmer();
		termAtt = addAttribute(TermAttribute.class);
    keywordAttr = addAttribute(KeywordAttribute.class);
	}
	@Deprecated 
	public FrenchStemFilter( TokenStream in, Set<?> exclusiontable ) {
		this( in );
		exclusions = exclusiontable;
	}
	@Override
	public boolean incrementToken() throws IOException {
	  if (input.incrementToken()) {
	    String term = termAtt.term();
	    if ( !keywordAttr.isKeyword() && (exclusions == null || !exclusions.contains( term )) ) {
	      String s = stemmer.stem( term );
	      if ((s != null) && !s.equals( term ) )
	        termAtt.setTermBuffer(s);
	    }
	    return true;
	  } else {
	    return false;
	  }
	}
	public void setStemmer( FrenchStemmer stemmer ) {
		if ( stemmer != null ) {
			this.stemmer = stemmer;
		}
	}
	@Deprecated 
	public void setExclusionTable( Map<?,?> exclusiontable ) {
		exclusions = new HashSet(exclusiontable.keySet());
	}
}
