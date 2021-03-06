package org.apache.lucene.search;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.store.RAMDirectory;
public class TestSloppyPhraseQuery extends LuceneTestCase {
  private static final String S_1 = "A A A";
  private static final String S_2 = "A 1 2 3 A 4 5 6 A";
  private static final Document DOC_1 = makeDocument("X " + S_1 + " Y");
  private static final Document DOC_2 = makeDocument("X " + S_2 + " Y");
  private static final Document DOC_3 = makeDocument("X " + S_1 + " A Y");
  private static final Document DOC_1_B = makeDocument("X " + S_1 + " Y N N N N " + S_1 + " Z");
  private static final Document DOC_2_B = makeDocument("X " + S_2 + " Y N N N N " + S_2 + " Z");
  private static final Document DOC_3_B = makeDocument("X " + S_1 + " A Y N N N N " + S_1 + " A Y");
  private static final Document DOC_4 = makeDocument("A A X A X B A X B B A A X B A A");
  private static final PhraseQuery QUERY_1 = makePhraseQuery( S_1 );
  private static final PhraseQuery QUERY_2 = makePhraseQuery( S_2 );
  private static final PhraseQuery QUERY_4 = makePhraseQuery( "X A A");
  public void testDoc4_Query4_All_Slops_Should_match() throws Exception {
    for (int slop=0; slop<30; slop++) {
      int numResultsExpected = slop<1 ? 0 : 1;
      checkPhraseQuery(DOC_4, QUERY_4, slop, numResultsExpected);
    }
  }
  public void testDoc1_Query1_All_Slops_Should_match() throws Exception {
    for (int slop=0; slop<30; slop++) {
      float score1 = checkPhraseQuery(DOC_1, QUERY_1, slop, 1);
      float score2 = checkPhraseQuery(DOC_1_B, QUERY_1, slop, 1);
      assertTrue("slop="+slop+" score2="+score2+" should be greater than score1 "+score1, score2>score1);
    }
  }
  public void testDoc2_Query1_Slop_6_or_more_Should_match() throws Exception {
    for (int slop=0; slop<30; slop++) {
      int numResultsExpected = slop<6 ? 0 : 1;
      float score1 = checkPhraseQuery(DOC_2, QUERY_1, slop, numResultsExpected);
      if (numResultsExpected>0) {
        float score2 = checkPhraseQuery(DOC_2_B, QUERY_1, slop, 1);
        assertTrue("slop="+slop+" score2="+score2+" should be greater than score1 "+score1, score2>score1);
      }
    }
  }
  public void testDoc2_Query2_All_Slops_Should_match() throws Exception {
    for (int slop=0; slop<30; slop++) {
      float score1 = checkPhraseQuery(DOC_2, QUERY_2, slop, 1);
      float score2 = checkPhraseQuery(DOC_2_B, QUERY_2, slop, 1);
      assertTrue("slop="+slop+" score2="+score2+" should be greater than score1 "+score1, score2>score1);
    }
  }
  public void testDoc3_Query1_All_Slops_Should_match() throws Exception {
    for (int slop=0; slop<30; slop++) {
      float score1 = checkPhraseQuery(DOC_3, QUERY_1, slop, 1);
      float score2 = checkPhraseQuery(DOC_3_B, QUERY_1, slop, 1);
      assertTrue("slop="+slop+" score2="+score2+" should be greater than score1 "+score1, score2>score1);
    }
  }
  private float  checkPhraseQuery(Document doc, PhraseQuery query, int slop, int expectedNumResults) throws Exception {
    query.setSlop(slop);
    RAMDirectory ramDir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(ramDir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    writer.addDocument(doc);
    writer.close();
    IndexSearcher searcher = new IndexSearcher(ramDir, true);
    TopDocs td = searcher.search(query,null,10);
    assertEquals("slop: "+slop+"  query: "+query+"  doc: "+doc+"  Wrong number of hits", expectedNumResults, td.totalHits);
    searcher.close();
    ramDir.close();
    return td.getMaxScore();
  }
  private static Document makeDocument(String docText) {
    Document doc = new Document();
    Field f = new Field("f", docText, Field.Store.NO, Field.Index.ANALYZED);
    f.setOmitNorms(true);
    doc.add(f);
    return doc;
  }
  private static PhraseQuery makePhraseQuery(String terms) {
    PhraseQuery query = new PhraseQuery();
    String[] t = terms.split(" +");
    for (int i=0; i<t.length; i++) {
      query.add(new Term("f", t[i]));
    }
    return query;
  }
}
