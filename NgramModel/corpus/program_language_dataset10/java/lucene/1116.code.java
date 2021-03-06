package org.apache.lucene.queryParser.core.nodes;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
public class NoTokenFoundQueryNode extends DeletedQueryNode {
  private static final long serialVersionUID = 7332975497586993833L;
  public NoTokenFoundQueryNode() {
    super();
  }
  @Override
  public CharSequence toQueryString(EscapeQuerySyntax escaper) {
    return "[NTF]";
  }
  @Override
  public String toString() {
    return "<notokenfound/>";
  }
  @Override
  public QueryNode cloneTree() throws CloneNotSupportedException {
    NoTokenFoundQueryNode clone = (NoTokenFoundQueryNode) super.cloneTree();
    return clone;
  }
}
