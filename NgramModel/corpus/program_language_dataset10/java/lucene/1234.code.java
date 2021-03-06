package org.apache.lucene.queryParser.spans;
import java.util.List;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.core.nodes.FieldableNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessorImpl;
public class UniqueFieldQueryNodeProcessor extends QueryNodeProcessorImpl {
  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    return node;
  }
  @Override
  protected QueryNode preProcessNode(QueryNode node) throws QueryNodeException {
    if (node instanceof FieldableNode) {
      FieldableNode fieldNode = (FieldableNode) node;
      QueryConfigHandler queryConfig = getQueryConfigHandler();
      if (queryConfig == null) {
        throw new IllegalArgumentException(
            "A config handler is expected by the processor UniqueFieldQueryNodeProcessor!");
      }
      if (!queryConfig.hasAttribute(UniqueFieldAttribute.class)) {
        throw new IllegalArgumentException(
            "UniqueFieldAttribute should be defined in the config handler!");
      }
      CharSequence uniqueField = queryConfig.getAttribute(
          UniqueFieldAttribute.class).getUniqueField();
      fieldNode.setField(uniqueField);
    }
    return node;
  }
  @Override
  protected List<QueryNode> setChildrenOrder(List<QueryNode> children)
      throws QueryNodeException {
    return children;
  }
}
