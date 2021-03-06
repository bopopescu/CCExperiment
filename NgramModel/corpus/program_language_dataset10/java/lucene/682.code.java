package org.apache.lucene.analysis.ngram;
import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public final class NGramTokenFilter extends TokenFilter {
  public static final int DEFAULT_MIN_NGRAM_SIZE = 1;
  public static final int DEFAULT_MAX_NGRAM_SIZE = 2;
  private int minGram, maxGram;
  private char[] curTermBuffer;
  private int curTermLength;
  private int curGramSize;
  private int curPos;
  private int tokStart;
  private TermAttribute termAtt;
  private OffsetAttribute offsetAtt;
  public NGramTokenFilter(TokenStream input, int minGram, int maxGram) {
    super(input);
    if (minGram < 1) {
      throw new IllegalArgumentException("minGram must be greater than zero");
    }
    if (minGram > maxGram) {
      throw new IllegalArgumentException("minGram must not be greater than maxGram");
    }
    this.minGram = minGram;
    this.maxGram = maxGram;
    this.termAtt = addAttribute(TermAttribute.class);
    this.offsetAtt = addAttribute(OffsetAttribute.class);
  }
  public NGramTokenFilter(TokenStream input) {
    this(input, DEFAULT_MIN_NGRAM_SIZE, DEFAULT_MAX_NGRAM_SIZE);
  }
  @Override
  public final boolean incrementToken() throws IOException {
    while (true) {
      if (curTermBuffer == null) {
        if (!input.incrementToken()) {
          return false;
        } else {
          curTermBuffer = termAtt.termBuffer().clone();
          curTermLength = termAtt.termLength();
          curGramSize = minGram;
          curPos = 0;
          tokStart = offsetAtt.startOffset();
        }
      }
      while (curGramSize <= maxGram) {
        while (curPos+curGramSize <= curTermLength) {     
          clearAttributes();
          termAtt.setTermBuffer(curTermBuffer, curPos, curGramSize);
          offsetAtt.setOffset(tokStart + curPos, tokStart + curPos + curGramSize);
          curPos++;
          return true;
        }
        curGramSize++;                         
        curPos = 0;
      }
      curTermBuffer = null;
    }
  }
  @Override
  public void reset() throws IOException {
    super.reset();
    curTermBuffer = null;
  }
}
