package org.apache.solr.handler.dataimport;
import junit.framework.Assert;
import static org.apache.solr.handler.dataimport.AbstractDataImportHandlerTest.createMap;
import org.junit.Test;
import java.io.StringReader;
import java.util.Properties;
public class TestPlainTextEntityProcessor {
  @Test
  public void simple() {
    DataImporter di = new DataImporter();
    di.loadAndInit(DATA_CONFIG);
    TestDocBuilder.SolrWriterImpl sw = new TestDocBuilder.SolrWriterImpl();
    DataImporter.RequestParams rp = new DataImporter.RequestParams(createMap("command", "full-import"));
    di.runCmd(rp, sw);
    Assert.assertEquals(DS.s, sw.docs.get(0).getFieldValue("x"));
  }
  public static class DS extends DataSource {
    static String s = "hello world";
    public void init(Context context, Properties initProps) {
    }
    public Object getData(String query) {
      return new StringReader(s);
    }
    public void close() {
    }
  }
  static String DATA_CONFIG = "<dataConfig>\n" +
          "\t<dataSource type=\"TestPlainTextEntityProcessor$DS\" />\n" +
          "\t<document>\n" +
          "\t\t<entity processor=\"PlainTextEntityProcessor\" name=\"x\" query=\"x\">\n" +
          "\t\t\t<field column=\"plainText\" name=\"x\" />\n" +
          "\t\t</entity>\n" +
          "\t</document>\n" +
          "</dataConfig>";
}
