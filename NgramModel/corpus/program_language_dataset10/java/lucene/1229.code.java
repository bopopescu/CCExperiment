package org.apache.lucene.queryParser.spans;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.nodes.FieldQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.standard.builders.StandardQueryBuilder;
import org.apache.lucene.search.spans.SpanTermQuery;
public class SpanTermQueryNodeBuilder implements StandardQueryBuilder {
  public SpanTermQuery build(QueryNode node) throws QueryNodeException {
    FieldQueryNode fieldQueryNode = (FieldQueryNode) node;
    return new SpanTermQuery(new Term(fieldQueryNode.getFieldAsString(),
        fieldQueryNode.getTextAsString()));
  }
}
