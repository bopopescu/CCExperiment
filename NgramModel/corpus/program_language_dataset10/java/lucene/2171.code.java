package org.apache.solr.common.params;
import org.apache.solr.common.util.StrUtils;
import java.util.Iterator;
import java.util.Map;
import java.io.IOException;
public class MultiMapSolrParams extends SolrParams {
  protected final Map<String,String[]> map;
  public static void addParam(String name, String val, Map<String,String[]> map) {
      String[] arr = map.get(name);
      if (arr ==null) {
        arr =new String[]{val};
      } else {
        String[] newarr = new String[arr.length+1];
        System.arraycopy(arr,0,newarr,0,arr.length);
        newarr[arr.length]=val;
        arr =newarr;
      }
      map.put(name, arr);
  }
  public MultiMapSolrParams(Map<String,String[]> map) {
    this.map = map;
  }
  @Override
  public String get(String name) {
    String[] arr = map.get(name);
    return arr==null ? null : arr[0];
  }
  @Override
  public String[] getParams(String name) {
    return map.get(name);
  }
  @Override
  public Iterator<String> getParameterNamesIterator() {
    return map.keySet().iterator();
  }
  public Map<String,String[]> getMap() { return map; }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(128);
    try {
      boolean first=true;
      for (Map.Entry<String,String[]> entry : map.entrySet()) {
        String key = entry.getKey();
        String[] valarr = entry.getValue();
        for (String val : valarr) {
          if (!first) sb.append('&');
          first=false;
          sb.append(key);
          sb.append('=');
          StrUtils.partialURLEncodeVal(sb, val==null ? "" : val);
        }
      }
    }
    catch (IOException e) {throw new RuntimeException(e);}  
    return sb.toString();
  }
}
