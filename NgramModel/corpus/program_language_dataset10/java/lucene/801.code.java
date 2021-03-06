package org.apache.lucene.analysis.sinks;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TeeSinkTokenFilter;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.TeeSinkTokenFilter.SinkTokenStream;
public class TokenRangeSinkTokenizerTest extends BaseTokenStreamTestCase {
  public TokenRangeSinkTokenizerTest(String s) {
    super(s);
  }
  public void test() throws IOException {
    TokenRangeSinkFilter sinkFilter = new TokenRangeSinkFilter(2, 4);
    String test = "The quick red fox jumped over the lazy brown dogs";
    TeeSinkTokenFilter tee = new TeeSinkTokenFilter(new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(test)));
    SinkTokenStream rangeToks = tee.newSinkTokenStream(sinkFilter);
    int count = 0;
    tee.reset();
    while(tee.incrementToken()) {
      count++;
    }
    int sinkCount = 0;
    rangeToks.reset();
    while (rangeToks.incrementToken()) {
      sinkCount++;
    }
    assertTrue(count + " does not equal: " + 10, count == 10);
    assertTrue("rangeToks Size: " + sinkCount + " is not: " + 2, sinkCount == 2);
  }
}