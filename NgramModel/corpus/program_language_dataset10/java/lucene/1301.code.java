package org.apache.lucene.search.spell;
import java.io.IOException;
import java.util.Iterator;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogMergePolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
public class SpellChecker implements java.io.Closeable {
  public static final String F_WORD = "word";
  private static final Term F_WORD_TERM = new Term(F_WORD);
  Directory spellIndex;
  private float bStart = 2.0f;
  private float bEnd = 1.0f;
  private IndexSearcher searcher;
  private final Object searcherLock = new Object();
  private final Object modifyCurrentIndexLock = new Object();
  private volatile boolean closed = false;
  private float minScore = 0.5f;
  private StringDistance sd;
  public SpellChecker(Directory spellIndex, StringDistance sd) throws IOException {
    setSpellIndex(spellIndex);
    setStringDistance(sd);
  }
  public SpellChecker(Directory spellIndex) throws IOException {
    this(spellIndex, new LevensteinDistance());
  }
  public void setSpellIndex(Directory spellIndexDir) throws IOException {
    synchronized (modifyCurrentIndexLock) {
      ensureOpen();
      if (!IndexReader.indexExists(spellIndexDir)) {
          IndexWriter writer = new IndexWriter(spellIndexDir,
            new IndexWriterConfig(Version.LUCENE_CURRENT,
                new WhitespaceAnalyzer(Version.LUCENE_CURRENT)));
          writer.close();
      }
      swapSearcher(spellIndexDir);
    }
  }
  public void setStringDistance(StringDistance sd) {
    this.sd = sd;
  }
  public StringDistance getStringDistance() {
    return sd;
  }
  public void setAccuracy(float minScore) {
    this.minScore = minScore;
  }
  public String[] suggestSimilar(String word, int numSug) throws IOException {
    return this.suggestSimilar(word, numSug, null, null, false);
  }
  public String[] suggestSimilar(String word, int numSug, IndexReader ir,
      String field, boolean morePopular) throws IOException {
    final IndexSearcher indexSearcher = obtainSearcher();
    try{
      float min = this.minScore;
      final int lengthWord = word.length();
      final int freq = (ir != null && field != null) ? ir.docFreq(new Term(field, word)) : 0;
      final int goalFreq = (morePopular && ir != null && field != null) ? freq : 0;
      if (!morePopular && freq > 0) {
        return new String[] { word };
      }
      BooleanQuery query = new BooleanQuery();
      String[] grams;
      String key;
      for (int ng = getMin(lengthWord); ng <= getMax(lengthWord); ng++) {
        key = "gram" + ng; 
        grams = formGrams(word, ng); 
        if (grams.length == 0) {
          continue; 
        }
        if (bStart > 0) { 
          add(query, "start" + ng, grams[0], bStart); 
        }
        if (bEnd > 0) { 
          add(query, "end" + ng, grams[grams.length - 1], bEnd); 
        }
        for (int i = 0; i < grams.length; i++) {
          add(query, key, grams[i]);
        }
      }
      int maxHits = 10 * numSug;
      ScoreDoc[] hits = indexSearcher.search(query, null, maxHits).scoreDocs;
      SuggestWordQueue sugQueue = new SuggestWordQueue(numSug);
      int stop = Math.min(hits.length, maxHits);
      SuggestWord sugWord = new SuggestWord();
      for (int i = 0; i < stop; i++) {
        sugWord.string = indexSearcher.doc(hits[i].doc).get(F_WORD); 
        if (sugWord.string.equals(word)) {
          continue;
        }
        sugWord.score = sd.getDistance(word,sugWord.string);
        if (sugWord.score < min) {
          continue;
        }
        if (ir != null && field != null) { 
          sugWord.freq = ir.docFreq(new Term(field, sugWord.string)); 
          if ((morePopular && goalFreq > sugWord.freq) || sugWord.freq < 1) {
            continue;
          }
        }
        sugQueue.insertWithOverflow(sugWord);
        if (sugQueue.size() == numSug) {
          min = sugQueue.top().score;
        }
        sugWord = new SuggestWord();
      }
      String[] list = new String[sugQueue.size()];
      for (int i = sugQueue.size() - 1; i >= 0; i--) {
        list[i] = sugQueue.pop().string;
      }
      return list;
    } finally {
      releaseSearcher(indexSearcher);
    }
  }
  private static void add(BooleanQuery q, String name, String value, float boost) {
    Query tq = new TermQuery(new Term(name, value));
    tq.setBoost(boost);
    q.add(new BooleanClause(tq, BooleanClause.Occur.SHOULD));
  }
  private static void add(BooleanQuery q, String name, String value) {
    q.add(new BooleanClause(new TermQuery(new Term(name, value)), BooleanClause.Occur.SHOULD));
  }
  private static String[] formGrams(String text, int ng) {
    int len = text.length();
    String[] res = new String[len - ng + 1];
    for (int i = 0; i < len - ng + 1; i++) {
      res[i] = text.substring(i, i + ng);
    }
    return res;
  }
  public void clearIndex() throws IOException {
    synchronized (modifyCurrentIndexLock) {
      ensureOpen();
      final Directory dir = this.spellIndex;
      final IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
          Version.LUCENE_CURRENT,
          new WhitespaceAnalyzer(Version.LUCENE_CURRENT))
          .setOpenMode(OpenMode.CREATE));
      writer.close();
      swapSearcher(dir);
    }
  }
  public boolean exist(String word) throws IOException {
    final IndexSearcher indexSearcher = obtainSearcher();
    try{
      return indexSearcher.docFreq(F_WORD_TERM.createTerm(word)) > 0;
    } finally {
      releaseSearcher(indexSearcher);
    }
  }
  public void indexDictionary(Dictionary dict, int mergeFactor, int ramMB) throws IOException {
    synchronized (modifyCurrentIndexLock) {
      ensureOpen();
      final Directory dir = this.spellIndex;
      final IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(Version.LUCENE_CURRENT, new WhitespaceAnalyzer(Version.LUCENE_CURRENT)).setRAMBufferSizeMB(ramMB));
      ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(mergeFactor);
      Iterator<String> iter = dict.getWordsIterator();
      while (iter.hasNext()) {
        String word = iter.next();
        int len = word.length();
        if (len < 3) {
          continue; 
        }
        if (this.exist(word)) { 
          continue;
        }
        Document doc = createDocument(word, getMin(len), getMax(len));
        writer.addDocument(doc);
      }
      writer.optimize();
      writer.close();
      swapSearcher(dir);
    }
  }
  public void indexDictionary(Dictionary dict) throws IOException {
    indexDictionary(dict, 300, 10);
  }
  private static int getMin(int l) {
    if (l > 5) {
      return 3;
    }
    if (l == 5) {
      return 2;
    }
    return 1;
  }
  private static int getMax(int l) {
    if (l > 5) {
      return 4;
    }
    if (l == 5) {
      return 3;
    }
    return 2;
  }
  private static Document createDocument(String text, int ng1, int ng2) {
    Document doc = new Document();
    doc.add(new Field(F_WORD, text, Field.Store.YES, Field.Index.NOT_ANALYZED)); 
    addGram(text, doc, ng1, ng2);
    return doc;
  }
  private static void addGram(String text, Document doc, int ng1, int ng2) {
    int len = text.length();
    for (int ng = ng1; ng <= ng2; ng++) {
      String key = "gram" + ng;
      String end = null;
      for (int i = 0; i < len - ng + 1; i++) {
        String gram = text.substring(i, i + ng);
        doc.add(new Field(key, gram, Field.Store.NO, Field.Index.NOT_ANALYZED));
        if (i == 0) {
          doc.add(new Field("start" + ng, gram, Field.Store.NO, Field.Index.NOT_ANALYZED));
        }
        end = gram;
      }
      if (end != null) { 
        doc.add(new Field("end" + ng, end, Field.Store.NO, Field.Index.NOT_ANALYZED));
      }
    }
  }
  private IndexSearcher obtainSearcher() {
    synchronized (searcherLock) {
      ensureOpen();
      searcher.getIndexReader().incRef();
      return searcher;
    }
  }
  private void releaseSearcher(final IndexSearcher aSearcher) throws IOException{
      aSearcher.getIndexReader().decRef();      
  }
  private void ensureOpen() {
    if (closed) {
      throw new AlreadyClosedException("Spellchecker has been closed");
    }
  }
  public void close() throws IOException {
    synchronized (searcherLock) {
      ensureOpen();
      closed = true;
      if (searcher != null) {
        searcher.close();
      }
      searcher = null;
    }
  }
  private void swapSearcher(final Directory dir) throws IOException {
    final IndexSearcher indexSearcher = createSearcher(dir);
    synchronized (searcherLock) {
      if(closed){
        indexSearcher.close();
        throw new AlreadyClosedException("Spellchecker has been closed");
      }
      if (searcher != null) {
        searcher.close();
      }
      searcher = indexSearcher;
      this.spellIndex = dir;
    }
  }
  IndexSearcher createSearcher(final Directory dir) throws IOException{
    return new IndexSearcher(dir, true);
  }
  boolean isClosed(){
    return closed;
  }
}
