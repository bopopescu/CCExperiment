package org.apache.lucene.analysis.tr;
import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public final class TurkishLowerCaseFilter extends TokenFilter {
  private static final int LATIN_CAPITAL_LETTER_I = '\u0049';
  private static final int LATIN_SMALL_LETTER_I = '\u0069';
  private static final int LATIN_SMALL_LETTER_DOTLESS_I = '\u0131';
  private static final int COMBINING_DOT_ABOVE = '\u0307';
  private final TermAttribute termAtt;
  public TurkishLowerCaseFilter(TokenStream in) {
    super(in);
    termAtt = addAttribute(TermAttribute.class);
  }
  @Override
  public final boolean incrementToken() throws IOException {
    boolean iOrAfter = false;
    if (input.incrementToken()) {
      final char[] buffer = termAtt.termBuffer();
      int length = termAtt.termLength();
      for (int i = 0; i < length;) {
        final int ch = Character.codePointAt(buffer, i);
        iOrAfter = (ch == LATIN_CAPITAL_LETTER_I || 
            (iOrAfter && Character.getType(ch) == Character.NON_SPACING_MARK));
        if (iOrAfter) { 
          switch(ch) {
            case COMBINING_DOT_ABOVE:
              length = delete(buffer, i, length);
              continue;
            case LATIN_CAPITAL_LETTER_I:
              if (isBeforeDot(buffer, i + 1, length)) {
                buffer[i] = LATIN_SMALL_LETTER_I;
              } else {
                buffer[i] = LATIN_SMALL_LETTER_DOTLESS_I;
                iOrAfter = false;
              }
              i++;
              continue;
          }
        }
        i += Character.toChars(Character.toLowerCase(ch), buffer, i);
      }
      termAtt.setTermLength(length);
      return true;
    } else
      return false;
  }
  private boolean isBeforeDot(char s[], int pos, int len) {
    for (int i = pos; i < len;) {
      final int ch = Character.codePointAt(s, i);
      if (Character.getType(ch) != Character.NON_SPACING_MARK)
        return false;
      if (ch == COMBINING_DOT_ABOVE)
        return true;
      i += Character.charCount(ch);
    }
    return false;
  }
  private int delete(char s[], int pos, int len) {
    if (pos < len) 
      System.arraycopy(s, pos + 1, s, pos, len - pos - 1);
    return len - 1;
  }
}
