package org.apache.solr.analysis;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.common.util.StrUtils;
import org.apache.solr.util.plugin.ResourceLoaderAware;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.CharArraySet;
import java.util.HashSet;
import java.util.List;
import java.io.File;
import java.util.Set;
import java.io.File;
import java.io.IOException;
public class StopFilterFactory extends BaseTokenFilterFactory implements ResourceLoaderAware {
  public void inform(ResourceLoader loader) {
    String stopWordFiles = args.get("words");
    ignoreCase = getBoolean("ignoreCase",false);
    enablePositionIncrements = getBoolean("enablePositionIncrements",false);
    if (stopWordFiles != null) {
      try {
        List<String> files = StrUtils.splitFileNames(stopWordFiles);
          if (stopWords == null && files.size() > 0){
            stopWords = new CharArraySet(files.size() * 10, ignoreCase);
          }
          for (String file : files) {
            List<String> wlist = loader.getLines(file.trim());
            stopWords.addAll(StopFilter.makeStopSet(wlist, ignoreCase));
          }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      stopWords = new CharArraySet(StopAnalyzer.ENGLISH_STOP_WORDS_SET, ignoreCase);
    }
  }
  private CharArraySet stopWords;
  private boolean ignoreCase;
  private boolean enablePositionIncrements;
  public boolean isEnablePositionIncrements() {
    return enablePositionIncrements;
  }
  public boolean isIgnoreCase() {
    return ignoreCase;
  }
  public Set<?> getStopWords() {
    return stopWords;
  }
  public StopFilter create(TokenStream input) {
    assureMatchVersion();
    StopFilter stopFilter = new StopFilter(luceneMatchVersion,input,stopWords,ignoreCase);
    stopFilter.setEnablePositionIncrements(enablePositionIncrements);
    return stopFilter;
  }
}
