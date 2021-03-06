package org.apache.lucene.analysis.cz;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.util.Version;
public class TestCzechAnalyzer extends BaseTokenStreamTestCase {
  @Deprecated
  public void testStopWordLegacy() throws Exception {
    assertAnalyzesTo(new CzechAnalyzer(Version.LUCENE_30), "Pokud mluvime o volnem", 
        new String[] { "mluvime", "volnem" });
  }
  public void testStopWord() throws Exception {
    assertAnalyzesTo(new CzechAnalyzer(TEST_VERSION_CURRENT), "Pokud mluvime o volnem", 
        new String[] { "mluvim", "voln" });
  }
  @Deprecated
  public void testReusableTokenStreamLegacy() throws Exception {
    Analyzer analyzer = new CzechAnalyzer(Version.LUCENE_30);
    assertAnalyzesToReuse(analyzer, "Pokud mluvime o volnem", new String[] { "mluvime", "volnem" });
    assertAnalyzesToReuse(analyzer, "Česká Republika", new String[] { "česká", "republika" });
  }
  public void testReusableTokenStream() throws Exception {
    Analyzer analyzer = new CzechAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesToReuse(analyzer, "Pokud mluvime o volnem", new String[] { "mluvim", "voln" });
    assertAnalyzesToReuse(analyzer, "Česká Republika", new String[] { "česk", "republik" });
  }
  @Deprecated
  private class UnreliableInputStream extends InputStream {
    @Override
    public int read() throws IOException {
      throw new IOException();
    }
  }
  @Deprecated
  public void testInvalidStopWordFile() throws Exception {
    CzechAnalyzer cz = new CzechAnalyzer(Version.LUCENE_30);
    cz.loadStopWords(new UnreliableInputStream(), "UTF-8");
    assertAnalyzesTo(cz, "Pokud mluvime o volnem",
        new String[] { "pokud", "mluvime", "o", "volnem" });
  }
  @Deprecated
  public void testStopWordFileReuse() throws Exception {
    CzechAnalyzer cz = new CzechAnalyzer(Version.LUCENE_30);
    assertAnalyzesToReuse(cz, "Česká Republika", 
      new String[] { "česká", "republika" });
    InputStream stopwords = getClass().getResourceAsStream("customStopWordFile.txt");
    cz.loadStopWords(stopwords, "UTF-8");
    assertAnalyzesToReuse(cz, "Česká Republika", new String[] { "česká" });
  }
  public void testWithStemExclusionSet() throws IOException{
    CharArraySet set = new CharArraySet(TEST_VERSION_CURRENT, 1, true);
    set.add("hole");
    CzechAnalyzer cz = new CzechAnalyzer(TEST_VERSION_CURRENT, CharArraySet.EMPTY_SET, set);
    assertAnalyzesTo(cz, "hole desek", new String[] {"hole", "desk"});
  }
}
