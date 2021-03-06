package org.apache.lucene.search.similar;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
public final class SimilarityQueries
{
	private SimilarityQueries()
	{
	}
    public static Query formSimilarQuery( String body,
										  Analyzer a,
										  String field,
										  Set<?> stop)
										  throws IOException
	{	
		TokenStream ts = a.tokenStream( field, new StringReader( body));
		TermAttribute termAtt = ts.addAttribute(TermAttribute.class);
		BooleanQuery tmp = new BooleanQuery();
		Set<String> already = new HashSet<String>(); 
		while (ts.incrementToken()) {
		  String word = termAtt.term();
			if ( stop != null &&
				 stop.contains( word)) continue;
			if ( ! already.add( word)) continue;
			TermQuery tq = new TermQuery( new Term( field, word));
			try
			{
				tmp.add( tq, BooleanClause.Occur.SHOULD);
			}
			catch( BooleanQuery.TooManyClauses too)
			{
				break;
			}
		}
		return tmp;
	}
}
