package org.apache.lucene.queryParser.surround.query;
import java.util.List;
import java.util.Iterator;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause;
import java.io.IOException;
public class OrQuery extends ComposedQuery implements DistanceSubQuery { 
  public OrQuery(List<SrndQuery> queries, boolean infix, String opName) {
    super(queries, infix, opName);
  }
  @Override
  public Query makeLuceneQueryFieldNoBoost(String fieldName, BasicQueryFactory qf) {
    return SrndBooleanQuery.makeBooleanQuery(
      makeLuceneSubQueriesField(fieldName, qf), BooleanClause.Occur.SHOULD);
  }
  public String distanceSubQueryNotAllowed() {
    Iterator sqi = getSubQueriesIterator();
    while (sqi.hasNext()) {
      SrndQuery leq = (SrndQuery) sqi.next();
      if (leq instanceof DistanceSubQuery) {
        String m = ((DistanceSubQuery)leq).distanceSubQueryNotAllowed();
        if (m != null) {
          return m;
        }
      } else {
        return "subquery not allowed: " + leq.toString();
      }
    }
    return null;
  }
  public void addSpanQueries(SpanNearClauseFactory sncf) throws IOException {
    Iterator sqi = getSubQueriesIterator();
    while (sqi.hasNext()) {
      ((DistanceSubQuery)sqi.next()).addSpanQueries(sncf);
    }
  }
}
