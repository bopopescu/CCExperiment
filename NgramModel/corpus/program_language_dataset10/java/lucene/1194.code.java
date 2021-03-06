package org.apache.lucene.queryParser.standard.nodes;
import org.apache.lucene.queryParser.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.standard.processors.GroupQueryNodeProcessor;
public class BooleanModifierNode extends ModifierQueryNode {
  private static final long serialVersionUID = -557816496416587068L;
  public BooleanModifierNode(QueryNode node, Modifier mod) {
    super(node, mod);
  }
}
