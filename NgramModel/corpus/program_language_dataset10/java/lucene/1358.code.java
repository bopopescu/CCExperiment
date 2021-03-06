package org.apache.lucene.wordnet;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
public class AnalyzerUtil {
  private AnalyzerUtil() {}
  public static Analyzer getLoggingAnalyzer(final Analyzer child, 
      final PrintStream log, final String logName) {
    if (child == null) 
      throw new IllegalArgumentException("child analyzer must not be null");
    if (log == null) 
      throw new IllegalArgumentException("logStream must not be null");
    return new Analyzer() {
      @Override
      public TokenStream tokenStream(final String fieldName, Reader reader) {
        return new TokenFilter(child.tokenStream(fieldName, reader)) {
          private int position = -1;
          private TermAttribute termAtt = addAttribute(TermAttribute.class);
          private PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
          private OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
          private TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
          @Override
          public boolean incrementToken() throws IOException {
            boolean hasNext = input.incrementToken();
            log.println(toString(hasNext));
            return hasNext;
          }
          private String toString(boolean hasNext) {
            if (!hasNext) return "[" + logName + ":EOS:" + fieldName + "]\n";
            position += posIncrAtt.getPositionIncrement();
            return "[" + logName + ":" + position + ":" + fieldName + ":"
                + termAtt.term() + ":" + offsetAtt.startOffset()
                + "-" + offsetAtt.endOffset() + ":" + typeAtt.type()
                + "]";
          }         
        };
      }
    };
  }
  public static Analyzer getMaxTokenAnalyzer(
      final Analyzer child, final int maxTokens) {
    if (child == null) 
      throw new IllegalArgumentException("child analyzer must not be null");
    if (maxTokens < 0) 
      throw new IllegalArgumentException("maxTokens must not be negative");
    if (maxTokens == Integer.MAX_VALUE) 
      return child; 
    return new Analyzer() {
      @Override
      public TokenStream tokenStream(String fieldName, Reader reader) {
        return new TokenFilter(child.tokenStream(fieldName, reader)) {
          private int todo = maxTokens;
          @Override
          public boolean incrementToken() throws IOException {
            return --todo >= 0 ? input.incrementToken() : false;
          }
        };
      }
    };
  }
  public static Analyzer getPorterStemmerAnalyzer(final Analyzer child) {
    if (child == null) 
      throw new IllegalArgumentException("child analyzer must not be null");
    return new Analyzer() {
      @Override
      public TokenStream tokenStream(String fieldName, Reader reader) {
        return new PorterStemFilter(
            child.tokenStream(fieldName, reader));
      }
    };
  }
  public static Analyzer getSynonymAnalyzer(final Analyzer child, 
      final SynonymMap synonyms, final int maxSynonyms) {
    if (child == null) 
      throw new IllegalArgumentException("child analyzer must not be null");
    if (synonyms == null)
      throw new IllegalArgumentException("synonyms must not be null");
    if (maxSynonyms < 0) 
      throw new IllegalArgumentException("maxSynonyms must not be negative");
    if (maxSynonyms == 0)
      return child; 
    return new Analyzer() {
      @Override
      public TokenStream tokenStream(String fieldName, Reader reader) {
        return new SynonymTokenFilter(
          child.tokenStream(fieldName, reader), synonyms, maxSynonyms);
      }
    };
  }
  public static Analyzer getTokenCachingAnalyzer(final Analyzer child) {
    if (child == null)
      throw new IllegalArgumentException("child analyzer must not be null");
    return new Analyzer() {
      private final HashMap<String,ArrayList<AttributeSource.State>> cache = new HashMap<String,ArrayList<AttributeSource.State>>();
      @Override
      public TokenStream tokenStream(String fieldName, Reader reader) {
        final ArrayList<AttributeSource.State> tokens = cache.get(fieldName);
        if (tokens == null) { 
          final ArrayList<AttributeSource.State> tokens2 = new ArrayList<AttributeSource.State>();
          TokenStream tokenStream = new TokenFilter(child.tokenStream(fieldName, reader)) {
            @Override
            public boolean incrementToken() throws IOException {
              boolean hasNext = input.incrementToken();
              if (hasNext) tokens2.add(captureState());
              return hasNext;
            }
          };
          cache.put(fieldName, tokens2);
          return tokenStream;
        } else { 
          return new TokenStream() {
            private Iterator<AttributeSource.State> iter = tokens.iterator();
            @Override
            public boolean incrementToken() {
              if (!iter.hasNext()) return false;
              restoreState(iter.next());
              return true;
            }
          };
        }
      }
    };
  }
  public static String[] getMostFrequentTerms(Analyzer analyzer, String text, int limit) {
    if (analyzer == null) 
      throw new IllegalArgumentException("analyzer must not be null");
    if (text == null) 
      throw new IllegalArgumentException("text must not be null");
    if (limit <= 0) limit = Integer.MAX_VALUE;
    HashMap<String,MutableInteger> map = new HashMap<String,MutableInteger>();
    TokenStream stream = analyzer.tokenStream("", new StringReader(text));
    TermAttribute termAtt = stream.addAttribute(TermAttribute.class);
    try {
      while (stream.incrementToken()) {
        MutableInteger freq = map.get(termAtt.term());
        if (freq == null) {
          freq = new MutableInteger(1);
          map.put(termAtt.term(), freq);
        } else {
          freq.setValue(freq.intValue() + 1);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        stream.close();
      } catch (IOException e2) {
        throw new RuntimeException(e2);
      }
    }
    Map.Entry<String,MutableInteger>[] entries = new Map.Entry[map.size()];
    map.entrySet().toArray(entries);
    Arrays.sort(entries, new Comparator<Map.Entry<String,MutableInteger>>() {
      public int compare(Map.Entry<String,MutableInteger> e1, Map.Entry<String,MutableInteger> e2) {
        int f1 = e1.getValue().intValue();
        int f2 = e2.getValue().intValue();
        if (f2 - f1 != 0) return f2 - f1;
        String s1 = e1.getKey();
        String s2 = e2.getKey();
        return s1.compareTo(s2);
      }
    });
    int size = Math.min(limit, entries.length);
    String[] pairs = new String[size];
    for (int i=0; i < size; i++) {
      pairs[i] = entries[i].getValue() + ":" + entries[i].getKey();
    }
    return pairs;
  }
  private static final class MutableInteger {
    private int value;
    public MutableInteger(int value) { this.value = value; }
    public int intValue() { return value; }
    public void setValue(int value) { this.value = value; }
    @Override
    public String toString() { return String.valueOf(value); }
  }
  private static final Pattern PARAGRAPHS = Pattern.compile("([\\r\\n\\u0085\\u2028\\u2029][ \\t\\x0B\\f]*){2,}");
  public static String[] getParagraphs(String text, int limit) {
    return tokenize(PARAGRAPHS, text, limit);
  }
  private static String[] tokenize(Pattern pattern, String text, int limit) {
    String[] tokens = pattern.split(text, limit);
    for (int i=tokens.length; --i >= 0; ) tokens[i] = tokens[i].trim();
    return tokens;
  }
  public static String[] getSentences(String text, int limit) {
    int len = text.length();
    if (len == 0) return new String[] { text };
    if (limit <= 0) limit = Integer.MAX_VALUE;
    String[] tokens = new String[Math.min(limit, 1 + len/40)];
    int size = 0;
    int i = 0;
    while (i < len && size < limit) {
      int start = i;
      while (i < len && !isSentenceSeparator(text.charAt(i))) i++;
      if (size == tokens.length) { 
        String[] tmp = new String[tokens.length << 1];
        System.arraycopy(tokens, 0, tmp, 0, size);
        tokens = tmp;
      }
      tokens[size++] = text.substring(start, i).trim();
      while (i < len && isSentenceSeparator(text.charAt(i))) i++;
    }
    if (size == tokens.length) return tokens;
    String[] results = new String[size];
    System.arraycopy(tokens, 0, results, 0, size);
    return results;
  }
  private static boolean isSentenceSeparator(char c) {
    switch (c) {
      case '!': return true;
      case '.': return true;
      case '?': return true;
      case 0xA1: return true; 
      case 0xBF: return true; 
      default: return false;
    }   
  }
}
