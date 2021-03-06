package org.apache.solr.request;
import org.apache.solr.response.BinaryQueryResponseWriter;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.AbstractSolrTestCase;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.impl.BinaryResponseParser;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import java.util.ArrayList;
import java.io.*;
public class TestWriterPerf extends AbstractSolrTestCase {
  public String getSchemaFile() { return "schema11.xml"; }
  public String getSolrConfigFile() { return "solrconfig-functionquery.xml"; }
  public String getCoreName() { return "basic"; }
  public void setUp() throws Exception {
    super.setUp();
  }
  public void tearDown() throws Exception {
    super.tearDown();
  }
  String id = "id";
  String t1 = "f_t";
  String i1 = "f_i";
  String tag = "f_ss";
  void index(Object... olst) {
    ArrayList<String> lst = new ArrayList<String>();
    for (Object o : olst) lst.add(o.toString());
    assertU(adoc(lst.toArray(new String[lst.size()])));
  }
  void makeIndex() {
    index(id,1, i1, 100,t1,"now is the time for all good men", tag,"patriotic");
    index(id,2, i1, 50 ,t1,"to come to the aid of their country.", tag,"patriotic",tag,"country",tag,"nation",tag,"speeches");
    index(id,3, i1, 2 ,t1,"how now brown cow", tag,"cow",tag,"jersey");
    index(id,4, i1, -100 ,t1,"the quick fox jumped over the lazy dog",tag,"fox",tag,"dog",tag,"quick",tag,"slow",tag,"lazy");
    index(id,5, i1, 50 ,t1,"the quick fox jumped way over the lazy dog",tag,"fox",tag,"dog");
    index(id,6, i1, -60 ,t1,"humpty dumpy sat on a wall",tag,"humpty",tag,"dumpty");
    index(id,7, i1, 123 ,t1,"humpty dumpy had a great fall",tag,"accidents");
    index(id,8, i1, 876 ,t1,"all the kings horses and all the kings men",tag,"king",tag,"horses",tag,"trouble");
    index(id,9, i1, 7 ,t1,"couldn't put humpty together again",tag,"humpty",tag,"broken");
    index(id,10, i1, 4321 ,t1,"this too shall pass",tag,"1",tag,"2",tag,"infinity");
    index(id,11, i1, 33 ,t1,"An eye for eye only ends up making the whole world blind.",tag,"ouch",tag,"eye",tag,"peace",tag,"world");
    index(id,12, i1, 379 ,t1,"Great works are performed, not by strength, but by perseverance.",tag,"herculese",tag,"strong",tag,"stubborn");
    assertU(optimize());
  }
  public SolrQueryResponse getResponse(SolrQueryRequest req) throws IOException, Exception {
    SolrQueryResponse rsp = new SolrQueryResponse();
    h.getCore().execute(h.getCore().getRequestHandler(null),req,rsp);
    if (rsp.getException() != null) {
      throw rsp.getException();
    }
    return rsp;
  }
  void doPerf(String writerName, SolrQueryRequest req, int encIter, int decIter) throws Exception {
    SolrQueryResponse rsp = getResponse(req);
    QueryResponseWriter w = h.getCore().getQueryResponseWriter(writerName);
    ByteArrayOutputStream out=null;
    System.gc();
    long start = System.currentTimeMillis();
    for (int i=0; i<encIter; i++) {
    if (w instanceof BinaryQueryResponseWriter) {
      BinaryQueryResponseWriter binWriter = (BinaryQueryResponseWriter) w;
      out = new ByteArrayOutputStream();
      binWriter.write(out, req, rsp);
      out.close();
    } else {
      out = new ByteArrayOutputStream();
      Writer writer = new OutputStreamWriter(out, "UTF-8");
      w.write(writer, req, rsp);
      writer.close();
    }
    }
    long encodeTime = Math.max(System.currentTimeMillis() - start, 1);
    byte[] arr = out.toByteArray();
    start = System.currentTimeMillis();
    writerName = writerName.intern();
    for (int i=0; i<decIter; i++) {
      ResponseParser rp = null;
      if (writerName == "xml") {
        rp = new XMLResponseParser();
      } else if (writerName == "javabin") {
        rp = new BinaryResponseParser();
      } else {
        break;
      }
      ByteArrayInputStream in = new ByteArrayInputStream(arr);
      rp.processResponse(in, "UTF-8");      
    }
    long decodeTime = Math.max(System.currentTimeMillis() - start, 1);
    System.out.println("writer "+writerName+", size="+out.size()+", encodeRate="+(encodeTime==1 ? "N/A":  ""+(encIter*1000L/encodeTime)) + ", decodeRate="+(decodeTime==1 ? "N/A":  ""+(decIter*1000L/decodeTime)) );
    req.close();
  }
  public void testPerf() throws Exception {
    makeIndex();
    SolrQueryRequest req = req("q", "id:[* TO *] all country"
                    ,"start","0"
                    ,"rows","100"
                    ,"echoParams","all"
                    ,"fl","*,score"
                    ,"indent","false"
                    ,"facet","true"
                    ,"facet.field", i1
                    ,"facet.field", tag
                    ,"facet.field", t1
                    ,"facet.mincount","0"
                    ,"facet.offset","0"
                    ,"facet.limit","100"
                    ,"facet.sort","count"
                    ,"hl","true"
                    ,"hl.fl","t1"
            );
    doPerf("xml", req, 2,2);
    doPerf("json", req, 2,2);
    doPerf("javabin", req, 2,2);
    int encIter=20000;
    int decIter=50000;
  }
}
