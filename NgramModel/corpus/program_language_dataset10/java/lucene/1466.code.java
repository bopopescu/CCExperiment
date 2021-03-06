package org.apache.lucene.analysis.tokenattributes;
import org.apache.lucene.util.Attribute;
public interface OffsetAttribute extends Attribute {
  public int startOffset();
  public void setOffset(int startOffset, int endOffset);
  public int endOffset();
}
