package org.apache.lucene.queryParser.standard.config;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.util.AttributeImpl;
public class FieldDateResolutionMapAttributeImpl extends AttributeImpl 
				implements FieldDateResolutionMapAttribute {
  private static final long serialVersionUID = -2104763012523049527L;
  private Map<CharSequence, DateTools.Resolution> dateRes = new HashMap<CharSequence, DateTools.Resolution>();
  public FieldDateResolutionMapAttributeImpl() {
  }
  public void setFieldDateResolutionMap(Map<CharSequence, DateTools.Resolution> dateRes) {
    this.dateRes = dateRes;
  }
  public Map<CharSequence, Resolution> getFieldDateResolutionMap() {
    return this.dateRes;
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
    if (other instanceof FieldDateResolutionMapAttributeImpl
        && ((FieldDateResolutionMapAttributeImpl) other).dateRes.equals(this.dateRes) ) {
      return true;
    }
    return false;
  }
  @Override
  public int hashCode() {
    final int prime = 97;
    if (this.dateRes != null) 
      return this.dateRes.hashCode() * prime;
    else 
      return Float.valueOf(prime).hashCode();
  }
  @Override
  public String toString() {
    return "<fieldDateResolutionMapAttribute map=" + this.dateRes + "/>";
  }
}
