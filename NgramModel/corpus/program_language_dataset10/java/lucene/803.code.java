package org.apache.lucene.analysis.snowball;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Payload;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.FlagsAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.Version;
public class TestSnowball extends BaseTokenStreamTestCase {
  public void testEnglish() throws Exception {
    Analyzer a = new SnowballAnalyzer(TEST_VERSION_CURRENT, "English");
    assertAnalyzesTo(a, "he abhorred accents",
        new String[]{"he", "abhor", "accent"});
  }
  public void testStopwords() throws Exception {
    Analyzer a = new SnowballAnalyzer(TEST_VERSION_CURRENT, "English",
        StandardAnalyzer.STOP_WORDS_SET);
    assertAnalyzesTo(a, "the quick brown fox jumped",
        new String[]{"quick", "brown", "fox", "jump"});
  }
  public void testEnglishLowerCase() throws Exception {
    Analyzer a = new SnowballAnalyzer(TEST_VERSION_CURRENT, "English");
    assertAnalyzesTo(a, "cryogenic", new String[] { "cryogen" });
    assertAnalyzesTo(a, "CRYOGENIC", new String[] { "cryogen" });
    Analyzer b = new SnowballAnalyzer(Version.LUCENE_30, "English");
    assertAnalyzesTo(b, "cryogenic", new String[] { "cryogen" });
    assertAnalyzesTo(b, "CRYOGENIC", new String[] { "cryogen" });
  }
  public void testTurkish() throws Exception {
    Analyzer a = new SnowballAnalyzer(TEST_VERSION_CURRENT, "Turkish");
    assertAnalyzesTo(a, "ağacı", new String[] { "ağaç" });
    assertAnalyzesTo(a, "AĞACI", new String[] { "ağaç" });
  }
  public void testTurkishBWComp() throws Exception {
    Analyzer a = new SnowballAnalyzer(Version.LUCENE_30, "Turkish");
    assertAnalyzesTo(a, "ağacı", new String[] { "ağaç" });
    assertAnalyzesTo(a, "AĞACI", new String[] { "ağaci" });
  }
  public void testReusableTokenStream() throws Exception {
    Analyzer a = new SnowballAnalyzer(TEST_VERSION_CURRENT, "English");
    assertAnalyzesToReuse(a, "he abhorred accents",
        new String[]{"he", "abhor", "accent"});
    assertAnalyzesToReuse(a, "she abhorred him",
        new String[]{"she", "abhor", "him"});
  }
  public void testFilterTokens() throws Exception {
    SnowballFilter filter = new SnowballFilter(new TestTokenStream(), "English");
    TermAttribute termAtt = filter.getAttribute(TermAttribute.class);
    OffsetAttribute offsetAtt = filter.getAttribute(OffsetAttribute.class);
    TypeAttribute typeAtt = filter.getAttribute(TypeAttribute.class);
    PayloadAttribute payloadAtt = filter.getAttribute(PayloadAttribute.class);
    PositionIncrementAttribute posIncAtt = filter.getAttribute(PositionIncrementAttribute.class);
    FlagsAttribute flagsAtt = filter.getAttribute(FlagsAttribute.class);
    filter.incrementToken();
    assertEquals("accent", termAtt.term());
    assertEquals(2, offsetAtt.startOffset());
    assertEquals(7, offsetAtt.endOffset());
    assertEquals("wrd", typeAtt.type());
    assertEquals(3, posIncAtt.getPositionIncrement());
    assertEquals(77, flagsAtt.getFlags());
    assertEquals(new Payload(new byte[]{0,1,2,3}), payloadAtt.getPayload());
  }
  private final class TestTokenStream extends TokenStream {
    private TermAttribute termAtt;
    private OffsetAttribute offsetAtt;
    private TypeAttribute typeAtt;
    private PayloadAttribute payloadAtt;
    private PositionIncrementAttribute posIncAtt;
    private FlagsAttribute flagsAtt;
    TestTokenStream() {
      super();
      termAtt = addAttribute(TermAttribute.class);
      offsetAtt = addAttribute(OffsetAttribute.class);
      typeAtt = addAttribute(TypeAttribute.class);
      payloadAtt = addAttribute(PayloadAttribute.class);
      posIncAtt = addAttribute(PositionIncrementAttribute.class);
      flagsAtt = addAttribute(FlagsAttribute.class);
    }
    @Override
    public boolean incrementToken() {
      clearAttributes();
      termAtt.setTermBuffer("accents");
      offsetAtt.setOffset(2, 7);
      typeAtt.setType("wrd");
      posIncAtt.setPositionIncrement(3);
      payloadAtt.setPayload(new Payload(new byte[]{0,1,2,3}));
      flagsAtt.setFlags(77);
      return true;
    }
  }
}