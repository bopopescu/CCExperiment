package org.apache.solr.analysis;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class TestHyphenatedWordsFilter extends BaseTokenTestCase {
	public void testHyphenatedWords() throws Exception {
		String input = "ecologi-\r\ncal devel-\r\n\r\nop compre-\u0009hensive-hands-on and ecologi-\ncal";
		TokenStream ts = new WhitespaceTokenizer(new StringReader(input));
		HyphenatedWordsFilterFactory factory = new HyphenatedWordsFilterFactory();
		ts = factory.create(ts);
		assertTokenStreamContents(ts, 
		    new String[] { "ecological", "develop", "comprehensive-hands-on", "and", "ecological" });
	}
	public void testHyphenAtEnd() throws Exception {
	    String input = "ecologi-\r\ncal devel-\r\n\r\nop compre-\u0009hensive-hands-on and ecology-";
	    TokenStream ts = new WhitespaceTokenizer(new StringReader(input));
	    HyphenatedWordsFilterFactory factory = new HyphenatedWordsFilterFactory();
	    ts = factory.create(ts);
	    assertTokenStreamContents(ts, 
	        new String[] { "ecological", "develop", "comprehensive-hands-on", "and", "ecology-" });
	  }
}
