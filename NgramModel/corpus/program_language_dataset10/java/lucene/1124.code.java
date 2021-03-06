package org.apache.lucene.queryParser.core.nodes;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
public interface QueryNode extends Serializable {
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser);
  public String toString();
  public List<QueryNode> getChildren();
  public boolean isLeaf();
  public boolean containsTag(CharSequence tagName);
  public Object getTag(CharSequence tagName);
  public QueryNode getParent();
  public QueryNode cloneTree() throws CloneNotSupportedException;
  public void add(QueryNode child);
  public void add(List<QueryNode> children);
  public void set(List<QueryNode> children);
  public void setTag(CharSequence tagName, Object value);
  public void unsetTag(CharSequence tagName);
  public Map<CharSequence, Object> getTags();
}
