package org.apache.lucene.analysis.br;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
public final class BrazilianAnalyzer extends StopwordAnalyzerBase {
	@Deprecated
	public final static String[] BRAZILIAN_STOP_WORDS = {
      "a","ainda","alem","ambas","ambos","antes",
      "ao","aonde","aos","apos","aquele","aqueles",
      "as","assim","com","como","contra","contudo",
      "cuja","cujas","cujo","cujos","da","das","de",
      "dela","dele","deles","demais","depois","desde",
      "desta","deste","dispoe","dispoem","diversa",
      "diversas","diversos","do","dos","durante","e",
      "ela","elas","ele","eles","em","entao","entre",
      "essa","essas","esse","esses","esta","estas",
      "este","estes","ha","isso","isto","logo","mais",
      "mas","mediante","menos","mesma","mesmas","mesmo",
      "mesmos","na","nas","nao","nas","nem","nesse","neste",
      "nos","o","os","ou","outra","outras","outro","outros",
      "pelas","pelas","pelo","pelos","perante","pois","por",
      "porque","portanto","proprio","propios","quais","qual",
      "qualquer","quando","quanto","que","quem","quer","se",
      "seja","sem","sendo","seu","seus","sob","sobre","sua",
      "suas","tal","tambem","teu","teus","toda","todas","todo",
      "todos","tua","tuas","tudo","um","uma","umas","uns"};
  public static Set<?> getDefaultStopSet(){
    return DefaultSetHolder.DEFAULT_STOP_SET;
  }
  private static class DefaultSetHolder {
    static final Set<?> DEFAULT_STOP_SET = CharArraySet
        .unmodifiableSet(new CharArraySet(Version.LUCENE_CURRENT, 
            Arrays.asList(BRAZILIAN_STOP_WORDS), false));
  }
	private Set<?> excltable = Collections.emptySet();
	public BrazilianAnalyzer(Version matchVersion) {
    this(matchVersion, DefaultSetHolder.DEFAULT_STOP_SET);
	}
  public BrazilianAnalyzer(Version matchVersion, Set<?> stopwords) {
     super(matchVersion, stopwords);
  }
  public BrazilianAnalyzer(Version matchVersion, Set<?> stopwords,
      Set<?> stemExclusionSet) {
    this(matchVersion, stopwords);
    excltable = CharArraySet.unmodifiableSet(CharArraySet
        .copy(matchVersion, stemExclusionSet));
  }
  @Deprecated
  public BrazilianAnalyzer(Version matchVersion, String... stopwords) {
    this(matchVersion, StopFilter.makeStopSet(matchVersion, stopwords));
  }
  @Deprecated
  public BrazilianAnalyzer(Version matchVersion, Map<?,?> stopwords) {
    this(matchVersion, stopwords.keySet());
  }
  @Deprecated
  public BrazilianAnalyzer(Version matchVersion, File stopwords)
      throws IOException {
    this(matchVersion, WordlistLoader.getWordSet(stopwords));
  }
	@Deprecated
	public void setStemExclusionTable( String... exclusionlist ) {
		excltable = StopFilter.makeStopSet( matchVersion, exclusionlist );
		setPreviousTokenStream(null); 
	}
	@Deprecated
	public void setStemExclusionTable( Map<?,?> exclusionlist ) {
		excltable = new HashSet<Object>(exclusionlist.keySet());
		setPreviousTokenStream(null); 
	}
	@Deprecated
	public void setStemExclusionTable( File exclusionlist ) throws IOException {
		excltable = WordlistLoader.getWordSet( exclusionlist );
		setPreviousTokenStream(null); 
	}
  @Override
  protected TokenStreamComponents createComponents(String fieldName,
      Reader reader) {
    Tokenizer source = new StandardTokenizer(matchVersion, reader);
    TokenStream result = new LowerCaseFilter(matchVersion, source);
    result = new StandardFilter(result);
    result = new StopFilter(matchVersion, result, stopwords);
    if(excltable != null && !excltable.isEmpty())
      result = new KeywordMarkerTokenFilter(result, excltable);
    return new TokenStreamComponents(source, new BrazilianStemFilter(result));
  }
}
