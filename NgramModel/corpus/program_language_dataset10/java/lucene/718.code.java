package org.apache.lucene.analysis.th;
import java.io.IOException;
import java.util.Locale;
import java.lang.Character.UnicodeBlock;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import java.text.BreakIterator;
public final class ThaiWordFilter extends TokenFilter {
  private BreakIterator breaker = null;
  private TermAttribute termAtt;
  private OffsetAttribute offsetAtt;
  private State thaiState = null;
  public ThaiWordFilter(TokenStream input) {
    super(input);
    breaker = BreakIterator.getWordInstance(new Locale("th"));
    termAtt = addAttribute(TermAttribute.class);
    offsetAtt = addAttribute(OffsetAttribute.class);
  }
  @Override
  public final boolean incrementToken() throws IOException {
    if (thaiState != null) {
      int start = breaker.current();
      int end = breaker.next();
      if (end != BreakIterator.DONE) {
        restoreState(thaiState);
        termAtt.setTermBuffer(termAtt.termBuffer(), start, end - start);
        offsetAtt.setOffset(offsetAtt.startOffset() + start, offsetAtt.startOffset() + end);
        return true;
      }
      thaiState = null;
    }
    if (input.incrementToken() == false || termAtt.termLength() == 0)
      return false;
    String text = termAtt.term();
    if (UnicodeBlock.of(text.charAt(0)) != UnicodeBlock.THAI) {
      termAtt.setTermBuffer(text.toLowerCase());
      return true;
    }
    thaiState = captureState();
    breaker.setText(text);
    int end = breaker.next();
    if (end != BreakIterator.DONE) {
      termAtt.setTermBuffer(text, 0, end);
      offsetAtt.setOffset(offsetAtt.startOffset(), offsetAtt.startOffset() + end);
      return true;
    }
    return false;
  }
  @Override
  public void reset() throws IOException {
    super.reset();
    thaiState = null;
  }
}
