package org.apache.lucene.collation;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordTokenizer;
import java.text.Collator;
import java.util.Locale;
import java.io.Reader;
public class TestCollationKeyFilter extends CollationTestBase {
  private Collator collator = Collator.getInstance(new Locale("ar"));
  private Analyzer analyzer = new TestAnalyzer(collator);
  private String firstRangeBeginning = encodeCollationKey
    (collator.getCollationKey(firstRangeBeginningOriginal).toByteArray());
  private String firstRangeEnd = encodeCollationKey
    (collator.getCollationKey(firstRangeEndOriginal).toByteArray());
  private String secondRangeBeginning = encodeCollationKey
    (collator.getCollationKey(secondRangeBeginningOriginal).toByteArray());
  private String secondRangeEnd = encodeCollationKey
    (collator.getCollationKey(secondRangeEndOriginal).toByteArray());
  public class TestAnalyzer extends Analyzer {
    private Collator _collator;
    TestAnalyzer(Collator collator) {
      _collator = collator;
    }
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
      TokenStream result = new KeywordTokenizer(reader);
      result = new CollationKeyFilter(result, _collator);
      return result;
    }
  }
  public void testFarsiRangeFilterCollating() throws Exception {
    testFarsiRangeFilterCollating
      (analyzer, firstRangeBeginning, firstRangeEnd, 
       secondRangeBeginning, secondRangeEnd);
  }
  public void testFarsiRangeQueryCollating() throws Exception {
    testFarsiRangeQueryCollating
      (analyzer, firstRangeBeginning, firstRangeEnd, 
       secondRangeBeginning, secondRangeEnd);
  }
  public void testFarsiTermRangeQuery() throws Exception {
    testFarsiTermRangeQuery
      (analyzer, firstRangeBeginning, firstRangeEnd, 
       secondRangeBeginning, secondRangeEnd);
  }
  public void testCollationKeySort() throws Exception {
    Analyzer usAnalyzer = new TestAnalyzer(Collator.getInstance(Locale.US));
    Analyzer franceAnalyzer 
      = new TestAnalyzer(Collator.getInstance(Locale.FRANCE));
    Analyzer swedenAnalyzer 
      = new TestAnalyzer(Collator.getInstance(new Locale("sv", "se")));
    Analyzer denmarkAnalyzer 
      = new TestAnalyzer(Collator.getInstance(new Locale("da", "dk")));
    testCollationKeySort
      (usAnalyzer, franceAnalyzer, swedenAnalyzer, denmarkAnalyzer, "BFJDH");
  }
}
