package org.apache.lucene.index;
import java.io.IOException;
import java.io.File;
import java.util.Date;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.StringHelper;
public class FieldNormModifier {
  public static void main(String[] args) throws IOException {
    if (args.length < 3) {
      System.err.println("Usage: FieldNormModifier <index> <package.SimilarityClassName | -n> <field1> [field2] ...");
      System.exit(1);
    }
    Similarity s = null;
    if (!args[1].equals("-n")) {
      try {
        s = Class.forName(args[1]).asSubclass(Similarity.class).newInstance();
      } catch (Exception e) {
        System.err.println("Couldn't instantiate similarity with empty constructor: " + args[1]);
        e.printStackTrace(System.err);
        System.exit(1);
      }
    }
    Directory d = FSDirectory.open(new File(args[0]));
    FieldNormModifier fnm = new FieldNormModifier(d, s);
    for (int i = 2; i < args.length; i++) {
      System.out.print("Updating field: " + args[i] + " " + (new Date()).toString() + " ... ");
      fnm.reSetNorms(args[i]);
      System.out.println(new Date().toString());
    }
    d.close();
  }
  private Directory dir;
  private Similarity sim;
  public FieldNormModifier(Directory d, Similarity s) {
    dir = d;
    sim = s;
  }
  public void reSetNorms(String field) throws IOException {
    String fieldName = StringHelper.intern(field);
    int[] termCounts = new int[0];
    IndexReader reader = null;
    TermEnum termEnum = null;
    TermDocs termDocs = null;
    try {
      reader = IndexReader.open(dir, true);
      termCounts = new int[reader.maxDoc()];
      try {
        termEnum = reader.terms(new Term(field));
        try {
          termDocs = reader.termDocs();
          do {
            Term term = termEnum.term();
            if (term != null && term.field().equals(fieldName)) {
              termDocs.seek(termEnum.term());
              while (termDocs.next()) {
                termCounts[termDocs.doc()] += termDocs.freq();
              }
            }
          } while (termEnum.next());
        } finally {
          if (null != termDocs) termDocs.close();
        }
      } finally {
        if (null != termEnum) termEnum.close();
      }
    } finally {
      if (null != reader) reader.close();
    }
    try {
      reader = IndexReader.open(dir, false); 
      for (int d = 0; d < termCounts.length; d++) {
        if (! reader.isDeleted(d)) {
          if (sim == null)
            reader.setNorm(d, fieldName, Similarity.encodeNorm(1.0f));
          else
            reader.setNorm(d, fieldName, sim.encodeNormValue(sim.lengthNorm(fieldName, termCounts[d])));
        }
      }
    } finally {
      if (null != reader) reader.close();
    }
  }
}
