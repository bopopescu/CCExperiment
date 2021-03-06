package org.apache.lucene.analysis.sinks;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Locale;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TeeSinkTokenFilter;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.TeeSinkTokenFilter.SinkTokenStream;
public class DateRecognizerSinkTokenizerTest extends BaseTokenStreamTestCase {
  public DateRecognizerSinkTokenizerTest(String s) {
    super(s);
  }
  public void test() throws IOException {
    DateRecognizerSinkFilter sinkFilter = new DateRecognizerSinkFilter(new SimpleDateFormat("MM/dd/yyyy", Locale.US));
    String test = "The quick red fox jumped over the lazy brown dogs on 7/11/2006  The dogs finally reacted on 7/12/2006";
    TeeSinkTokenFilter tee = new TeeSinkTokenFilter(new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(test)));
    SinkTokenStream sink = tee.newSinkTokenStream(sinkFilter);
    int count = 0;
    tee.reset();
    while (tee.incrementToken()) {
      count++;
    }
    assertTrue(count + " does not equal: " + 18, count == 18);
    int sinkCount = 0;
    sink.reset();
    while (sink.incrementToken()) {
      sinkCount++;
    }
    assertTrue("sink Size: " + sinkCount + " is not: " + 2, sinkCount == 2);
  }
}