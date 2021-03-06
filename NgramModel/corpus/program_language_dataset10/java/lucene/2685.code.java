package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.core.SolrResourceLoader;
import java.io.StringReader;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
public class CommonGramsQueryFilterFactoryTest extends BaseTokenTestCase {
  public void testInform() throws Exception {
    ResourceLoader loader = new SolrResourceLoader(null, null);
    assertTrue("loader is null and it shouldn't be", loader != null);
    CommonGramsQueryFilterFactory factory = new CommonGramsQueryFilterFactory();
    Map<String, String> args = new HashMap<String, String>();
    args.put("words", "stop-1.txt");
    args.put("ignoreCase", "true");
    factory.init(args);
    factory.inform(loader);
    Set words = factory.getCommonWords();
    assertTrue("words is null and it shouldn't be", words != null);
    assertTrue("words Size: " + words.size() + " is not: " + 2,
        words.size() == 2);
    assertTrue(factory.isIgnoreCase() + " does not equal: " + true, factory
        .isIgnoreCase() == true);
    factory = new CommonGramsQueryFilterFactory();
    args.put("words", "stop-1.txt, stop-2.txt");
    factory.init(args);
    factory.inform(loader);
    words = factory.getCommonWords();
    assertTrue("words is null and it shouldn't be", words != null);
    assertTrue("words Size: " + words.size() + " is not: " + 4,
        words.size() == 4);
    assertTrue(factory.isIgnoreCase() + " does not equal: " + true, factory
        .isIgnoreCase() == true);
  }
  public void testDefaults() throws Exception {
    ResourceLoader loader = new SolrResourceLoader(null, null);
    assertTrue("loader is null and it shouldn't be", loader != null);
    CommonGramsQueryFilterFactory factory = new CommonGramsQueryFilterFactory();
    Map<String, String> args = new HashMap<String, String>();
    factory.init(args);
    factory.inform(loader);
    Set words = factory.getCommonWords();
    assertTrue("words is null and it shouldn't be", words != null);
    assertTrue(words.contains("the"));
    Tokenizer tokenizer = new WhitespaceTokenizer(new StringReader("testing the factory"));
    TokenStream stream = factory.create(tokenizer);
    assertTokenStreamContents(stream, 
        new String[] { "testing_the", "the_factory" });
  }
}
