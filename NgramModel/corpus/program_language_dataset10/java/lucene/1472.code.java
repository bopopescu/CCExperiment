package org.apache.lucene.analysis.tokenattributes;
import org.apache.lucene.util.Attribute;
public interface TermAttribute extends Attribute {
  public String term();
  public void setTermBuffer(char[] buffer, int offset, int length);
  public void setTermBuffer(String buffer);
  public void setTermBuffer(String buffer, int offset, int length);
  public char[] termBuffer();
  public char[] resizeTermBuffer(int newSize);
  public int termLength();
  public void setTermLength(int length);
}
