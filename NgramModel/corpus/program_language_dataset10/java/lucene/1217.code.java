package org.apache.lucene.queryParser.standard.processors;
import java.util.LinkedList;
import java.util.List;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryParser.core.nodes.FieldableNode;
import org.apache.lucene.queryParser.core.nodes.GroupQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryParser.standard.config.MultiFieldAttribute;
public class MultiFieldQueryNodeProcessor extends QueryNodeProcessorImpl {
  private boolean processChildren = true;
  public MultiFieldQueryNodeProcessor() {
  }
  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    return node;
  }
  @Override
  protected void processChildren(QueryNode queryTree) throws QueryNodeException {
    if (this.processChildren) {
      super.processChildren(queryTree);
    } else {
      this.processChildren = true;
    }
  }
  @Override
  protected QueryNode preProcessNode(QueryNode node) throws QueryNodeException {
    if (node instanceof FieldableNode) {
      this.processChildren = false;
      FieldableNode fieldNode = (FieldableNode) node;
      if (fieldNode.getField() == null) {
        if (!getQueryConfigHandler().hasAttribute(MultiFieldAttribute.class)) {
          throw new IllegalArgumentException(
              "MultiFieldAttribute should be set on the QueryConfigHandler");
        }
        CharSequence[] fields = getQueryConfigHandler().getAttribute(
            MultiFieldAttribute.class).getFields();
        if (fields != null && fields.length > 0) {
          fieldNode.setField(fields[0]);
          if (fields.length == 1) {
            return fieldNode;
          } else {
            LinkedList<QueryNode> children = new LinkedList<QueryNode>();
            children.add(fieldNode);
            for (int i = 1; i < fields.length; i++) {
              try {
                fieldNode = (FieldableNode) fieldNode.cloneTree();
                fieldNode.setField(fields[i]);
                children.add(fieldNode);
              } catch (CloneNotSupportedException e) {
              }
            }
            return new GroupQueryNode(new BooleanQueryNode(children));
          }
        }
      }
    }
    return node;
  }
  @Override
  protected List<QueryNode> setChildrenOrder(List<QueryNode> children)
      throws QueryNodeException {
    return children;
  }
}
