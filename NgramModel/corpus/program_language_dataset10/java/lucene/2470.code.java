package org.apache.solr.search;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.function.BoostedQuery;
import org.apache.solr.search.function.FunctionQuery;
import org.apache.solr.search.function.QueryValueSource;
import org.apache.solr.search.function.ValueSource;
public class BoostQParserPlugin extends QParserPlugin {
  public static String NAME = "boost";
  public static String BOOSTFUNC = "b";
  public void init(NamedList args) {
  }
  public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    return new QParser(qstr, localParams, params, req) {
      QParser baseParser;
      ValueSource vs;
      String b;
      public Query parse() throws ParseException {
        b = localParams.get(BOOSTFUNC);
        baseParser = subQuery(localParams.get(QueryParsing.V), null);
        Query q = baseParser.parse();
        if (b == null) return q;
        Query bq = subQuery(b, FunctionQParserPlugin.NAME).parse();
        if (bq instanceof FunctionQuery) {
          vs = ((FunctionQuery)bq).getValueSource();
        } else {
          vs = new QueryValueSource(bq, 0.0f);
        }
        return new BoostedQuery(q, vs);
      }
      public String[] getDefaultHighlightFields() {
        return baseParser.getDefaultHighlightFields();
      }
      public Query getHighlightQuery() throws ParseException {
        return baseParser.getHighlightQuery();
      }
      public void addDebugInfo(NamedList<Object> debugInfo) {
        baseParser.addDebugInfo(debugInfo);
        debugInfo.add("boost_str",b);
        debugInfo.add("boost_parsed",vs);
      }
    };
  }
}
