package org.apache.lucene.analysis.cn.smart;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.cn.smart.SentenceTokenizer;
import org.apache.lucene.analysis.cn.smart.WordTokenFilter;
import org.apache.lucene.util.Version;
public final class SmartChineseAnalyzer extends Analyzer {
  private final Set<?> stopWords;
  private static final String DEFAULT_STOPWORD_FILE = "stopwords.txt";
  private static final String STOPWORD_FILE_COMMENT = "//";
  public static Set<String> getDefaultStopSet(){
    return DefaultSetHolder.DEFAULT_STOP_SET;
  }
  private static class DefaultSetHolder {
    static final Set<String> DEFAULT_STOP_SET;
    static {
      try {
        DEFAULT_STOP_SET = loadDefaultStopWordSet();
      } catch (IOException ex) {
        throw new RuntimeException("Unable to load default stopword set");
      }
    }
    static Set<String> loadDefaultStopWordSet() throws IOException {
      InputStream stream = SmartChineseAnalyzer.class
          .getResourceAsStream(DEFAULT_STOPWORD_FILE);
      try {
        InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
        return Collections.unmodifiableSet(WordlistLoader.getWordSet(reader, STOPWORD_FILE_COMMENT));
      } finally {
        stream.close();
      }
    }
  }
  private final Version matchVersion;
  public SmartChineseAnalyzer(Version matchVersion) {
    this(matchVersion, true);
  }
  public SmartChineseAnalyzer(Version matchVersion, boolean useDefaultStopWords) {
    stopWords = useDefaultStopWords ? DefaultSetHolder.DEFAULT_STOP_SET
      : Collections.EMPTY_SET;
    this.matchVersion = matchVersion;
  }
  public SmartChineseAnalyzer(Version matchVersion, Set stopWords) {
    this.stopWords = stopWords==null?Collections.EMPTY_SET:stopWords;
    this.matchVersion = matchVersion;
  }
  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    TokenStream result = new SentenceTokenizer(reader);
    result = new WordTokenFilter(result);
    result = new PorterStemFilter(result);
    if (!stopWords.isEmpty()) {
      result = new StopFilter(matchVersion, result, stopWords, false);
    }
    return result;
  }
  private static final class SavedStreams {
    Tokenizer tokenStream;
    TokenStream filteredTokenStream;
  }
  @Override
  public TokenStream reusableTokenStream(String fieldName, Reader reader)
      throws IOException {
    SavedStreams streams = (SavedStreams) getPreviousTokenStream();
    if (streams == null) {
      streams = new SavedStreams();
      setPreviousTokenStream(streams);
      streams.tokenStream = new SentenceTokenizer(reader);
      streams.filteredTokenStream = new WordTokenFilter(streams.tokenStream);
      streams.filteredTokenStream = new PorterStemFilter(streams.filteredTokenStream);
      if (!stopWords.isEmpty()) {
        streams.filteredTokenStream = new StopFilter(matchVersion, streams.filteredTokenStream, stopWords, false);
      }
    } else {
      streams.tokenStream.reset(reader);
      streams.filteredTokenStream.reset(); 
    }
    return streams.filteredTokenStream;
  }
}
