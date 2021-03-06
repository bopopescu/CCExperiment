package org.apache.lucene.queryParser.standard.config;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.lucene.queryParser.core.config.FieldConfig;
import org.apache.lucene.queryParser.standard.processors.MultiFieldQueryNodeProcessor;
import org.apache.lucene.util.AttributeImpl;
public class FieldBoostMapAttributeImpl extends AttributeImpl 
				implements FieldBoostMapAttribute {
  private static final long serialVersionUID = -2104763012523049527L;
  private Map<CharSequence, Float> boosts = new LinkedHashMap<CharSequence, Float>();
  public FieldBoostMapAttributeImpl() {
  }
  public void setFieldBoostMap(Map<CharSequence, Float> boosts) {
    this.boosts = boosts;
  }
  public Map<CharSequence, Float> getFieldBoostMap() {
    return this.boosts;
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
    if (other instanceof FieldBoostMapAttributeImpl
        && ((FieldBoostMapAttributeImpl) other).boosts.equals(this.boosts) ) {
      return true;
    }
    return false;
  }
  @Override
  public int hashCode() {
    final int prime = 97;
    if (this.boosts != null) 
      return this.boosts.hashCode() * prime;
    else 
      return Float.valueOf(prime).hashCode();
  }
  @Override
  public String toString() {
    return "<fieldBoostMapAttribute map=" + this.boosts + "/>";
  }
}
