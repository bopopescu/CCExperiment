package org.apache.lucene.benchmark.byTask.tasks;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.lucene.benchmark.BenchmarkTestCase;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.feeds.DocMaker;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
public class WriteLineDocTaskTest extends BenchmarkTestCase {
  public static final class WriteLineDocMaker extends DocMaker {
    @Override
    public Document makeDocument() throws Exception {
      Document doc = new Document();
      doc.add(new Field(BODY_FIELD, "body", Store.NO, Index.NOT_ANALYZED_NO_NORMS));
      doc.add(new Field(TITLE_FIELD, "title", Store.NO, Index.NOT_ANALYZED_NO_NORMS));
      doc.add(new Field(DATE_FIELD, "date", Store.NO, Index.NOT_ANALYZED_NO_NORMS));
      return doc;
    }
  }
  public static final class NewLinesDocMaker extends DocMaker {
    @Override
    public Document makeDocument() throws Exception {
      Document doc = new Document();
      doc.add(new Field(BODY_FIELD, "body\r\ntext\ttwo", Store.NO, Index.NOT_ANALYZED_NO_NORMS));
      doc.add(new Field(TITLE_FIELD, "title\r\ntext", Store.NO, Index.NOT_ANALYZED_NO_NORMS));
      doc.add(new Field(DATE_FIELD, "date\r\ntext", Store.NO, Index.NOT_ANALYZED_NO_NORMS));
      return doc;
    }
  }
  public static final class NoBodyDocMaker extends DocMaker {
    @Override
    public Document makeDocument() throws Exception {
      Document doc = new Document();
      doc.add(new Field(TITLE_FIELD, "title", Store.NO, Index.NOT_ANALYZED_NO_NORMS));
      doc.add(new Field(DATE_FIELD, "date", Store.NO, Index.NOT_ANALYZED_NO_NORMS));
      return doc;
    }
  }
  public static final class NoTitleDocMaker extends DocMaker {
    @Override
    public Document makeDocument() throws Exception {
      Document doc = new Document();
      doc.add(new Field(BODY_FIELD, "body", Store.NO, Index.NOT_ANALYZED_NO_NORMS));
      doc.add(new Field(DATE_FIELD, "date", Store.NO, Index.NOT_ANALYZED_NO_NORMS));
      return doc;
    }
  }
  public static final class JustDateDocMaker extends DocMaker {
    @Override
    public Document makeDocument() throws Exception {
      Document doc = new Document();
      doc.add(new Field(DATE_FIELD, "date", Store.NO, Index.NOT_ANALYZED_NO_NORMS));
      return doc;
    }
  }
  private static final CompressorStreamFactory csFactory = new CompressorStreamFactory();
  private PerfRunData createPerfRunData(File file, boolean setBZCompress,
                                        String bz2CompressVal,
                                        String docMakerName) throws Exception {
    Properties props = new Properties();
    props.setProperty("doc.maker", docMakerName);
    props.setProperty("line.file.out", file.getAbsolutePath());
    if (setBZCompress) {
      props.setProperty("bzip.compression", bz2CompressVal);
    }
    props.setProperty("directory", "RAMDirectory"); 
    Config config = new Config(props);
    return new PerfRunData(config);
  }
  private void doReadTest(File file, boolean bz2File, String expTitle,
                          String expDate, String expBody) throws Exception {
    InputStream in = new FileInputStream(file);
    if (bz2File) {
      in = csFactory.createCompressorInputStream("bzip2", in);
    }
    BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));
    try {
      String line = br.readLine();
      assertNotNull(line);
      String[] parts = line.split(Character.toString(WriteLineDocTask.SEP));
      int numExpParts = expBody == null ? 2 : 3;
      assertEquals(numExpParts, parts.length);
      assertEquals(expTitle, parts[0]);
      assertEquals(expDate, parts[1]);
      if (expBody != null) {
        assertEquals(expBody, parts[2]);
      }
      assertNull(br.readLine());
    } finally {
      br.close();
    }
  }
  public void testBZip2() throws Exception {
    File file = new File(getWorkDir(), "one-line.bz2");
    PerfRunData runData = createPerfRunData(file, true, "true", WriteLineDocMaker.class.getName());
    WriteLineDocTask wldt = new WriteLineDocTask(runData);
    wldt.doLogic();
    wldt.close();
    doReadTest(file, true, "title", "date", "body");
  }
  public void testBZip2AutoDetect() throws Exception {
    File file = new File(getWorkDir(), "one-line.bz2");
    PerfRunData runData = createPerfRunData(file, false, null, WriteLineDocMaker.class.getName());
    WriteLineDocTask wldt = new WriteLineDocTask(runData);
    wldt.doLogic();
    wldt.close();
    doReadTest(file, true, "title", "date", "body");
  }
  public void testRegularFile() throws Exception {
    File file = new File(getWorkDir(), "one-line");
    PerfRunData runData = createPerfRunData(file, true, "false", WriteLineDocMaker.class.getName());
    WriteLineDocTask wldt = new WriteLineDocTask(runData);
    wldt.doLogic();
    wldt.close();
    doReadTest(file, false, "title", "date", "body");
  }
  public void testCharsReplace() throws Exception {
    File file = new File(getWorkDir(), "one-line");
    PerfRunData runData = createPerfRunData(file, false, null, NewLinesDocMaker.class.getName());
    WriteLineDocTask wldt = new WriteLineDocTask(runData);
    wldt.doLogic();
    wldt.close();
    doReadTest(file, false, "title text", "date text", "body text two");
  }
  public void testEmptyBody() throws Exception {
    File file = new File(getWorkDir(), "one-line");
    PerfRunData runData = createPerfRunData(file, false, null, NoBodyDocMaker.class.getName());
    WriteLineDocTask wldt = new WriteLineDocTask(runData);
    wldt.doLogic();
    wldt.close();
    doReadTest(file, false, "title", "date", null);
  }
  public void testEmptyTitle() throws Exception {
    File file = new File(getWorkDir(), "one-line");
    PerfRunData runData = createPerfRunData(file, false, null, NoTitleDocMaker.class.getName());
    WriteLineDocTask wldt = new WriteLineDocTask(runData);
    wldt.doLogic();
    wldt.close();
    doReadTest(file, false, "", "date", "body");
  }
  public void testJustDate() throws Exception {
    File file = new File(getWorkDir(), "one-line");
    PerfRunData runData = createPerfRunData(file, false, null, JustDateDocMaker.class.getName());
    WriteLineDocTask wldt = new WriteLineDocTask(runData);
    wldt.doLogic();
    wldt.close();
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
    try {
      String line = br.readLine();
      assertNull(line);
    } finally {
      br.close();
    }
  }
}
