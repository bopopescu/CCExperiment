package lucli;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import jline.ConsoleReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
class LuceneMethods {
  private int numDocs;
  private final FSDirectory indexName; 
  private List<String> fields; 
  private List<String> indexedFields; 
  private String fieldsArray[]; 
  private Searcher searcher;
  private Query query; 
  private String analyzerClassFQN = null; 
  public LuceneMethods(String index) throws IOException {
    indexName = FSDirectory.open(new File(index));
    message("Lucene CLI. Using directory '" + indexName + "'. Type 'help' for instructions.");
  }
    private Analyzer createAnalyzer() {
        if (analyzerClassFQN == null) return new StandardAnalyzer(Version.LUCENE_CURRENT);
        try {
            return Class.forName(analyzerClassFQN).asSubclass(Analyzer.class).newInstance();
        } catch (ClassCastException cce) {
            message("Given class is not an Analyzer: " + analyzerClassFQN);
            return new StandardAnalyzer(Version.LUCENE_CURRENT);
        } catch (Exception e) {
            message("Unable to use Analyzer " + analyzerClassFQN);
            return new StandardAnalyzer(Version.LUCENE_CURRENT);
        }
    }
  public void info() throws java.io.IOException {
    IndexReader indexReader = IndexReader.open(indexName, true);
    getFieldInfo();
    numDocs = indexReader.numDocs();
    message("Index has " + numDocs + " documents ");
    message("All Fields:" + fields.toString());
    message("Indexed Fields:" + indexedFields.toString());
    if (IndexWriter.isLocked(indexName)) {
      message("Index is locked");
    }
    indexReader.close();
  }
  public void search(String queryString, boolean explain, boolean showTokens, ConsoleReader cr)
  		throws java.io.IOException, org.apache.lucene.queryParser.ParseException {
    initSearch(queryString);
    int numHits = computeCount(query);
    message(numHits + " total matching documents");
    if (explain) {
      query = explainQuery(queryString);
    }
    final int HITS_PER_PAGE = 10;
    message("--------------------------------------");
    for (int start = 0; start < numHits; start += HITS_PER_PAGE) {
      int end = Math.min(numHits, start + HITS_PER_PAGE);
      ScoreDoc[] hits = search(query, end);
      for (int ii = start; ii < end; ii++) {
        Document doc = searcher.doc(hits[ii].doc);
        message("---------------- " + (ii + 1) + " score:" + hits[ii].score + "---------------------");
        printHit(doc);
        if (showTokens) {
          invertDocument(doc);
        }
        if (explain) {
          Explanation exp = searcher.explain(query, hits[ii].doc);
          message("Explanation:" + exp.toString());
        }
      }
      message("#################################################");
      if (numHits > end) {
      	queryString = cr.readLine("more (y/n) ? ");
        if (queryString.length() == 0 || queryString.charAt(0) == 'n')
          break;
      }
    }
    searcher.close();
  }
  private void printHit(Document doc) {
    for (int ii = 0; ii < fieldsArray.length; ii++) {
      String currField = fieldsArray[ii];
      String[] result = doc.getValues(currField);
      if (result != null) {
        for (int i = 0; i < result.length; i++) {
          message(currField + ":" + result[i]);
        }
      } else {
        message(currField + ": <not available>");
      }
    }
  }
    public void optimize() throws IOException {
    IndexWriter indexWriter = new IndexWriter(indexName, new IndexWriterConfig(
        Version.LUCENE_CURRENT, createAnalyzer()).setOpenMode(
        OpenMode.APPEND));
    message("Starting to optimize index.");
    long start = System.currentTimeMillis();
    indexWriter.optimize();
    message("Done optimizing index. Took " + (System.currentTimeMillis() - start) + " msecs");
    indexWriter.close();
  }
    private Query explainQuery(String queryString) throws IOException, ParseException {
    searcher = new IndexSearcher(indexName, true);
    Analyzer analyzer = createAnalyzer();
    getFieldInfo();
    int arraySize = indexedFields.size();
    String indexedArray[] = new String[arraySize];
    for (int ii = 0; ii < arraySize; ii++) {
      indexedArray[ii] = indexedFields.get(ii);
    }
    MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_CURRENT, indexedArray, analyzer);
    query = parser.parse(queryString);
    message("Searching for: " + query.toString());
    return (query);
  }
  private void initSearch(String queryString) throws IOException, ParseException {
    searcher = new IndexSearcher(indexName, true);
    Analyzer analyzer = createAnalyzer();
    getFieldInfo();
    int arraySize = fields.size();
    fieldsArray = new String[arraySize];
    for (int ii = 0; ii < arraySize; ii++) {
      fieldsArray[ii] = fields.get(ii);
    }
    MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_CURRENT, fieldsArray, analyzer);
    query = parser.parse(queryString);
    System.out.println("Searching for: " + query.toString());
  }
  final static class CountingCollector extends Collector {
    public int numHits = 0;
    @Override
    public void setScorer(Scorer scorer) throws IOException {}
    @Override
    public void collect(int doc) throws IOException {
      numHits++;
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase) {}
    @Override
    public boolean acceptsDocsOutOfOrder() {
      return true;
    }    
  }
  private int computeCount(Query q) throws IOException {
    CountingCollector countingCollector = new CountingCollector();
    searcher.search(q, countingCollector);    
    return countingCollector.numHits;
  }
  public void count(String queryString) throws java.io.IOException, ParseException {
    initSearch(queryString);
    message(computeCount(query) + " total documents");
    searcher.close();
  }
  private ScoreDoc[] search(Query q, int numHits) throws IOException {
    return searcher.search(query, numHits).scoreDocs;
  }
  static public void message(String s) {
    System.out.println(s);
  }
  private void getFieldInfo() throws IOException {
    IndexReader indexReader = IndexReader.open(indexName, true);
    fields = new ArrayList<String>();
    indexedFields = new ArrayList<String>();
    for(String field : indexReader.getFieldNames(FieldOption.ALL)) {
      if (field != null && !field.equals(""))
        fields.add(field.toString());
    }
    for(String field : indexReader.getFieldNames(FieldOption.INDEXED)) {
      if (field != null && !field.equals(""))
        indexedFields.add(field.toString());
    }
    indexReader.close();
  }
  private void invertDocument(Document doc)
    throws IOException {
    Map<String,Integer> tokenMap = new HashMap<String,Integer>();
    final int maxFieldLength = 10000;
    Analyzer analyzer = createAnalyzer();
    for (Fieldable field : doc.getFields()) {
      String fieldName = field.name();
      if (field.isIndexed()) {
        if (field.isTokenized()) {     
          Reader reader;        
          if (field.readerValue() != null)
            reader = field.readerValue();
          else if (field.stringValue() != null)
            reader = new StringReader(field.stringValue());
          else
            throw new IllegalArgumentException
              ("field must have either String or Reader value");
          int position = 0;
          TokenStream stream = analyzer.tokenStream(fieldName, reader);
          TermAttribute termAtt = stream.addAttribute(TermAttribute.class);
          PositionIncrementAttribute posIncrAtt = stream.addAttribute(PositionIncrementAttribute.class);
          try {
            while (stream.incrementToken()) {
              position += (posIncrAtt.getPositionIncrement() - 1);
              position++;
              String name = termAtt.term();
              Integer Count = tokenMap.get(name);
              if (Count == null) { 
                tokenMap.put(name, Integer.valueOf(1)); 
              } else {
                int count = Count.intValue();
                tokenMap.put(name, Integer.valueOf(count + 1));
              }
              if (position > maxFieldLength) break;
            }
          } finally {
            stream.close();
          }
        }
      }
    }
    Map.Entry<String,Integer>[] sortedHash = getSortedMapEntries(tokenMap);
    for (int ii = 0; ii < sortedHash.length && ii < 10; ii++) {
      Map.Entry<String,Integer> currentEntry = sortedHash[ii];
      message((ii + 1) + ":" + currentEntry.getKey() + " " + currentEntry.getValue());
    }
  }
  public void terms(String field) throws IOException {
    TreeMap<String,Integer> termMap = new TreeMap<String,Integer>();
    IndexReader indexReader = IndexReader.open(indexName, true);
    TermEnum terms = indexReader.terms();
    while (terms.next()) {
      Term term = terms.term();
      if ((field == null) || field.equals(term.field()))
        termMap.put(term.field() + ":" + term.text(), Integer.valueOf((terms.docFreq())));
    }
    Iterator<String> termIterator = termMap.keySet().iterator();
    for (int ii = 0; termIterator.hasNext() && ii < 100; ii++) {
      String termDetails = termIterator.next();
      Integer termFreq = termMap.get(termDetails);
      message(termDetails + ": " + termFreq);
    }
    indexReader.close();
  }
  @SuppressWarnings("unchecked")
  public static <K,V extends Comparable<V>> Map.Entry<K,V>[]
    getSortedMapEntries(Map<K,V> m) {
    Set<Map.Entry<K, V>> set = m.entrySet();
    Map.Entry<K,V>[] entries =
       set.toArray(new Map.Entry[set.size()]);
    Arrays.sort(entries, new Comparator<Map.Entry<K,V>>() {
      public int compare(Map.Entry<K,V> o1, Map.Entry<K,V> o2) {
        V v1 = o1.getValue();
        V v2 = o2.getValue();
        return v2.compareTo(v1); 
      }
    });
    return entries;
  }
    public void analyzer(String word) {
        if ("current".equals(word)) {
            String current = analyzerClassFQN == null ? "StandardAnalyzer" : analyzerClassFQN;
            message("The currently used Analyzer class is: " + current);
            return;
        }
        analyzerClassFQN = word;
        message("Switched to Analyzer class " + analyzerClassFQN);
    }
}
