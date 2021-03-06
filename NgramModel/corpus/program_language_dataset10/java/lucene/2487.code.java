package org.apache.solr.search;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Filter;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.function.*;
public class FunctionRangeQParserPlugin extends QParserPlugin {
  public static String NAME = "frange";
  public void init(NamedList args) {
  }
  public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    return new QParser(qstr, localParams, params, req) {
      ValueSource vs;
      String funcStr;
      public Query parse() throws ParseException {
        funcStr = localParams.get(QueryParsing.V, null);
        Query funcQ = subQuery(funcStr, FunctionQParserPlugin.NAME).parse();
        if (funcQ instanceof FunctionQuery) {
          vs = ((FunctionQuery)funcQ).getValueSource();
        } else {
          vs = new QueryValueSource(funcQ, 0.0f);
        }
        String l = localParams.get("l");
        String u = localParams.get("u");
        boolean includeLower = localParams.getBool("incl",true);
        boolean includeUpper = localParams.getBool("incu",true);
        ValueSourceRangeFilter rf = new ValueSourceRangeFilter(vs, l, u, includeLower, includeUpper);
        SolrConstantScoreQuery csq = new SolrConstantScoreQuery(rf);
        return csq;
      }
    };
  }
}
