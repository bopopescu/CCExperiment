package org.apache.lucene.search;
import java.util.Arrays;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
public class TestDateSort extends LuceneTestCase {
  private static final String TEXT_FIELD = "text";
  private static final String DATE_TIME_FIELD = "dateTime";
  private static Directory directory;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    directory = new RAMDirectory();
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    writer.addDocument(createDocument("Document 1", 1192001122000L));
    writer.addDocument(createDocument("Document 2", 1192001126000L));
    writer.addDocument(createDocument("Document 3", 1192101133000L));
    writer.addDocument(createDocument("Document 4", 1192104129000L));
    writer.addDocument(createDocument("Document 5", 1192209943000L));
    writer.optimize();
    writer.close();
  }
  public void testReverseDateSort() throws Exception {
    IndexSearcher searcher = new IndexSearcher(directory, true);
    Sort sort = new Sort(new SortField(DATE_TIME_FIELD, SortField.STRING, true));
    QueryParser queryParser = new QueryParser(TEST_VERSION_CURRENT, TEXT_FIELD, new WhitespaceAnalyzer(TEST_VERSION_CURRENT));
    Query query = queryParser.parse("Document");
    String[] actualOrder = new String[5];
    ScoreDoc[] hits = searcher.search(query, null, 1000, sort).scoreDocs;
    for (int i = 0; i < hits.length; i++) {
      Document document = searcher.doc(hits[i].doc);
      String text = document.get(TEXT_FIELD);
      actualOrder[i] = text;
    }
    searcher.close();
    String[] expectedOrder = new String[5];
    expectedOrder[0] = "Document 5";
    expectedOrder[1] = "Document 4";
    expectedOrder[2] = "Document 3";
    expectedOrder[3] = "Document 2";
    expectedOrder[4] = "Document 1";
    assertEquals(Arrays.asList(expectedOrder), Arrays.asList(actualOrder));
  }
  private static Document createDocument(String text, long time) {
    Document document = new Document();
    Field textField = new Field(TEXT_FIELD, text, Field.Store.YES, Field.Index.ANALYZED);
    document.add(textField);
    String dateTimeString = DateTools.timeToString(time, DateTools.Resolution.SECOND);
    Field dateTimeField = new Field(DATE_TIME_FIELD, dateTimeString, Field.Store.YES,
        Field.Index.NOT_ANALYZED);
    document.add(dateTimeField);
    return document;
  }
}
