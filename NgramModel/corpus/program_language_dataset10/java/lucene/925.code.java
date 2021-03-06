package org.apache.lucene.benchmark.quality;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
public interface QualityQueryParser {
  public Query parse(QualityQuery qq) throws ParseException;
}
