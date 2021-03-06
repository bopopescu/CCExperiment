package org.apache.solr.response;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import java.util.*;
public class SolrQueryResponse {
  protected NamedList values = new SimpleOrderedMap();
  protected NamedList toLog = new SimpleOrderedMap();
  protected Set<String> defaultReturnFields;
  protected Exception err;
  protected boolean httpCaching=true;
  public SolrQueryResponse() {
  }
  public NamedList getValues() { return values; }
  public void setAllValues(NamedList nameValuePairs) {
    values=nameValuePairs;
  }
  public void setReturnFields(Set<String> fields) {
    defaultReturnFields=fields;
  }
  public Set<String> getReturnFields() {
    return defaultReturnFields;
  }
  public void add(String name, Object val) {
    values.add(name,val);
  }
  public void setException(Exception e) {
    err=e;
  }
  public Exception getException() {
    return err;
  }
  protected long endtime;
  public long getEndTime() {
    if (endtime==0) {
      setEndTime();
    }
    return endtime;
  }
  public long setEndTime() {
    return setEndTime(System.currentTimeMillis());
  }
  public long setEndTime(long endtime) {
    if (endtime!=0) {
      this.endtime=endtime;
    }
    return this.endtime;
  }
  public NamedList getResponseHeader() {
	  SimpleOrderedMap<Object> header = (SimpleOrderedMap<Object>) values.get("responseHeader");
	  return header;
  }
  public void addToLog(String name, Object val) {
	  toLog.add(name, val);
  }
  public NamedList getToLog() {
	  return toLog;
  }
  public void setHttpCaching(boolean httpCaching) {
    this.httpCaching=httpCaching;
  }
  public boolean isHttpCaching() {
    return this.httpCaching;
  }
}
