package org.apache.lucene.index.memory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.index.TermDocs;
public class MemoryIndexTest extends BaseTokenStreamTestCase {
  private Analyzer analyzer;
  private static final String FIELD_NAME = "content";
  public static void main(String[] args) throws Throwable {
    new MemoryIndexTest().run(args);    
  }
  public String fileDir;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    fileDir = System.getProperty("lucene.common.dir", null);
  }
  public void testMany() throws Throwable {
    String[] files = listFiles(new String[] {
      "*.txt", "*.html", "*.xml", "xdocs/*.xml", 
      "src/java/test/org/apache/lucene/queryParser/*.java",
      "contrib/memory/src/java/org/apache/lucene/index/memory/*.java",
    });
    if (VERBOSE) System.out.println("files = " + java.util.Arrays.asList(files));
    String[] xargs = new String[] {
      "1", "1", "memram", 
      "@contrib/memory/src/test/org/apache/lucene/index/memory/testqueries.txt",
    };
    String[] args = new String[xargs.length + files.length];
    System.arraycopy(xargs, 0, args, 0, xargs.length);
    System.arraycopy(files, 0, args, xargs.length, files.length);
    run(args);
  }
  private void run(String[] args) throws Throwable {
    int k = -1;
    int iters = 1;
    if (args.length > ++k) iters = Math.max(1, Integer.parseInt(args[k]));
    int runs = 1;
    if (args.length > ++k) runs = Math.max(1, Integer.parseInt(args[k]));
    String cmd = "memram";
    if (args.length > ++k) cmd = args[k];
    boolean useMemIndex = cmd.indexOf("mem") >= 0;
    boolean useRAMIndex = cmd.indexOf("ram") >= 0;
    String[] queries = { "term", "term*", "term~", "Apache", "Apach~ AND Copy*" };
    if (args.length > ++k) {
      String arg = args[k];
      if (arg.startsWith("@")) 
        queries = readLines(new File(fileDir, arg.substring(1)));
      else
        queries = new String[] { arg };
    }
    File[] files = new File[] {new File("CHANGES.txt"), new File("LICENSE.txt") };
    if (args.length > ++k) {
      files = new File[args.length - k];
      for (int i=k; i < args.length; i++) {
        files[i-k] = new File(args[i]);
      }
    }
    Analyzer[] analyzers = new Analyzer[] { 
        new SimpleAnalyzer(TEST_VERSION_CURRENT),
        new StopAnalyzer(TEST_VERSION_CURRENT),
        new StandardAnalyzer(TEST_VERSION_CURRENT),
    };
    boolean first = true;
    for (int iter=0; iter < iters; iter++) {
      if (VERBOSE) System.out.println("\n########### iteration=" + iter);
      long start = System.currentTimeMillis();            
      long bytes = 0;
      for (int anal=0; anal < analyzers.length; anal++) {
        this.analyzer = analyzers[anal];
        for (int i=0; i < files.length; i++) {
          File file = files[i];
          if (!file.exists() || file.isDirectory()) continue; 
          bytes += file.length();
          String text = toString(new FileInputStream(file), null);
          Document doc = createDocument(text);
          if (VERBOSE) System.out.println("\n*********** FILE=" + file);
          boolean measureIndexing = false; 
          MemoryIndex memind = null;
          IndexSearcher memsearcher = null;
          if (useMemIndex && !measureIndexing) {
            memind = createMemoryIndex(doc);
            memsearcher = memind.createSearcher();
          }
          if (first) {
            IndexSearcher s = memind.createSearcher();
            TermDocs td = s.getIndexReader().termDocs(null);
            assertTrue(td.next());
            assertEquals(0, td.doc());
            assertEquals(1, td.freq());
            td.close();
            s.close();
            first = false;
          }
          RAMDirectory ramind = null;
          IndexSearcher ramsearcher = null;
          if (useRAMIndex && !measureIndexing) {
            ramind = createRAMIndex(doc);
            ramsearcher = new IndexSearcher(ramind);
          }
          for (int q=0; q < queries.length; q++) {
            try {
              Query query = parseQuery(queries[q]);
              for (int run=0; run < runs; run++) {
                float score1 = 0.0f; float score2 = 0.0f;
                if (useMemIndex && measureIndexing) {
                  memind = createMemoryIndex(doc);
                  memsearcher = memind.createSearcher();
                }
                if (useMemIndex) score1 = query(memsearcher, query); 
                if (useRAMIndex && measureIndexing) {
                  ramind = createRAMIndex(doc);
                  ramsearcher = new IndexSearcher(ramind);
                }
                if (useRAMIndex) score2 = query(ramsearcher, query);
                if (useMemIndex && useRAMIndex) {
                  if (VERBOSE) System.out.println("diff="+ (score1-score2) + ", query=" + queries[q] + ", s1=" + score1 + ", s2=" + score2);
                  if (score1 != score2 || score1 < 0.0f || score2 < 0.0f || score1 > 1.0f || score2 > 1.0f) {
                    throw new IllegalStateException("BUG DETECTED:" + (i*(q+1)) + " at query=" + queries[q] + ", file=" + file + ", anal=" + analyzer);
                  }
                }
              }
            } catch (Throwable t) {
              if (t instanceof OutOfMemoryError) t.printStackTrace();
              if (VERBOSE) System.out.println("Fatal error at query=" + queries[q] + ", file=" + file + ", anal=" + analyzer);
              throw t;
            }
          }
        }
      }
      long end = System.currentTimeMillis();
      if (VERBOSE) {
        System.out.println("\nsecs = " + ((end-start)/1000.0f));
        System.out.println("queries/sec= " + 
        (1.0f * runs * queries.length * analyzers.length * files.length 
            / ((end-start)/1000.0f)));
        float mb = (1.0f * bytes * queries.length * runs) / (1024.0f * 1024.0f);
        System.out.println("MB/sec = " + (mb / ((end-start)/1000.0f)));
      }
    }
    if (!VERBOSE) return;
    if (useMemIndex && useRAMIndex) 
      System.out.println("No bug found. done.");
    else 
      System.out.println("Done benchmarking (without checking correctness).");
  }
  private String[] readLines(File file) throws Exception {
    BufferedReader reader = new BufferedReader(new InputStreamReader(
        new FileInputStream(file))); 
    List<String> lines = new ArrayList<String>();
    String line;  
    while ((line = reader.readLine()) != null) {
      String t = line.trim(); 
      if (t.length() > 0 && t.charAt(0) != '#' && (!t.startsWith("//"))) {
        lines.add(line);
      }
    }
    reader.close();
    String[] result = new String[lines.size()];
    lines.toArray(result);
    return result;
  }
  private Document createDocument(String content) {
    Document doc = new Document();
    doc.add(new Field(FIELD_NAME, content, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
    return doc;
  }
  private MemoryIndex createMemoryIndex(Document doc) {
    MemoryIndex index = new MemoryIndex();
    Iterator<Fieldable> iter = doc.getFields().iterator();
    while (iter.hasNext()) {
      Fieldable field = iter.next();
      index.addField(field.name(), field.stringValue(), analyzer);
    }
    return index;
  }
  private RAMDirectory createRAMIndex(Document doc) {
    RAMDirectory dir = new RAMDirectory();    
    IndexWriter writer = null;
    try {
      writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, analyzer));
      writer.addDocument(doc);
      writer.optimize();
      return dir;
    } catch (IOException e) { 
      throw new RuntimeException(e);
    } finally {
      try {
        if (writer != null) writer.close();
      } catch (IOException e) { 
        throw new RuntimeException(e);
      }
    }
  }
  final float[] scores = new float[1]; 
  private float query(IndexSearcher searcher, Query query) {
    try {
      searcher.search(query, new Collector() {
        private Scorer scorer;
        @Override
        public void collect(int doc) throws IOException {
          scores[0] = scorer.score();
        }
        @Override
        public void setScorer(Scorer scorer) throws IOException {
          this.scorer = scorer;
        }
        @Override
        public boolean acceptsDocsOutOfOrder() {
          return true;
        }
        @Override
        public void setNextReader(IndexReader reader, int docBase) { }
      });
      float score = scores[0];
      return score;
    } catch (IOException e) { 
      throw new RuntimeException(e);
    }
  }
  int getMemorySize(Object index) {
    if (index instanceof Directory) {
      try {
        Directory dir = (Directory) index;
        int size = 0;
        String[] fileNames = dir.listAll();
        for (int i=0; i < fileNames.length; i++) {
          size += dir.fileLength(fileNames[i]);
        }
        return size;
      }
      catch (IOException e) { 
        throw new RuntimeException(e);
      }
    }
    else {
      return ((MemoryIndex) index).getMemorySize();
    }
  }
  private Query parseQuery(String expression) throws ParseException {
    QueryParser parser = new QueryParser(TEST_VERSION_CURRENT, FIELD_NAME, analyzer);
    return parser.parse(expression);
  }
  static String[] listFiles(String[] fileNames) {
    LinkedHashSet<String> allFiles = new LinkedHashSet<String>();
    for (int i=0; i < fileNames.length; i++) {
      int k;
      if ((k = fileNames[i].indexOf("*")) < 0) {
        allFiles.add(fileNames[i]);
      } else {
        String prefix = fileNames[i].substring(0, k);
        if (prefix.length() == 0) prefix = ".";
        final String suffix = fileNames[i].substring(k+1);
        File[] files = new File(prefix).listFiles(new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return name.endsWith(suffix);
          }
        });
        if (files != null) {
          for (int j=0; j < files.length; j++) {
            allFiles.add(files[j].getPath());
          }
        }
      }      
    }
    String[] result = new String[allFiles.size()];
    allFiles.toArray(result);
    return result;
  }
  private static final Charset DEFAULT_PLATFORM_CHARSET = 
    Charset.forName(new InputStreamReader(new ByteArrayInputStream(new byte[0])).getEncoding());  
  private static String toString(InputStream input, Charset charset) throws IOException {
    if (charset == null) charset = DEFAULT_PLATFORM_CHARSET;      
    byte[] data = toByteArray(input);
    return charset.decode(ByteBuffer.wrap(data)).toString();
  }
  private static byte[] toByteArray(InputStream input) throws IOException {
    try {
      int len = Math.max(256, input.available());
      byte[] buffer = new byte[len];
      byte[] output = new byte[len];
      len = 0;
      int n;
      while ((n = input.read(buffer)) >= 0) {
        if (len + n > output.length) { 
          byte tmp[] = new byte[Math.max(output.length << 1, len + n)];
          System.arraycopy(output, 0, tmp, 0, len);
          System.arraycopy(buffer, 0, tmp, len, n);
          buffer = output; 
          output = tmp;
        } else {
          System.arraycopy(buffer, 0, output, len, n);
        }
        len += n;
      }
      if (len == output.length) return output;
      buffer = null; 
      buffer = new byte[len];
      System.arraycopy(output, 0, buffer, 0, len);
      return buffer;
    } finally {
      input.close();
    }
  }
}