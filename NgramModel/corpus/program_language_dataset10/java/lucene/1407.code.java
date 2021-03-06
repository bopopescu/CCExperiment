package org.apache.lucene.demo;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import java.io.File;
import java.util.Date;
import java.util.Arrays;
public class IndexHTML {
  private IndexHTML() {}
  private static boolean deleting = false;	  
  private static IndexReader reader;		  
  private static IndexWriter writer;		  
  private static TermEnum uidIter;		  
  public static void main(String[] argv) {
    try {
      File index = new File("index");
      boolean create = false;
      File root = null;
      String usage = "IndexHTML [-create] [-index <index>] <root_directory>";
      if (argv.length == 0) {
        System.err.println("Usage: " + usage);
        return;
      }
      for (int i = 0; i < argv.length; i++) {
        if (argv[i].equals("-index")) {		  
          index = new File(argv[++i]);
        } else if (argv[i].equals("-create")) {	  
          create = true;
        } else if (i != argv.length-1) {
          System.err.println("Usage: " + usage);
          return;
        } else
          root = new File(argv[i]);
      }
      if(root == null) {
        System.err.println("Specify directory to index");
        System.err.println("Usage: " + usage);
        return;
      }
      Date start = new Date();
      if (!create) {				  
        deleting = true;
        indexDocs(root, index, create);
      }
      writer = new IndexWriter(FSDirectory.open(index), new IndexWriterConfig(
          Version.LUCENE_CURRENT, new StandardAnalyzer(Version.LUCENE_CURRENT))
          .setMaxFieldLength(1000000).setOpenMode(
              create ? OpenMode.CREATE : OpenMode.CREATE_OR_APPEND));
      indexDocs(root, index, create);		  
      System.out.println("Optimizing index...");
      writer.optimize();
      writer.close();
      Date end = new Date();
      System.out.print(end.getTime() - start.getTime());
      System.out.println(" total milliseconds");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private static void indexDocs(File file, File index, boolean create)
       throws Exception {
    if (!create) {				  
      reader = IndexReader.open(FSDirectory.open(index), false);		  
      uidIter = reader.terms(new Term("uid", "")); 
      indexDocs(file);
      if (deleting) {				  
        while (uidIter.term() != null && uidIter.term().field() == "uid") {
          System.out.println("deleting " +
              HTMLDocument.uid2url(uidIter.term().text()));
          reader.deleteDocuments(uidIter.term());
          uidIter.next();
        }
        deleting = false;
      }
      uidIter.close();				  
      reader.close();				  
    } else					  
      indexDocs(file);
  }
  private static void indexDocs(File file) throws Exception {
    if (file.isDirectory()) {			  
      String[] files = file.list();		  
      Arrays.sort(files);			  
      for (int i = 0; i < files.length; i++)	  
        indexDocs(new File(file, files[i]));
    } else if (file.getPath().endsWith(".html") || 
      file.getPath().endsWith(".htm") || 
      file.getPath().endsWith(".txt")) { 
      if (uidIter != null) {
        String uid = HTMLDocument.uid(file);	  
        while (uidIter.term() != null && uidIter.term().field() == "uid" &&
            uidIter.term().text().compareTo(uid) < 0) {
          if (deleting) {			  
            System.out.println("deleting " +
                HTMLDocument.uid2url(uidIter.term().text()));
            reader.deleteDocuments(uidIter.term());
          }
          uidIter.next();
        }
        if (uidIter.term() != null && uidIter.term().field() == "uid" &&
            uidIter.term().text().compareTo(uid) == 0) {
          uidIter.next();			  
        } else if (!deleting) {			  
          Document doc = HTMLDocument.Document(file);
          System.out.println("adding " + doc.get("path"));
          writer.addDocument(doc);
        }
      } else {					  
        Document doc = HTMLDocument.Document(file);
        System.out.println("adding " + doc.get("path"));
        writer.addDocument(doc);		  
      }
    }
  }
}
