package org.apache.lucene.analysis.snowball;
import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tr.TurkishLowerCaseFilter; 
import org.apache.lucene.analysis.LowerCaseFilter; 
import org.tartarus.snowball.SnowballProgram;
public final class SnowballFilter extends TokenFilter {
  private final SnowballProgram stemmer;
  private final TermAttribute termAtt = addAttribute(TermAttribute.class);
  private final KeywordAttribute keywordAttr = addAttribute(KeywordAttribute.class);
  public SnowballFilter(TokenStream input, SnowballProgram stemmer) {
    super(input);
    this.stemmer = stemmer;
  }
  public SnowballFilter(TokenStream in, String name) {
    super(in);
    try {      
      Class<?> stemClass = Class.forName("org.tartarus.snowball.ext." + name + "Stemmer");
      stemmer = (SnowballProgram) stemClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e.toString());
    }
  }
  @Override
  public final boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      if (!keywordAttr.isKeyword()) {
        char termBuffer[] = termAtt.termBuffer();
        final int length = termAtt.termLength();
        stemmer.setCurrent(termBuffer, length);
        stemmer.stem();
        final char finalTerm[] = stemmer.getCurrentBuffer();
        final int newLength = stemmer.getCurrentBufferLength();
        if (finalTerm != termBuffer)
          termAtt.setTermBuffer(finalTerm, 0, newLength);
        else
          termAtt.setTermLength(newLength);
      }
      return true;
    } else {
      return false;
    }
  }
}
