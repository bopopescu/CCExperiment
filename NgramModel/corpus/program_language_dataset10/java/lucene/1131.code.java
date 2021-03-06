package org.apache.lucene.queryParser.core.parser;
import org.apache.lucene.queryParser.core.QueryNodeParseException;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
public interface SyntaxParser {
  public QueryNode parse(CharSequence query, CharSequence field)
      throws QueryNodeParseException;
}
