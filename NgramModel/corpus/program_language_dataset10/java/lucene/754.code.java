package org.apache.lucene.analysis.cz;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
import org.apache.lucene.analysis.WhitespaceTokenizer;
public class TestCzechStemmer extends BaseTokenStreamTestCase {
  public void testMasculineNouns() throws IOException {
    CzechAnalyzer cz = new CzechAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(cz, "pán", new String[] { "pán" });
    assertAnalyzesTo(cz, "páni", new String[] { "pán" });
    assertAnalyzesTo(cz, "pánové", new String[] { "pán" });
    assertAnalyzesTo(cz, "pána", new String[] { "pán" });
    assertAnalyzesTo(cz, "pánů", new String[] { "pán" });
    assertAnalyzesTo(cz, "pánovi", new String[] { "pán" });
    assertAnalyzesTo(cz, "pánům", new String[] { "pán" });
    assertAnalyzesTo(cz, "pány", new String[] { "pán" });
    assertAnalyzesTo(cz, "páne", new String[] { "pán" });
    assertAnalyzesTo(cz, "pánech", new String[] { "pán" });
    assertAnalyzesTo(cz, "pánem", new String[] { "pán" });
    assertAnalyzesTo(cz, "hrad", new String[] { "hrad" });
    assertAnalyzesTo(cz, "hradu", new String[] { "hrad" });
    assertAnalyzesTo(cz, "hrade", new String[] { "hrad" });
    assertAnalyzesTo(cz, "hradem", new String[] { "hrad" });
    assertAnalyzesTo(cz, "hrady", new String[] { "hrad" });
    assertAnalyzesTo(cz, "hradech", new String[] { "hrad" });
    assertAnalyzesTo(cz, "hradům", new String[] { "hrad" });
    assertAnalyzesTo(cz, "hradů", new String[] { "hrad" });
    assertAnalyzesTo(cz, "muž", new String[] { "muh" });
    assertAnalyzesTo(cz, "muži", new String[] { "muh" });
    assertAnalyzesTo(cz, "muže", new String[] { "muh" });
    assertAnalyzesTo(cz, "mužů", new String[] { "muh" });
    assertAnalyzesTo(cz, "mužům", new String[] { "muh" });
    assertAnalyzesTo(cz, "mužích", new String[] { "muh" });
    assertAnalyzesTo(cz, "mužem", new String[] { "muh" });
    assertAnalyzesTo(cz, "stroj", new String[] { "stroj" });
    assertAnalyzesTo(cz, "stroje", new String[] { "stroj" });
    assertAnalyzesTo(cz, "strojů", new String[] { "stroj" });
    assertAnalyzesTo(cz, "stroji", new String[] { "stroj" });
    assertAnalyzesTo(cz, "strojům", new String[] { "stroj" });
    assertAnalyzesTo(cz, "strojích", new String[] { "stroj" });
    assertAnalyzesTo(cz, "strojem", new String[] { "stroj" });
    assertAnalyzesTo(cz, "předseda", new String[] { "předsd" });
    assertAnalyzesTo(cz, "předsedové", new String[] { "předsd" });
    assertAnalyzesTo(cz, "předsedy", new String[] { "předsd" });
    assertAnalyzesTo(cz, "předsedů", new String[] { "předsd" });
    assertAnalyzesTo(cz, "předsedovi", new String[] { "předsd" });
    assertAnalyzesTo(cz, "předsedům", new String[] { "předsd" });
    assertAnalyzesTo(cz, "předsedu", new String[] { "předsd" });
    assertAnalyzesTo(cz, "předsedo", new String[] { "předsd" });
    assertAnalyzesTo(cz, "předsedech", new String[] { "předsd" });
    assertAnalyzesTo(cz, "předsedou", new String[] { "předsd" });
    assertAnalyzesTo(cz, "soudce", new String[] { "soudk" });
    assertAnalyzesTo(cz, "soudci", new String[] { "soudk" });
    assertAnalyzesTo(cz, "soudců", new String[] { "soudk" });
    assertAnalyzesTo(cz, "soudcům", new String[] { "soudk" });
    assertAnalyzesTo(cz, "soudcích", new String[] { "soudk" });
    assertAnalyzesTo(cz, "soudcem", new String[] { "soudk" });
  }
  public void testFeminineNouns() throws IOException {
    CzechAnalyzer cz = new CzechAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(cz, "kost", new String[] { "kost" });
    assertAnalyzesTo(cz, "kosti", new String[] { "kost" });
    assertAnalyzesTo(cz, "kostí", new String[] { "kost" });
    assertAnalyzesTo(cz, "kostem", new String[] { "kost" });
    assertAnalyzesTo(cz, "kostech", new String[] { "kost" });
    assertAnalyzesTo(cz, "kostmi", new String[] { "kost" });
    assertAnalyzesTo(cz, "píseň", new String[] { "písň" });
    assertAnalyzesTo(cz, "písně", new String[] { "písn" });
    assertAnalyzesTo(cz, "písni", new String[] { "písn" });
    assertAnalyzesTo(cz, "písněmi", new String[] { "písn" });
    assertAnalyzesTo(cz, "písních", new String[] { "písn" });
    assertAnalyzesTo(cz, "písním", new String[] { "písn" });
    assertAnalyzesTo(cz, "růže", new String[] { "růh" });
    assertAnalyzesTo(cz, "růží", new String[] { "růh" });
    assertAnalyzesTo(cz, "růžím", new String[] { "růh" });
    assertAnalyzesTo(cz, "růžích", new String[] { "růh" });
    assertAnalyzesTo(cz, "růžemi", new String[] { "růh" });
    assertAnalyzesTo(cz, "růži", new String[] { "růh" });
    assertAnalyzesTo(cz, "žena", new String[] { "žn" });
    assertAnalyzesTo(cz, "ženy", new String[] { "žn" });
    assertAnalyzesTo(cz, "žen", new String[] { "žn" });
    assertAnalyzesTo(cz, "ženě", new String[] { "žn" });
    assertAnalyzesTo(cz, "ženám", new String[] { "žn" });
    assertAnalyzesTo(cz, "ženu", new String[] { "žn" });
    assertAnalyzesTo(cz, "ženo", new String[] { "žn" });
    assertAnalyzesTo(cz, "ženách", new String[] { "žn" });
    assertAnalyzesTo(cz, "ženou", new String[] { "žn" });
    assertAnalyzesTo(cz, "ženami", new String[] { "žn" });
  }
  public void testNeuterNouns() throws IOException {
    CzechAnalyzer cz = new CzechAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(cz, "město", new String[] { "měst" });
    assertAnalyzesTo(cz, "města", new String[] { "měst" });
    assertAnalyzesTo(cz, "měst", new String[] { "měst" });
    assertAnalyzesTo(cz, "městu", new String[] { "měst" });
    assertAnalyzesTo(cz, "městům", new String[] { "měst" });
    assertAnalyzesTo(cz, "městě", new String[] { "měst" });
    assertAnalyzesTo(cz, "městech", new String[] { "měst" });
    assertAnalyzesTo(cz, "městem", new String[] { "měst" });
    assertAnalyzesTo(cz, "městy", new String[] { "měst" });
    assertAnalyzesTo(cz, "moře", new String[] { "moř" });
    assertAnalyzesTo(cz, "moří", new String[] { "moř" });
    assertAnalyzesTo(cz, "mořím", new String[] { "moř" });
    assertAnalyzesTo(cz, "moři", new String[] { "moř" });
    assertAnalyzesTo(cz, "mořích", new String[] { "moř" });
    assertAnalyzesTo(cz, "mořem", new String[] { "moř" });
    assertAnalyzesTo(cz, "kuře", new String[] { "kuř" });
    assertAnalyzesTo(cz, "kuřata", new String[] { "kuř" });
    assertAnalyzesTo(cz, "kuřete", new String[] { "kuř" });
    assertAnalyzesTo(cz, "kuřat", new String[] { "kuř" });
    assertAnalyzesTo(cz, "kuřeti", new String[] { "kuř" });
    assertAnalyzesTo(cz, "kuřatům", new String[] { "kuř" });
    assertAnalyzesTo(cz, "kuřatech", new String[] { "kuř" });
    assertAnalyzesTo(cz, "kuřetem", new String[] { "kuř" });
    assertAnalyzesTo(cz, "kuřaty", new String[] { "kuř" });
    assertAnalyzesTo(cz, "stavení", new String[] { "stavn" });
    assertAnalyzesTo(cz, "stavením", new String[] { "stavn" });
    assertAnalyzesTo(cz, "staveních", new String[] { "stavn" });
    assertAnalyzesTo(cz, "staveními", new String[] { "stavn" });    
  }
  public void testAdjectives() throws IOException {
    CzechAnalyzer cz = new CzechAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(cz, "mladý", new String[] { "mlad" });
    assertAnalyzesTo(cz, "mladí", new String[] { "mlad" });
    assertAnalyzesTo(cz, "mladého", new String[] { "mlad" });
    assertAnalyzesTo(cz, "mladých", new String[] { "mlad" });
    assertAnalyzesTo(cz, "mladému", new String[] { "mlad" });
    assertAnalyzesTo(cz, "mladým", new String[] { "mlad" });
    assertAnalyzesTo(cz, "mladé", new String[] { "mlad" });
    assertAnalyzesTo(cz, "mladém", new String[] { "mlad" });
    assertAnalyzesTo(cz, "mladými", new String[] { "mlad" }); 
    assertAnalyzesTo(cz, "mladá", new String[] { "mlad" });
    assertAnalyzesTo(cz, "mladou", new String[] { "mlad" });
    assertAnalyzesTo(cz, "jarní", new String[] { "jarn" });
    assertAnalyzesTo(cz, "jarního", new String[] { "jarn" });
    assertAnalyzesTo(cz, "jarních", new String[] { "jarn" });
    assertAnalyzesTo(cz, "jarnímu", new String[] { "jarn" });
    assertAnalyzesTo(cz, "jarním", new String[] { "jarn" });
    assertAnalyzesTo(cz, "jarními", new String[] { "jarn" });  
  }
  public void testPossessive() throws IOException {
    CzechAnalyzer cz = new CzechAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(cz, "Karlův", new String[] { "karl" });
    assertAnalyzesTo(cz, "jazykový", new String[] { "jazyk" });
  }
  public void testExceptions() throws IOException {
    CzechAnalyzer cz = new CzechAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(cz, "český", new String[] { "česk" });
    assertAnalyzesTo(cz, "čeští", new String[] { "česk" });
    assertAnalyzesTo(cz, "anglický", new String[] { "anglick" });
    assertAnalyzesTo(cz, "angličtí", new String[] { "anglick" });
    assertAnalyzesTo(cz, "kniha", new String[] { "knih" });
    assertAnalyzesTo(cz, "knize", new String[] { "knih" });
    assertAnalyzesTo(cz, "mazat", new String[] { "mah" });
    assertAnalyzesTo(cz, "mažu", new String[] { "mah" });
    assertAnalyzesTo(cz, "kluk", new String[] { "kluk" });
    assertAnalyzesTo(cz, "kluci", new String[] { "kluk" });
    assertAnalyzesTo(cz, "klucích", new String[] { "kluk" });
    assertAnalyzesTo(cz, "hezký", new String[] { "hezk" });
    assertAnalyzesTo(cz, "hezčí", new String[] { "hezk" });
    assertAnalyzesTo(cz, "hůl", new String[] { "hol" });
    assertAnalyzesTo(cz, "hole", new String[] { "hol" });
    assertAnalyzesTo(cz, "deska", new String[] { "desk" });
    assertAnalyzesTo(cz, "desek", new String[] { "desk" });
  }
  public void testDontStem() throws IOException {
    CzechAnalyzer cz = new CzechAnalyzer(TEST_VERSION_CURRENT);
    assertAnalyzesTo(cz, "e", new String[] { "e" });
    assertAnalyzesTo(cz, "zi", new String[] { "zi" });
  }
  public void testWithKeywordAttribute() throws IOException {
    CharArraySet set = new CharArraySet(TEST_VERSION_CURRENT, 1, true);
    set.add("hole");
    CzechStemFilter filter = new CzechStemFilter(new KeywordMarkerTokenFilter(
        new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader("hole desek")), set));
    assertTokenStreamContents(filter, new String[] { "hole", "desk" });
  }
}
