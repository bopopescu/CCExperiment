package org.apache.solr.servlet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.util.DateUtil;
public class CacheHeaderTest extends CacheHeaderTestBase {
  @Override
  public String getSolrConfigFilename() {
    return "solrconfig.xml";
  }
  protected static final String CHARSET = "UTF-8";
  protected static final String CONTENTS = "id\n100\n101\n102";
  public void testCacheVetoHandler() throws Exception {
    File f=makeFile(CONTENTS);
    HttpMethodBase m=getUpdateMethod("GET");
    m.setQueryString(new NameValuePair[] { new NameValuePair("stream.file",f.getCanonicalPath())});
    getClient().executeMethod(m);
    assertEquals(200, m.getStatusCode());
    checkVetoHeaders(m);
  }
  public void testCacheVetoException() throws Exception {
    HttpMethodBase m = getSelectMethod("GET");
    m.setQueryString(new NameValuePair[] { new NameValuePair("q", "xyz:solr"),
        new NameValuePair("qt", "standard") });
    getClient().executeMethod(m);
    assertFalse(m.getStatusCode() == 200);
    checkVetoHeaders(m);
  }
  protected void checkVetoHeaders(HttpMethodBase m) throws Exception {
    Header head = m.getResponseHeader("Cache-Control");
    assertNotNull("We got no Cache-Control header", head);
    assertEquals("no-cache, no-store", head.getValue());
    head = m.getResponseHeader("Pragma");
    assertNotNull("We got no Pragma header", head);
    assertEquals("no-cache", head.getValue());
    head = m.getResponseHeader("Expires");
    assertNotNull("We got no Expires header", head);
    Date d = DateUtil.parseDate(head.getValue());
    assertTrue("We got no Expires header far in the past", System
        .currentTimeMillis()
        - d.getTime() > 100000);
  }
  protected void doLastModified(String method) throws Exception {
    HttpMethodBase get = getSelectMethod(method);
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals("Got no response code 200 in initial request", 200, get
        .getStatusCode());
    Header head = get.getResponseHeader("Last-Modified");
    assertNotNull("We got no Last-Modified header", head);
    Date lastModified = DateUtil.parseDate(head.getValue());
    get = getSelectMethod(method);
    get.addRequestHeader("If-Modified-Since", DateUtil.formatDate(new Date()));
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals("Expected 304 NotModified response with current date", 304,
        get.getStatusCode());
    get = getSelectMethod(method);
    get.addRequestHeader("If-Modified-Since", DateUtil.formatDate(new Date(
        lastModified.getTime() - 10000)));
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals("Expected 200 OK response with If-Modified-Since in the past",
        200, get.getStatusCode());
    get = getSelectMethod(method);
    get.addRequestHeader("If-Unmodified-Since", DateUtil.formatDate(new Date(
        lastModified.getTime() - 10000)));
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals(
        "Expected 412 Precondition failed with If-Unmodified-Since in the past",
        412, get.getStatusCode());
    get = getSelectMethod(method);
    get
        .addRequestHeader("If-Unmodified-Since", DateUtil
            .formatDate(new Date()));
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals(
        "Expected 200 OK response with If-Unmodified-Since and current date",
        200, get.getStatusCode());
  }
  protected void doETag(String method) throws Exception {
    HttpMethodBase get = getSelectMethod(method);
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals("Got no response code 200 in initial request", 200, get
        .getStatusCode());
    Header head = get.getResponseHeader("ETag");
    assertNotNull("We got no ETag in the response", head);
    assertTrue("Not a valid ETag", head.getValue().startsWith("\"")
        && head.getValue().endsWith("\""));
    String etag = head.getValue();
    get = getSelectMethod(method);
    get.addRequestHeader("If-None-Match", "\"xyz123456\"");
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals(
        "If-None-Match: Got no response code 200 in response to non matching ETag",
        200, get.getStatusCode());
    get = getSelectMethod(method);
    get.addRequestHeader("If-None-Match", "\"xyz1223\"");
    get.addRequestHeader("If-None-Match", "\"1231323423\", \"1211211\",   "
        + etag);
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals("If-None-Match: Got no response 304 to matching ETag", 304,
        get.getStatusCode());
    get = getSelectMethod(method);
    get.addRequestHeader("If-None-Match", "*");
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals("If-None-Match: Got no response 304 for star ETag", 304, get
        .getStatusCode());
    get = getSelectMethod(method);
    get.addRequestHeader("If-Match", "\"xyz123456\"");
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals(
        "If-Match: Got no response code 412 in response to non matching ETag",
        412, get.getStatusCode());
    get = getSelectMethod(method);
    get.addRequestHeader("If-Match", "\"xyz1223\"");
    get.addRequestHeader("If-Match", "\"1231323423\", \"1211211\",   " + etag);
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals("If-Match: Got no response 200 to matching ETag", 200, get
        .getStatusCode());
    get = getSelectMethod(method);
    get.addRequestHeader("If-Match", "*");
    getClient().executeMethod(get);
    checkResponseBody(method, get);
    assertEquals("If-Match: Got no response 200 to star ETag", 200, get
        .getStatusCode());
  }
  protected void doCacheControl(String method) throws Exception {
    if ("POST".equals(method)) {
      HttpMethodBase m = getSelectMethod(method);
      getClient().executeMethod(m);
      checkResponseBody(method, m);
      Header head = m.getResponseHeader("Cache-Control");
      assertNull("We got a cache-control header in response to POST", head);
      head = m.getResponseHeader("Expires");
      assertNull("We got an Expires  header in response to POST", head);
    } else {
      HttpMethodBase m = getSelectMethod(method);
      getClient().executeMethod(m);
      checkResponseBody(method, m);
      Header head = m.getResponseHeader("Cache-Control");
      assertNotNull("We got no cache-control header", head);
      head = m.getResponseHeader("Expires");
      assertNotNull("We got no Expires header in response", head);
    }
  }
  protected File makeFile(String contents) {
    return makeFile(contents, CHARSET);
  }
  protected File makeFile(String contents, String charset) {
    try {
      File f = File.createTempFile(getClass().getName(),"csv");
      f.deleteOnExit();
      Writer out = new OutputStreamWriter(new FileOutputStream(f),
          charset);
      out.write(contents);
      out.close();
      return f;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
