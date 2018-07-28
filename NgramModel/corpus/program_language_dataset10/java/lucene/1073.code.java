package org.apache.lucene.queryParser.analyzing;
import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.util.LuceneTestCase;
public class TestAnalyzingQueryParser extends LuceneTestCase {
  private Analyzer a;
  private String[] wildcardInput;
  private String[] wildcardExpected;
  private String[] prefixInput;
  private String[] prefixExpected;
  private String[] rangeInput;
  private String[] rangeExpected;
  private String[] fuzzyInput;
  private String[] fuzzyExpected;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    wildcardInput = new String[] { "übersetzung über*ung",
        "Mötley Cr\u00fce Mötl?* Crü?", "Renée Zellweger Ren?? Zellw?ger" };
    wildcardExpected = new String[] { "ubersetzung uber*ung", "motley crue motl?* cru?",
        "renee zellweger ren?? zellw?ger" };
    prefixInput = new String[] { "übersetzung übersetz*",
        "Mötley Crüe Mötl* crü*", "René? Zellw*" };
    prefixExpected = new String[] { "ubersetzung ubersetz*", "motley crue motl* cru*",
        "rene? zellw*" };
    rangeInput = new String[] { "[aa TO bb]", "{Anaïs TO Zoé}" };
    rangeExpected = new String[] { "[aa TO bb]", "{anais TO zoe}" };
    fuzzyInput = new String[] { "Übersetzung Übersetzung~0.9",
        "Mötley Crüe Mötley~0.75 Crüe~0.5",
        "Renée Zellweger Renée~0.9 Zellweger~" };
    fuzzyExpected = new String[] { "ubersetzung ubersetzung~0.9",
        "motley crue motley~0.75 crue~0.5", "renee zellweger renee~0.9 zellweger~0.5" };
    a = new ASCIIAnalyzer();
  }
  public void testWildCardQuery() throws ParseException {
    for (int i = 0; i < wildcardInput.length; i++) {
      assertEquals("Testing wildcards with analyzer " + a.getClass() + ", input string: "
          + wildcardInput[i], wildcardExpected[i], parseWithAnalyzingQueryParser(wildcardInput[i], a));
    }
  }
  public void testPrefixQuery() throws ParseException {
    for (int i = 0; i < prefixInput.length; i++) {
      assertEquals("Testing prefixes with analyzer " + a.getClass() + ", input string: "
          + prefixInput[i], prefixExpected[i], parseWithAnalyzingQueryParser(prefixInput[i], a));
    }
  }
  public void testRangeQuery() throws ParseException {
    for (int i = 0; i < rangeInput.length; i++) {
      assertEquals("Testing ranges with analyzer " + a.getClass() + ", input string: "
          + rangeInput[i], rangeExpected[i], parseWithAnalyzingQueryParser(rangeInput[i], a));
    }
  }
  public void testFuzzyQuery() throws ParseException {
    for (int i = 0; i < fuzzyInput.length; i++) {
      assertEquals("Testing fuzzys with analyzer " + a.getClass() + ", input string: "
          + fuzzyInput[i], fuzzyExpected[i], parseWithAnalyzingQueryParser(fuzzyInput[i], a));
    }
  }
  private String parseWithAnalyzingQueryParser(String s, Analyzer a) throws ParseException {
    AnalyzingQueryParser qp = new AnalyzingQueryParser(TEST_VERSION_CURRENT, "field", a);
    org.apache.lucene.search.Query q = qp.parse(s);
    return q.toString("field");
  }
}
class ASCIIAnalyzer extends org.apache.lucene.analysis.Analyzer {
  public ASCIIAnalyzer() {
  }
  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    TokenStream result = new StandardTokenizer(LuceneTestCase.TEST_VERSION_CURRENT, reader);
    result = new StandardFilter(result);
    result = new ASCIIFoldingFilter(result);
    result = new LowerCaseFilter(LuceneTestCase.TEST_VERSION_CURRENT, result);
    return result;
  }
}