package org.apache.lucene.queryParser.core.nodes;
import java.util.List;
import org.apache.lucene.queryParser.core.nodes.ParametricQueryNode.CompareOperator;
import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;
public class ParametricRangeQueryNode extends QueryNodeImpl implements
    FieldableNode {
  private static final long serialVersionUID = 7120958816535573935L;
  public ParametricRangeQueryNode(ParametricQueryNode lowerBound,
      ParametricQueryNode upperBound) {
    if (upperBound.getOperator() != CompareOperator.LE
        && upperBound.getOperator() != CompareOperator.LT) {
      throw new IllegalArgumentException("upper bound should have "
          + CompareOperator.LE + " or " + CompareOperator.LT);
    }
    if (lowerBound.getOperator() != CompareOperator.GE
        && lowerBound.getOperator() != CompareOperator.GT) {
      throw new IllegalArgumentException("lower bound should have "
          + CompareOperator.GE + " or " + CompareOperator.GT);
    }
    if (upperBound.getField() != lowerBound.getField()
        || (upperBound.getField() != null && !upperBound.getField().equals(
            lowerBound.getField()))) {
      throw new IllegalArgumentException(
          "lower and upper bounds should have the same field name!");
    }
    allocate();
    setLeaf(false);
    add(lowerBound);
    add(upperBound);
  }
  public ParametricQueryNode getUpperBound() {
    return (ParametricQueryNode) getChildren().get(1);
  }
  public ParametricQueryNode getLowerBound() {
    return (ParametricQueryNode) getChildren().get(0);
  }
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser) {
    return getLowerBound().toQueryString(escapeSyntaxParser) + " AND "
        + getUpperBound().toQueryString(escapeSyntaxParser);
  }
  public CharSequence getField() {
    return getLowerBound().getField();
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("<parametricRange>\n\t");
    sb.append(getUpperBound()).append("\n\t");
    sb.append(getLowerBound()).append("\n");
    sb.append("</parametricRange>\n");
    return sb.toString();
  }
  @Override
  public ParametricRangeQueryNode cloneTree() throws CloneNotSupportedException {
    ParametricRangeQueryNode clone = (ParametricRangeQueryNode) super
        .cloneTree();
    return clone;
  }
  public void setField(CharSequence fieldName) {
    List<QueryNode> children = getChildren();
    if (children != null) {
      for (QueryNode child : getChildren()) {
        if (child instanceof FieldableNode) {
          ((FieldableNode) child).setField(fieldName);
        }
      }
    }
  }
}
