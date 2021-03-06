package org.apache.lucene.analysis.cz;
import org.apache.lucene.analysis.ReusableAnalyzerBase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
public final class CzechAnalyzer extends ReusableAnalyzerBase {
	@Deprecated
	public final static String[] CZECH_STOP_WORDS = {
        "a","s","k","o","i","u","v","z","dnes","cz","t\u00edmto","bude\u0161","budem",
        "byli","jse\u0161","m\u016fj","sv\u00fdm","ta","tomto","tohle","tuto","tyto",
        "jej","zda","pro\u010d","m\u00e1te","tato","kam","tohoto","kdo","kte\u0159\u00ed",
        "mi","n\u00e1m","tom","tomuto","m\u00edt","nic","proto","kterou","byla",
        "toho","proto\u017ee","asi","ho","na\u0161i","napi\u0161te","re","co\u017e","t\u00edm",
        "tak\u017ee","sv\u00fdch","jej\u00ed","sv\u00fdmi","jste","aj","tu","tedy","teto",
        "bylo","kde","ke","prav\u00e9","ji","nad","nejsou","\u010di","pod","t\u00e9ma",
        "mezi","p\u0159es","ty","pak","v\u00e1m","ani","kdy\u017e","v\u0161ak","neg","jsem",
        "tento","\u010dl\u00e1nku","\u010dl\u00e1nky","aby","jsme","p\u0159ed","pta","jejich",
        "byl","je\u0161t\u011b","a\u017e","bez","tak\u00e9","pouze","prvn\u00ed","va\u0161e","kter\u00e1",
        "n\u00e1s","nov\u00fd","tipy","pokud","m\u016f\u017ee","strana","jeho","sv\u00e9","jin\u00e9",
        "zpr\u00e1vy","nov\u00e9","nen\u00ed","v\u00e1s","jen","podle","zde","u\u017e","b\u00fdt","v\u00edce",
        "bude","ji\u017e","ne\u017e","kter\u00fd","by","kter\u00e9","co","nebo","ten","tak",
        "m\u00e1","p\u0159i","od","po","jsou","jak","dal\u0161\u00ed","ale","si","se","ve",
        "to","jako","za","zp\u011bt","ze","do","pro","je","na","atd","atp",
        "jakmile","p\u0159i\u010dem\u017e","j\u00e1","on","ona","ono","oni","ony","my","vy",
        "j\u00ed","ji","m\u011b","mne","jemu","tomu","t\u011bm","t\u011bmu","n\u011bmu","n\u011bmu\u017e",
        "jeho\u017e","j\u00ed\u017e","jeliko\u017e","je\u017e","jako\u017e","na\u010de\u017e",
    };
	public static final Set<?> getDefaultStopSet(){
	  return DefaultSetHolder.DEFAULT_SET;
	}
	private static class DefaultSetHolder {
	  private static final Set<?> DEFAULT_SET = CharArraySet.unmodifiableSet(new CharArraySet(
	      Version.LUCENE_CURRENT, Arrays.asList(CZECH_STOP_WORDS), false));
	}
	private Set<?> stoptable;
  private final Version matchVersion;
  private final Set<?> stemExclusionTable;
	public CzechAnalyzer(Version matchVersion) {
    this(matchVersion, DefaultSetHolder.DEFAULT_SET);
	}
  public CzechAnalyzer(Version matchVersion, Set<?> stopwords) {
    this(matchVersion, stopwords, CharArraySet.EMPTY_SET);
  }
  public CzechAnalyzer(Version matchVersion, Set<?> stopwords, Set<?> stemExclusionTable) {
    this.matchVersion = matchVersion;
    this.stoptable = CharArraySet.unmodifiableSet(CharArraySet.copy(matchVersion, stopwords));
    this.stemExclusionTable = CharArraySet.unmodifiableSet(CharArraySet.copy(matchVersion, stemExclusionTable));
  }
  @Deprecated
  public CzechAnalyzer(Version matchVersion, String... stopwords) {
    this(matchVersion, StopFilter.makeStopSet( matchVersion, stopwords ));
	}
  @Deprecated
  public CzechAnalyzer(Version matchVersion, HashSet<?> stopwords) {
    this(matchVersion, (Set<?>)stopwords);
	}
  @Deprecated
  public CzechAnalyzer(Version matchVersion, File stopwords ) throws IOException {
    this(matchVersion, (Set<?>)WordlistLoader.getWordSet( stopwords ));
	}
    @Deprecated
    public void loadStopWords( InputStream wordfile, String encoding ) {
        setPreviousTokenStream(null); 
        if ( wordfile == null ) {
            stoptable = Collections.emptySet();
            return;
        }
        try {
            stoptable = Collections.emptySet();
            InputStreamReader isr;
            if (encoding == null)
                isr = new InputStreamReader(wordfile);
            else
                isr = new InputStreamReader(wordfile, encoding);
            stoptable = WordlistLoader.getWordSet(isr);
        } catch ( IOException e ) {
          stoptable = Collections.emptySet();
        }
    }
  @Override
  protected TokenStreamComponents createComponents(String fieldName,
      Reader reader) {
    final Tokenizer source = new StandardTokenizer(matchVersion, reader);
    TokenStream result = new StandardFilter(source);
    result = new LowerCaseFilter(matchVersion, result);
    result = new StopFilter( matchVersion, result, stoptable);
    if (matchVersion.onOrAfter(Version.LUCENE_31)) {
      if(!this.stemExclusionTable.isEmpty())
        result = new KeywordMarkerTokenFilter(result, stemExclusionTable);
      result = new CzechStemFilter(result);
    }
    return new TokenStreamComponents(source, result);
  }
}
