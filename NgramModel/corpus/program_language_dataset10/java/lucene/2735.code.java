package org.apache.solr.analysis;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.SingleTokenTokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.solr.SolrTestCaseJ4;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.apache.solr.analysis.BaseTokenTestCase.*;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
public class TestWordDelimiterFilter extends SolrTestCaseJ4 {
  @BeforeClass
  public static void beforeClass() throws Exception {
    initCore("solrconfig.xml","schema.xml");
  }
  public void posTst(String v1, String v2, String s1, String s2) {
    assertU(adoc("id",  "42",
                 "subword", v1,
                 "subword", v2));
    assertU(commit());
    assertQ("position increment lost",
            req("+id:42 +subword:\"" + s1 + ' ' + s2 + "\"~90")
            ,"//result[@numFound=0]"
    );
    assertQ("position increment lost",
            req("+id:42 +subword:\"" + s1 + ' ' + s2 + "\"~110")
            ,"//result[@numFound=1]"
    );
    clearIndex();
  }
  @Test
  public void testRetainPositionIncrement() {
    posTst("foo","bar","foo","bar");
    posTst("-foo-","-bar-","foo","bar");
    posTst("foo","bar","-foo-","-bar-");
    posTst("123","456","123","456");
    posTst("/123/","/456/","123","456");
    posTst("/123/abc","qwe/456/","abc","qwe");
    posTst("zoo-foo","bar-baz","foo","bar");
    posTst("zoo-foo-123","456-bar-baz","foo","bar");
  }
  @Test
  public void testNoGenerationEdgeCase() {
    assertU(adoc("id", "222", "numberpartfail", "123.123.123.123"));
    clearIndex();
  }
  @Test
  public void testIgnoreCaseChange() {
    assertU(adoc("id",  "43",
                 "wdf_nocase", "HellO WilliAM",
                 "subword", "GoodBye JonEs"));
    assertU(commit());
    assertQ("no case change",
            req("wdf_nocase:(hell o am)")
            ,"//result[@numFound=0]"
    );
    assertQ("case change",
            req("subword:(good jon)")
            ,"//result[@numFound=1]"
    );
    clearIndex();
  }
  @Test
  public void testPreserveOrignalTrue() {
    assertU(adoc("id",  "144",
                 "wdf_preserve", "404-123"));
    assertU(commit());
    assertQ("preserving original word",
            req("wdf_preserve:404")
            ,"//result[@numFound=1]"
    );
    assertQ("preserving original word",
        req("wdf_preserve:123")
        ,"//result[@numFound=1]"
    );
    assertQ("preserving original word",
        req("wdf_preserve:404-123*")
        ,"//result[@numFound=1]"
    );
    clearIndex();
  }
  @Test
  public void testOffsets() throws IOException {
    WordDelimiterFilter wdf = new WordDelimiterFilter(
            new SingleTokenTokenStream(new Token("foo-bar", 5, 12)),
    1,1,0,0,1,1,0);
    assertTokenStreamContents(wdf, 
        new String[] { "foo", "bar", "foobar" },
        new int[] { 5, 9, 5 }, 
        new int[] { 8, 12, 12 });
    wdf = new WordDelimiterFilter(
            new SingleTokenTokenStream(new Token("foo-bar", 5, 6)),
    1,1,0,0,1,1,0);
    assertTokenStreamContents(wdf,
        new String[] { "foo", "bar", "foobar" },
        new int[] { 5, 5, 5 },
        new int[] { 6, 6, 6 });
  }
  @Test
  public void testOffsetChange() throws Exception
  {
    WordDelimiterFilter wdf = new WordDelimiterFilter(
      new SingleTokenTokenStream(new Token("übelkeit)", 7, 16)),
      1,1,0,0,1,1,0
    );
    assertTokenStreamContents(wdf,
        new String[] { "übelkeit" },
        new int[] { 7 },
        new int[] { 15 });
  }
  @Test
  public void testOffsetChange2() throws Exception
  {
    WordDelimiterFilter wdf = new WordDelimiterFilter(
      new SingleTokenTokenStream(new Token("(übelkeit", 7, 17)),
      1,1,0,0,1,1,0
    );
    assertTokenStreamContents(wdf,
        new String[] { "übelkeit" },
        new int[] { 8 },
        new int[] { 17 });
  }
  @Test
  public void testOffsetChange3() throws Exception
  {
    WordDelimiterFilter wdf = new WordDelimiterFilter(
      new SingleTokenTokenStream(new Token("(übelkeit", 7, 16)),
      1,1,0,0,1,1,0
    );
    assertTokenStreamContents(wdf,
        new String[] { "übelkeit" },
        new int[] { 8 },
        new int[] { 16 });
  }
  @Test
  public void testOffsetChange4() throws Exception
  {
    WordDelimiterFilter wdf = new WordDelimiterFilter(
      new SingleTokenTokenStream(new Token("(foo,bar)", 7, 16)),
      1,1,0,0,1,1,0
    );
    assertTokenStreamContents(wdf,
        new String[] { "foo", "bar", "foobar"},
        new int[] { 8, 12, 8 },
        new int[] { 11, 15, 15 });
  }
  @Test
  public void testAlphaNumericWords(){
     assertU(adoc("id",  "68","numericsubword","Java/J2SE"));
     assertU(commit());
     assertQ("j2se found",
            req("numericsubword:(J2SE)")
            ,"//result[@numFound=1]"
    );
      assertQ("no j2 or se",
            req("numericsubword:(J2 OR SE)")
            ,"//result[@numFound=0]"
    );
    clearIndex();
  }
  @Test
  public void testProtectedWords(){
    assertU(adoc("id", "70","protectedsubword","c# c++ .net Java/J2SE"));
    assertU(commit());
    assertQ("java found",
            req("protectedsubword:(java)")
            ,"//result[@numFound=1]"
    );
    assertQ(".net found",
            req("protectedsubword:(.net)")
            ,"//result[@numFound=1]"
    );
    assertQ("c# found",
            req("protectedsubword:(c#)")
            ,"//result[@numFound=1]"
    );
    assertQ("c++ found",
            req("protectedsubword:(c++)")
            ,"//result[@numFound=1]"
    );
    assertQ("c found?",
            req("protectedsubword:c")
            ,"//result[@numFound=0]"
    );
    assertQ("net found?",
            req("protectedsubword:net")
            ,"//result[@numFound=0]"
    );
    clearIndex();
  }
  public void doSplit(final String input, String... output) throws Exception {
    WordDelimiterFilter wdf = new WordDelimiterFilter(new KeywordTokenizer(
        new StringReader(input)), 1, 1, 0, 0, 0);
    assertTokenStreamContents(wdf, output);
  }
  @Test
  public void testSplits() throws Exception {
    doSplit("basic-split","basic","split");
    doSplit("camelCase","camel","Case");
    doSplit("\u0e1a\u0e49\u0e32\u0e19","\u0e1a\u0e49\u0e32\u0e19");
    doSplit("test's'", "test");
    doSplit("Роберт", "Роберт");
    doSplit("РобЕрт", "Роб", "Ерт");
    doSplit("aǅungla", "aǅungla");
    doSplit("ســـــــــــــــــلام", "ســـــــــــــــــلام");
    doSplit("۞test", "۞test");
    doSplit("हिन्दी", "हिन्दी");
    doSplit("١٢٣٤", "١٢٣٤");
    doSplit("𠀀𠀀", "𠀀𠀀");
  }
  public void doSplitPossessive(int stemPossessive, final String input, final String... output) throws Exception {
    WordDelimiterFilter wdf = new WordDelimiterFilter(new KeywordTokenizer(
        new StringReader(input)), 1,1,0,0,0,1,0,1,stemPossessive, null);
    assertTokenStreamContents(wdf, output);
  }
  @Test
  public void testPossessives() throws Exception {
    doSplitPossessive(1, "ra's", "ra");
    doSplitPossessive(0, "ra's", "ra", "s");
  }
  private final class LargePosIncTokenFilter extends TokenFilter {
    private TermAttribute termAtt;
    private PositionIncrementAttribute posIncAtt;
    protected LargePosIncTokenFilter(TokenStream input) {
      super(input);
      termAtt = (TermAttribute) addAttribute(TermAttribute.class);
      posIncAtt = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
    }
    @Override
    public boolean incrementToken() throws IOException {
      if (input.incrementToken()) {
        if (termAtt.term().equals("largegap") || termAtt.term().equals("/"))
          posIncAtt.setPositionIncrement(10);
        return true;
      } else {
        return false;
      }
    }  
  }
  @Test
  public void testPositionIncrements() throws Exception {
    final CharArraySet protWords = new CharArraySet(new HashSet<String>(Arrays.asList("NUTCH")), false);
    Analyzer a = new Analyzer() {
      public TokenStream tokenStream(String field, Reader reader) {
        return new WordDelimiterFilter(
            new WhitespaceTokenizer(reader),
            1, 1, 0, 0, 1, 1, 0, 1, 1, protWords);
      }
    };
    assertAnalyzesTo(a, "LUCENE / SOLR", new String[] { "LUCENE", "SOLR" },
        new int[] { 0, 9 },
        new int[] { 6, 13 },
        new int[] { 1, 1 });
    assertAnalyzesTo(a, "LUCENE / solR", new String[] { "LUCENE", "sol", "R", "solR" },
        new int[] { 0, 9, 12, 9 },
        new int[] { 6, 12, 13, 13 },
        new int[] { 1, 1, 1, 0 });
    assertAnalyzesTo(a, "LUCENE / NUTCH SOLR", new String[] { "LUCENE", "NUTCH", "SOLR" },
        new int[] { 0, 9, 15 },
        new int[] { 6, 14, 19 },
        new int[] { 1, 1, 1 });
    Analyzer a2 = new Analyzer() {
      public TokenStream tokenStream(String field, Reader reader) {
        return new WordDelimiterFilter(
            new LargePosIncTokenFilter(
            new WhitespaceTokenizer(reader)),
            1, 1, 0, 0, 1, 1, 0, 1, 1, protWords);
      }
    };
    assertAnalyzesTo(a2, "LUCENE largegap SOLR", new String[] { "LUCENE", "largegap", "SOLR" },
        new int[] { 0, 7, 16 },
        new int[] { 6, 15, 20 },
        new int[] { 1, 10, 1 });
    assertAnalyzesTo(a2, "LUCENE / SOLR", new String[] { "LUCENE", "SOLR" },
        new int[] { 0, 9 },
        new int[] { 6, 13 },
        new int[] { 1, 11 });
    assertAnalyzesTo(a2, "LUCENE / solR", new String[] { "LUCENE", "sol", "R", "solR" },
        new int[] { 0, 9, 12, 9 },
        new int[] { 6, 12, 13, 13 },
        new int[] { 1, 11, 1, 0 });
    assertAnalyzesTo(a2, "LUCENE / NUTCH SOLR", new String[] { "LUCENE", "NUTCH", "SOLR" },
        new int[] { 0, 9, 15 },
        new int[] { 6, 14, 19 },
        new int[] { 1, 11, 1 });
  }
}
