package org.apache.solr.spelling;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.search.spell.StringDistance;
import org.apache.lucene.store.FSDirectory;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.util.AbstractSolrTestCase;
import org.apache.solr.util.RefCounted;
import org.apache.solr.search.SolrIndexSearcher;
import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
public class IndexBasedSpellCheckerTest extends AbstractSolrTestCase {
  protected SpellingQueryConverter queryConverter;
  protected static String[] DOCS = new String[]{
          "This is a title",
          "The quick reb fox jumped over the lazy brown dogs.",
          "This is a document",
          "another document",
          "red fox",
          "green bun",
          "green bud"
  };
  public String getSchemaFile() {
    return "schema.xml";
  }
  public String getSolrConfigFile() {
    return "solrconfig.xml";
  }
  @Override
  public void setUp() throws Exception {
    super.setUp();
    for (int i = 0; i < DOCS.length; i++) {
      assertU(adoc("id", String.valueOf(i), "title", DOCS[i]));
    }
    assertU("commit",
            commit());
    String allq = "id:[0 TO 3]";
    assertQ("docs not added", req(allq));
    queryConverter = new SimpleQueryConverter();
  }
  public void testSpelling() throws Exception {
    IndexBasedSpellChecker checker = new IndexBasedSpellChecker();
    NamedList spellchecker = new NamedList();
    spellchecker.add("classname", IndexBasedSpellChecker.class.getName());
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    File indexDir = new File(tmpDir, "spellingIdx" + new Date().getTime());
    indexDir.mkdirs();
    spellchecker.add(AbstractLuceneSpellChecker.INDEX_DIR, indexDir.getAbsolutePath());
    spellchecker.add(IndexBasedSpellChecker.FIELD, "title");
    spellchecker.add(AbstractLuceneSpellChecker.SPELLCHECKER_ARG_NAME, spellchecker);
    SolrCore core = h.getCore();
    String dictName = checker.init(spellchecker, core);
    assertTrue(dictName + " is not equal to " + SolrSpellChecker.DEFAULT_DICTIONARY_NAME,
            dictName.equals(SolrSpellChecker.DEFAULT_DICTIONARY_NAME) == true);
    RefCounted<SolrIndexSearcher> holder = core.getSearcher();
    SolrIndexSearcher searcher = holder.get();
    try {
    checker.build(core, searcher);
    IndexReader reader = searcher.getReader();
    Collection<Token> tokens = queryConverter.convert("documemt");
    SpellingResult result = checker.getSuggestions(tokens, reader);
    assertTrue("result is null and it shouldn't be", result != null);
    Map<String, Integer> suggestions = result.get(tokens.iterator().next());
    assertTrue("documemt is null and it shouldn't be", suggestions != null);
    assertTrue("documemt Size: " + suggestions.size() + " is not: " + 1, suggestions.size() == 1);
    Map.Entry<String, Integer> entry = suggestions.entrySet().iterator().next();
    assertTrue(entry.getKey() + " is not equal to " + "document", entry.getKey().equals("document") == true);
    assertTrue(entry.getValue() + " does not equal: " + SpellingResult.NO_FREQUENCY_INFO, entry.getValue() == SpellingResult.NO_FREQUENCY_INFO);
    tokens = queryConverter.convert("super");
    result = checker.getSuggestions(tokens, reader);
    assertTrue("result is null and it shouldn't be", result != null);
    suggestions = result.get(tokens.iterator().next());
    assertTrue("suggestions is not null and it should be", suggestions == null);
    tokens = queryConverter.convert("document");
    result = checker.getSuggestions(tokens, reader);
    assertTrue("result is null and it shouldn't be", result != null);
    suggestions = result.get(tokens.iterator().next());
    assertTrue("suggestions is null and it shouldn't be", suggestions == null);
    tokens = queryConverter.convert("red");
    result = checker.getSuggestions(tokens, reader, 2);
    assertTrue("result is null and it shouldn't be", result != null);
    suggestions = result.get(tokens.iterator().next());
    assertTrue("suggestions is not null and it should be", suggestions == null);
    tokens = queryConverter.convert("bug");
    result = checker.getSuggestions(tokens, reader, 2);
    assertTrue("result is null and it shouldn't be", result != null);
    suggestions = result.get(tokens.iterator().next());
    assertTrue("suggestions is null and it shouldn't be", suggestions != null);
    assertTrue("suggestions Size: " + suggestions.size() + " is not: " + 2, suggestions.size() == 2);
    entry = suggestions.entrySet().iterator().next();
    assertTrue(entry.getKey() + " is equal to " + "bug and it shouldn't be", entry.getKey().equals("bug") == false);
    assertTrue(entry.getValue() + " does not equal: " + SpellingResult.NO_FREQUENCY_INFO, entry.getValue() == SpellingResult.NO_FREQUENCY_INFO);
    entry = suggestions.entrySet().iterator().next();
    assertTrue(entry.getKey() + " is equal to " + "bug and it shouldn't be", entry.getKey().equals("bug") == false);
    assertTrue(entry.getValue() + " does not equal: " + SpellingResult.NO_FREQUENCY_INFO, entry.getValue() == SpellingResult.NO_FREQUENCY_INFO);
    } finally {
      holder.decref();
    }
  }
  public void testExtendedResults() throws Exception {
    IndexBasedSpellChecker checker = new IndexBasedSpellChecker();
    NamedList spellchecker = new NamedList();
    spellchecker.add("classname", IndexBasedSpellChecker.class.getName());
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    File indexDir = new File(tmpDir, "spellingIdx" + new Date().getTime());
    indexDir.mkdirs();
    spellchecker.add(AbstractLuceneSpellChecker.INDEX_DIR, indexDir.getAbsolutePath());
    spellchecker.add(IndexBasedSpellChecker.FIELD, "title");
    spellchecker.add(AbstractLuceneSpellChecker.SPELLCHECKER_ARG_NAME, spellchecker);
    SolrCore core = h.getCore();
    String dictName = checker.init(spellchecker, core);
    assertTrue(dictName + " is not equal to " + SolrSpellChecker.DEFAULT_DICTIONARY_NAME,
            dictName.equals(SolrSpellChecker.DEFAULT_DICTIONARY_NAME) == true);
    RefCounted<SolrIndexSearcher> holder = core.getSearcher();
    SolrIndexSearcher searcher = holder.get();
    try {
    checker.build(core, searcher);
    IndexReader reader = searcher.getReader();
    Collection<Token> tokens = queryConverter.convert("documemt");
    SpellingResult result = checker.getSuggestions(tokens, reader, 1, false, true);
    assertTrue("result is null and it shouldn't be", result != null);
    Map<String, Integer> suggestions = result.get(tokens.iterator().next());
    assertTrue("documemt is null and it shouldn't be", suggestions != null);
    assertTrue("documemt Size: " + suggestions.size() + " is not: " + 1, suggestions.size() == 1);
    Map.Entry<String, Integer> entry = suggestions.entrySet().iterator().next();
    assertTrue(entry.getKey() + " is not equal to " + "document", entry.getKey().equals("document") == true);
    assertTrue(entry.getValue() + " does not equal: " + 2, entry.getValue() == 2);
    tokens = queryConverter.convert("super");
    result = checker.getSuggestions(tokens, reader, 1, false, true);
    assertTrue("result is null and it shouldn't be", result != null);
    suggestions = result.get(tokens.iterator().next());
    assertTrue("suggestions is not null and it should be", suggestions == null);
    tokens = queryConverter.convert("document");
    result = checker.getSuggestions(tokens, reader, 1, false, true);
    assertTrue("result is null and it shouldn't be", result != null);
    suggestions = result.get(tokens.iterator().next());
    assertTrue("suggestions is not null and it should be", suggestions == null);
    } finally {
      holder.decref();
    }
  }
  private class TestSpellChecker extends IndexBasedSpellChecker{
    public SpellChecker getSpellChecker(){
      return spellChecker;
    }
  }
  public void testAlternateDistance() throws Exception {
    TestSpellChecker checker = new TestSpellChecker();
    NamedList spellchecker = new NamedList();
    spellchecker.add("classname", IndexBasedSpellChecker.class.getName());
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    File indexDir = new File(tmpDir, "spellingIdx" + new Date().getTime());
    indexDir.mkdirs();
    spellchecker.add(AbstractLuceneSpellChecker.INDEX_DIR, indexDir.getAbsolutePath());
    spellchecker.add(IndexBasedSpellChecker.FIELD, "title");
    spellchecker.add(AbstractLuceneSpellChecker.SPELLCHECKER_ARG_NAME, spellchecker);
    spellchecker.add(AbstractLuceneSpellChecker.STRING_DISTANCE, JaroWinklerDistance.class.getName());
    SolrCore core = h.getCore();
    String dictName = checker.init(spellchecker, core);
    assertTrue(dictName + " is not equal to " + SolrSpellChecker.DEFAULT_DICTIONARY_NAME,
            dictName.equals(SolrSpellChecker.DEFAULT_DICTIONARY_NAME) == true);
    RefCounted<SolrIndexSearcher> holder = core.getSearcher();
    SolrIndexSearcher searcher = holder.get();
    try {
    checker.build(core, searcher);
    SpellChecker sc = checker.getSpellChecker();
    assertTrue("sc is null and it shouldn't be", sc != null);
    StringDistance sd = sc.getStringDistance();
    assertTrue("sd is null and it shouldn't be", sd != null);
    assertTrue("sd is not an instance of " + JaroWinklerDistance.class.getName(), sd instanceof JaroWinklerDistance);
    } finally {
      holder.decref();
    }
  }
  public void testAlternateLocation() throws Exception {
    String[] ALT_DOCS = new String[]{
            "jumpin jack flash",
            "Sargent Peppers Lonely Hearts Club Band",
            "Born to Run",
            "Thunder Road",
            "Londons Burning",
            "A Horse with No Name",
            "Sweet Caroline"
    };
    IndexBasedSpellChecker checker = new IndexBasedSpellChecker();
    NamedList spellchecker = new NamedList();
    spellchecker.add("classname", IndexBasedSpellChecker.class.getName());
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    File indexDir = new File(tmpDir, "spellingIdx" + new Date().getTime());
    File altIndexDir = new File(tmpDir, "alternateIdx" + new Date().getTime());
    IndexWriter iw = new IndexWriter(FSDirectory.open(altIndexDir), new WhitespaceAnalyzer(), IndexWriter.MaxFieldLength.LIMITED);
    for (int i = 0; i < ALT_DOCS.length; i++) {
      Document doc = new Document();
      doc.add(new Field("title", ALT_DOCS[i], Field.Store.YES, Field.Index.ANALYZED));
      iw.addDocument(doc);
    }
    iw.optimize();
    iw.close();
    indexDir.mkdirs();
    spellchecker.add(AbstractLuceneSpellChecker.INDEX_DIR, indexDir.getAbsolutePath());
    spellchecker.add(AbstractLuceneSpellChecker.LOCATION, altIndexDir.getAbsolutePath());
    spellchecker.add(IndexBasedSpellChecker.FIELD, "title");
    spellchecker.add(AbstractLuceneSpellChecker.SPELLCHECKER_ARG_NAME, spellchecker);
    SolrCore core = h.getCore();
    String dictName = checker.init(spellchecker, core);
    assertTrue(dictName + " is not equal to " + SolrSpellChecker.DEFAULT_DICTIONARY_NAME,
            dictName.equals(SolrSpellChecker.DEFAULT_DICTIONARY_NAME) == true);
    RefCounted<SolrIndexSearcher> holder = core.getSearcher();
    SolrIndexSearcher searcher = holder.get();
    try {
    checker.build(core, searcher);
    IndexReader reader = searcher.getReader();
    Collection<Token> tokens = queryConverter.convert("flesh");
    SpellingResult result = checker.getSuggestions(tokens, reader, 1, false, true);
    assertTrue("result is null and it shouldn't be", result != null);
    Map<String, Integer> suggestions = result.get(tokens.iterator().next());
    assertTrue("flesh is null and it shouldn't be", suggestions != null);
    assertTrue("flesh Size: " + suggestions.size() + " is not: " + 1, suggestions.size() == 1);
    Map.Entry<String, Integer> entry = suggestions.entrySet().iterator().next();
    assertTrue(entry.getKey() + " is not equal to " + "flash", entry.getKey().equals("flash") == true);
    assertTrue(entry.getValue() + " does not equal: " + 1, entry.getValue() == 1);
    tokens = queryConverter.convert("super");
    result = checker.getSuggestions(tokens, reader, 1, false, true);
    assertTrue("result is null and it shouldn't be", result != null);
    suggestions = result.get(tokens.iterator().next());
    assertTrue("suggestions is not null and it should be", suggestions == null);
    tokens = queryConverter.convert("Caroline");
    result = checker.getSuggestions(tokens, reader, 1, false, true);
    assertTrue("result is null and it shouldn't be", result != null);
    suggestions = result.get(tokens.iterator().next());
    assertTrue("suggestions is not null and it should be", suggestions == null);
    } finally {
      holder.decref();
    }
  }
}
