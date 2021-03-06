package org.apache.lucene.xmlparser.builders;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.xmlparser.DOMUtils;
import org.apache.lucene.xmlparser.ParserException;
import org.apache.lucene.xmlparser.QueryBuilder;
import org.w3c.dom.Element;
public class TermsQueryBuilder implements QueryBuilder {
	Analyzer analyzer;
	public TermsQueryBuilder(Analyzer analyzer)
	{
		this.analyzer = analyzer;
	}
	public Query getQuery(Element e) throws ParserException {
        String fieldName=DOMUtils.getAttributeWithInheritanceOrFail(e,"fieldName");
 		String text=DOMUtils.getNonBlankTextOrFail(e);
		BooleanQuery bq=new BooleanQuery(DOMUtils.getAttribute(e,"disableCoord",false));
		bq.setMinimumNumberShouldMatch(DOMUtils.getAttribute(e,"minimumNumberShouldMatch",0));
		TokenStream ts = analyzer.tokenStream(fieldName, new StringReader(text));
		try
		{
		  TermAttribute termAtt = ts.addAttribute(TermAttribute.class);
			Term term = null;
			while (ts.incrementToken()) {
				if (term == null)
				{
					term = new Term(fieldName, termAtt.term());
				} else
				{
					term = term.createTerm(termAtt.term()); 
				}
				bq.add(new BooleanClause(new TermQuery(term),BooleanClause.Occur.SHOULD));
			}
		} 
		catch (IOException ioe)
		{
			throw new RuntimeException("Error constructing terms from index:"
					+ ioe);
		}
  		bq.setBoost(DOMUtils.getAttribute(e,"boost",1.0f));
  		return bq;
	}
}
