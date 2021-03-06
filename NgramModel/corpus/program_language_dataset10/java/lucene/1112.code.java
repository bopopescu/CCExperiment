package org.apache.lucene.queryParser.core.nodes;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.messages.MessageImpl;
import org.apache.lucene.queryParser.core.QueryNodeError;
import org.apache.lucene.queryParser.core.messages.QueryParserMessages;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
public class GroupQueryNode extends QueryNodeImpl {
  private static final long serialVersionUID = -9204673493869114999L;
  public GroupQueryNode(QueryNode query) {
    if (query == null) {
      throw new QueryNodeError(new MessageImpl(
          QueryParserMessages.PARAMETER_VALUE_NOT_SUPPORTED, "query", "null"));
    }
    allocate();
    setLeaf(false);
    add(query);
  }
  public QueryNode getChild() {
    return getChildren().get(0);
  }
  @Override
  public String toString() {
    return "<group>" + "\n" + getChild().toString() + "\n</group>";
  }
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser) {
    if (getChild() == null)
      return "";
    return "( " + getChild().toQueryString(escapeSyntaxParser) + " )";
  }
  @Override
  public QueryNode cloneTree() throws CloneNotSupportedException {
    GroupQueryNode clone = (GroupQueryNode) super.cloneTree();
    return clone;
  }
  public void setChild(QueryNode child) {
    List<QueryNode> list = new ArrayList<QueryNode>();
    list.add(child);
    this.set(list);
  }
}
