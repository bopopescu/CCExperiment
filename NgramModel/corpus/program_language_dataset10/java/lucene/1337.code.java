package org.apache.lucene.queryParser.surround.query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
public class SingleFieldTestDb {
  private Directory db;
  private String[] docs;
  private String fieldName;
  public SingleFieldTestDb(String[] documents, String fName) {
    try {
      db = new RAMDirectory();
      docs = documents;
      fieldName = fName;
      IndexWriter writer = new IndexWriter(db, new IndexWriterConfig(
          Version.LUCENE_CURRENT,
          new WhitespaceAnalyzer(Version.LUCENE_CURRENT)));
      for (int j = 0; j < docs.length; j++) {
        Document d = new Document();
        d.add(new Field(fieldName, docs[j], Field.Store.NO, Field.Index.ANALYZED));
        writer.addDocument(d);
      }
      writer.close();
    } catch (java.io.IOException ioe) {
      throw new Error(ioe);
    }
  }
  Directory getDb() {return db;}
  String[] getDocs() {return docs;}
  String getFieldname() {return fieldName;}
}
