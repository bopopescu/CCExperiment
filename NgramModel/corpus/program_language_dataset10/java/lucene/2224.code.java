package org.apache.solr.analysis;
import java.io.IOException;
import java.util.LinkedList;
import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
public class DoubleMetaphoneFilter extends TokenFilter {
  private static final String TOKEN_TYPE = "DoubleMetaphone";
  private final LinkedList<State> remainingTokens = new LinkedList<State>();
  private final DoubleMetaphone encoder = new DoubleMetaphone();
  private final boolean inject;
  private final TermAttribute termAtt;
  private final PositionIncrementAttribute posAtt;
  protected DoubleMetaphoneFilter(TokenStream input, int maxCodeLength, boolean inject) {
    super(input);
    this.encoder.setMaxCodeLen(maxCodeLength);
    this.inject = inject;
    this.termAtt = (TermAttribute) addAttribute(TermAttribute.class);
    this.posAtt = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
  }
  @Override
  public boolean incrementToken() throws IOException {
    for(;;) {
      if (!remainingTokens.isEmpty()) {
        restoreState(remainingTokens.removeFirst());
        return true;
      }
      if (!input.incrementToken()) return false;
      int len = termAtt.termLength();
      if (len==0) return true; 
      int firstAlternativeIncrement = inject ? 0 : posAtt.getPositionIncrement();
      String v = new String(termAtt.termBuffer(), 0, len);
      String primaryPhoneticValue = encoder.doubleMetaphone(v);
      String alternatePhoneticValue = encoder.doubleMetaphone(v, true);
      boolean saveState=inject;
      if (primaryPhoneticValue!=null && primaryPhoneticValue.length() > 0 && !primaryPhoneticValue.equals(v)) {
        if (saveState) {
          remainingTokens.addLast(captureState());
        }
        posAtt.setPositionIncrement( firstAlternativeIncrement );
        firstAlternativeIncrement = 0;
        termAtt.setTermBuffer(primaryPhoneticValue);
        saveState = true;
      }
      if (alternatePhoneticValue!=null && alternatePhoneticValue.length() > 0
              && !alternatePhoneticValue.equals(primaryPhoneticValue)
              && !primaryPhoneticValue.equals(v)) {
        if (saveState) {
          remainingTokens.addLast(captureState());
          saveState = false;
        }
        posAtt.setPositionIncrement( firstAlternativeIncrement );
        termAtt.setTermBuffer(alternatePhoneticValue);
        saveState = true;
      }
      if (remainingTokens.isEmpty()) {
        return true;
      }
      if (saveState) {
        remainingTokens.addLast(captureState());
      }
    }
  }
  @Override
  public void reset() throws IOException {
    input.reset();
    remainingTokens.clear();
  }
}
