package org.apache.lucene.queryParser.standard.config;
import java.util.Locale;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.ParametricRangeQueryNodeProcessor;
import org.apache.lucene.util.AttributeImpl;
public class LocaleAttributeImpl extends AttributeImpl
				implements LocaleAttribute {
  private static final long serialVersionUID = -6804760312720049526L;
  private Locale locale = Locale.getDefault();
  public LocaleAttributeImpl() {
	  locale = Locale.getDefault(); 
  }
  public void setLocale(Locale locale) {
    this.locale = locale;
  }
  public Locale getLocale() {
    return this.locale;
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
    if (other instanceof LocaleAttributeImpl) {
    	LocaleAttributeImpl localeAttr = (LocaleAttributeImpl) other;
      if (localeAttr.locale == this.locale
          || (this.locale != null && localeAttr.locale != null && this.locale
              .equals(localeAttr.locale))) {
        return true;
      }
    }
    return false;
  }
  @Override
  public int hashCode() {
    return (this.locale == null) ? 0 : this.locale.hashCode();
  }
  @Override
  public String toString() {
    return "<localeAttribute locale=" + this.locale + "/>";
  }
}
