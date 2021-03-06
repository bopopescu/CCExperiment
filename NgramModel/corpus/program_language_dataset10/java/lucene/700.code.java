package org.apache.lucene.analysis.query;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.util.StringHelper;
import org.apache.lucene.util.Version;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
public final class QueryAutoStopWordAnalyzer extends Analyzer {
  Analyzer delegate;
  HashMap<String,HashSet<String>> stopWordsPerField = new HashMap<String,HashSet<String>>();
  public static final float defaultMaxDocFreqPercent = 0.4f;
  private final Version matchVersion;
  public QueryAutoStopWordAnalyzer(Version matchVersion, Analyzer delegate) {
    this.delegate = delegate;
    this.matchVersion = matchVersion;
  }
  public int addStopWords(IndexReader reader) throws IOException {
    return addStopWords(reader, defaultMaxDocFreqPercent);
  }
  public int addStopWords(IndexReader reader, int maxDocFreq) throws IOException {
    int numStopWords = 0;
    Collection<String> fieldNames = reader.getFieldNames(IndexReader.FieldOption.INDEXED);
    for (Iterator<String> iter = fieldNames.iterator(); iter.hasNext();) {
      String fieldName = iter.next();
      numStopWords += addStopWords(reader, fieldName, maxDocFreq);
    }
    return numStopWords;
  }
  public int addStopWords(IndexReader reader, float maxPercentDocs) throws IOException {
    int numStopWords = 0;
    Collection<String> fieldNames = reader.getFieldNames(IndexReader.FieldOption.INDEXED);
    for (Iterator<String> iter = fieldNames.iterator(); iter.hasNext();) {
      String fieldName = iter.next();
      numStopWords += addStopWords(reader, fieldName, maxPercentDocs);
    }
    return numStopWords;
  }
  public int addStopWords(IndexReader reader, String fieldName, float maxPercentDocs) throws IOException {
    return addStopWords(reader, fieldName, (int) (reader.numDocs() * maxPercentDocs));
  }
  public int addStopWords(IndexReader reader, String fieldName, int maxDocFreq) throws IOException {
    HashSet<String> stopWords = new HashSet<String>();
    String internedFieldName = StringHelper.intern(fieldName);
    TermEnum te = reader.terms(new Term(fieldName));
    Term term = te.term();
    while (term != null) {
      if (term.field() != internedFieldName) {
        break;
      }
      if (te.docFreq() > maxDocFreq) {
        stopWords.add(term.text());
      }
      if (!te.next()) {
        break;
      }
      term = te.term();
    }
    stopWordsPerField.put(fieldName, stopWords);
    Map<String,SavedStreams> streamMap = (Map<String,SavedStreams>) getPreviousTokenStream();
    if (streamMap != null)
      streamMap.remove(fieldName);
    return stopWords.size();
  }
  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    TokenStream result;
    try {
      result = delegate.reusableTokenStream(fieldName, reader);
    } catch (IOException e) {
      result = delegate.tokenStream(fieldName, reader);
    }
    HashSet<String> stopWords = stopWordsPerField.get(fieldName);
    if (stopWords != null) {
      result = new StopFilter(matchVersion, result, stopWords);
    }
    return result;
  }
  private class SavedStreams {
    TokenStream wrapped;
    TokenStream withStopFilter;
  }
  @Override
  public TokenStream reusableTokenStream(String fieldName, Reader reader)
      throws IOException {
    Map<String,SavedStreams> streamMap = (Map<String,SavedStreams>) getPreviousTokenStream();
    if (streamMap == null) {
      streamMap = new HashMap<String, SavedStreams>();
      setPreviousTokenStream(streamMap);
    }
    SavedStreams streams = streamMap.get(fieldName);
    if (streams == null) {
      streams = new SavedStreams();
      streamMap.put(fieldName, streams);
      streams.wrapped = delegate.reusableTokenStream(fieldName, reader);
      HashSet<String> stopWords = stopWordsPerField.get(fieldName);
      if (stopWords != null)
        streams.withStopFilter = new StopFilter(matchVersion, streams.wrapped, stopWords);
      else
        streams.withStopFilter = streams.wrapped;
    } else {
      TokenStream result = delegate.reusableTokenStream(fieldName, reader);
      if (result == streams.wrapped) {
        streams.withStopFilter.reset();
      } else {
        streams.wrapped = result;
        HashSet<String> stopWords = stopWordsPerField.get(fieldName);
        if (stopWords != null)
          streams.withStopFilter = new StopFilter(matchVersion, streams.wrapped, stopWords);
        else
          streams.withStopFilter = streams.wrapped;
      }
    }
    return streams.withStopFilter;
  }
  public String[] getStopWords(String fieldName) {
    String[] result;
    HashSet<String> stopWords = stopWordsPerField.get(fieldName);
    if (stopWords != null) {
      result = stopWords.toArray(new String[stopWords.size()]);
    } else {
      result = new String[0];
    }
    return result;
  }
  public Term[] getStopWords() {
    ArrayList<Term> allStopWords = new ArrayList<Term>();
    for (Iterator<String> iter = stopWordsPerField.keySet().iterator(); iter.hasNext();) {
      String fieldName = iter.next();
      HashSet<String> stopWords = stopWordsPerField.get(fieldName);
      for (Iterator<String> iterator = stopWords.iterator(); iterator.hasNext();) {
        String text = iterator.next();
        allStopWords.add(new Term(fieldName, text));
      }
    }
    return allStopWords.toArray(new Term[allStopWords.size()]);
	}
}
