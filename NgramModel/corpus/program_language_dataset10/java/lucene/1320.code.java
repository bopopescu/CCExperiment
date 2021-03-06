package org.apache.lucene.queryParser.surround.query;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
public class BasicQueryFactory {
  public BasicQueryFactory(int maxBasicQueries) {
    this.maxBasicQueries = maxBasicQueries;
    this.queriesMade = 0;
  }
  public BasicQueryFactory() {
    this(1024);
  }
  private int maxBasicQueries;
  private int queriesMade;
  public int getNrQueriesMade() {return queriesMade;}
  public int getMaxBasicQueries() {return maxBasicQueries;}
  private synchronized void checkMax() throws TooManyBasicQueries {
    if (queriesMade >= maxBasicQueries)
      throw new TooManyBasicQueries(getMaxBasicQueries());
    queriesMade++;
  }
  public TermQuery newTermQuery(Term term) throws TooManyBasicQueries {
    checkMax();
    return new TermQuery(term);
  }
  public SpanTermQuery newSpanTermQuery(Term term) throws TooManyBasicQueries {
    checkMax();
    return new SpanTermQuery(term);
  }
}
