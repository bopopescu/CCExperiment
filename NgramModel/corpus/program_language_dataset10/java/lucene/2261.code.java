package org.apache.solr.analysis;
import org.apache.commons.codec.Encoder;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import java.io.IOException;
public class PhoneticFilter extends TokenFilter 
{
  protected boolean inject = true; 
  protected Encoder encoder = null;
  protected String name = null;
  protected State save = null;
  private final TermAttribute termAtt;
  private final PositionIncrementAttribute posAtt;
  public PhoneticFilter(TokenStream in, Encoder encoder, String name, boolean inject) {
    super(in);
    this.encoder = encoder;
    this.name = name;
    this.inject = inject;
    this.termAtt = (TermAttribute) addAttribute(TermAttribute.class);
    this.posAtt = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);    
  }
  @Override
  public boolean incrementToken() throws IOException {
    if( save != null ) {
      restoreState(save);
      save = null;
      return true;
    }
    if (!input.incrementToken()) return false;
    if (termAtt.termLength()==0) return true;
    String value = termAtt.term();
    String phonetic = null;
    try {
     String v = encoder.encode(value).toString();
     if (v.length() > 0 && !value.equals(v)) phonetic = v;
    } catch (Exception ignored) {} 
    if (phonetic == null) return true;
    if (!inject) {
      termAtt.setTermBuffer(phonetic);
      return true;
    }
    int origOffset = posAtt.getPositionIncrement();
    posAtt.setPositionIncrement(0);
    save = captureState();
    posAtt.setPositionIncrement(origOffset);
    termAtt.setTermBuffer(phonetic);
    return true;
  }
  @Override
  public void reset() throws IOException {
    input.reset();
    save = null;
  }
}
