package org.apache.solr.analysis;
import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
public class TestCJKTokenizerFactory extends BaseTokenTestCase {
  public void testTokenizer() throws Exception {
    Reader reader = new StringReader("我是中国人");
    CJKTokenizerFactory factory = new CJKTokenizerFactory();
    TokenStream stream = factory.create(reader);
    assertTokenStreamContents(stream, new String[] {"我是", "是中", "中国", "国人"});
  }
}
