package org.apache.lucene.analysis.cn.smart.hhmm;
import java.util.Arrays;
import org.apache.lucene.analysis.cn.smart.WordType; 
public class SegToken {
  public char[] charArray;
  public int startOffset;
  public int endOffset;
  public int wordType;
  public int weight;
  public int index;
  public SegToken(char[] idArray, int start, int end, int wordType, int weight) {
    this.charArray = idArray;
    this.startOffset = start;
    this.endOffset = end;
    this.wordType = wordType;
    this.weight = weight;
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    for(int i=0;i<charArray.length;i++) {
      result = prime * result + charArray[i];
    }
    result = prime * result + endOffset;
    result = prime * result + index;
    result = prime * result + startOffset;
    result = prime * result + weight;
    result = prime * result + wordType;
    return result;
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SegToken other = (SegToken) obj;
    if (!Arrays.equals(charArray, other.charArray))
      return false;
    if (endOffset != other.endOffset)
      return false;
    if (index != other.index)
      return false;
    if (startOffset != other.startOffset)
      return false;
    if (weight != other.weight)
      return false;
    if (wordType != other.wordType)
      return false;
    return true;
  }
}
