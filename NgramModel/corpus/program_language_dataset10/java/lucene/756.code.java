package org.apache.lucene.analysis.de;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
import org.apache.lucene.analysis.LowerCaseTokenizer;
import org.apache.lucene.util.Version;
public class TestGermanAnalyzer extends BaseTokenStreamTestCase {
  public void testReusableTokenStream() throws Exception {
    Analyzer a = new GermanAnalyzer(TEST_VERSION_CURRENT);
    checkOneTermReuse(a, "Tisch", "tisch");
    checkOneTermReuse(a, "Tische", "tisch");
    checkOneTermReuse(a, "Tischen", "tisch");
  }
  public void testExclusionTableBWCompat() throws IOException {
    GermanStemFilter filter = new GermanStemFilter(new LowerCaseTokenizer(TEST_VERSION_CURRENT, 
        new StringReader("Fischen Trinken")));
    CharArraySet set = new CharArraySet(TEST_VERSION_CURRENT, 1, true);
    set.add("fischen");
    filter.setExclusionSet(set);
    assertTokenStreamContents(filter, new String[] { "fischen", "trink" });
  }
  public void testWithKeywordAttribute() throws IOException {
    CharArraySet set = new CharArraySet(TEST_VERSION_CURRENT, 1, true);
    set.add("fischen");
    GermanStemFilter filter = new GermanStemFilter(
        new KeywordMarkerTokenFilter(new LowerCaseTokenizer(TEST_VERSION_CURRENT, new StringReader( 
            "Fischen Trinken")), set));
    assertTokenStreamContents(filter, new String[] { "fischen", "trink" });
  }
  public void testWithKeywordAttributeAndExclusionTable() throws IOException {
    CharArraySet set = new CharArraySet(TEST_VERSION_CURRENT, 1, true);
    set.add("fischen");
    CharArraySet set1 = new CharArraySet(TEST_VERSION_CURRENT, 1, true);
    set1.add("trinken");
    set1.add("fischen");
    GermanStemFilter filter = new GermanStemFilter(
        new KeywordMarkerTokenFilter(new LowerCaseTokenizer(TEST_VERSION_CURRENT, new StringReader(
            "Fischen Trinken")), set));
    filter.setExclusionSet(set1);
    assertTokenStreamContents(filter, new String[] { "fischen", "trinken" });
  }
  public void testExclusionTableReuse() throws Exception {
    GermanAnalyzer a = new GermanAnalyzer(TEST_VERSION_CURRENT);
    checkOneTermReuse(a, "tischen", "tisch");
    a.setStemExclusionTable(new String[] { "tischen" });
    checkOneTermReuse(a, "tischen", "tischen");
  }
  public void testGermanSpecials() throws Exception {
    GermanAnalyzer a = new GermanAnalyzer(TEST_VERSION_CURRENT);
    checkOneTermReuse(a, "Schaltflächen", "schaltflach");
    checkOneTermReuse(a, "Schaltflaechen", "schaltflach");
    a = new GermanAnalyzer(Version.LUCENE_30);
    checkOneTermReuse(a, "Schaltflächen", "schaltflach");
    checkOneTermReuse(a, "Schaltflaechen", "schaltflaech");
  }
}
