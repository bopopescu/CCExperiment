package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.PhraseSlopQueryNodeProcessor;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.util.AttributeImpl;
public class FuzzyAttributeImpl extends AttributeImpl 
				implements FuzzyAttribute {
  private static final long serialVersionUID = -2104763012527049527L;
  private int prefixLength = FuzzyQuery.defaultPrefixLength;
  private float minSimilarity = FuzzyQuery.defaultMinSimilarity;
  public FuzzyAttributeImpl() {
  }
  public void setPrefixLength(int prefixLength) {
    this.prefixLength = prefixLength;
  }
  public int getPrefixLength() {
    return this.prefixLength;
  }
  public void setFuzzyMinSimilarity(float minSimilarity) {
    this.minSimilarity = minSimilarity;
  }
  public float getFuzzyMinSimilarity() {
    return this.minSimilarity;
  }
  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }
  @Override
  public void copyTo(AttributeImpl target) {
    throw new UnsupportedOperationException();
  }
  @Override
  public boolean equals(Object other) {
    if (other instanceof FuzzyAttributeImpl
        && ((FuzzyAttributeImpl) other).prefixLength == this.prefixLength) {
      return true;
    }
    return false;
  }
  @Override
  public int hashCode() {
    return Integer.valueOf(this.prefixLength).hashCode();
  }
  @Override
  public String toString() {
    return "<fuzzyAttribute prefixLength=" + this.prefixLength + "/>";
  }
}
