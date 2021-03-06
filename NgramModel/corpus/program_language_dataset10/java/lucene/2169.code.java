package org.apache.solr.common.params;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
public class ModifiableSolrParams extends SolrParams 
{
  private Map<String,String[]> vals;
  public ModifiableSolrParams()
  {
    vals = new LinkedHashMap<String, String[]>();
  }
  public ModifiableSolrParams( Map<String,String[]> v )
  {
    vals = v;
  }
  public ModifiableSolrParams(SolrParams params)
  {
    vals = new LinkedHashMap<String, String[]>();
    if( params != null ) {
      this.add( params );
    }
  }
  public ModifiableSolrParams set( String name, String ... val ) {
    if (val==null || (val.length==1 && val[0]==null)) {
      vals.remove(name);
    } else {
      vals.put( name, val );
    }
    return this;
  }
  public ModifiableSolrParams set( String name, int val ) {
    set( name, String.valueOf(val) );
    return this;
  }
  public ModifiableSolrParams set( String name, boolean val ) {
    set( name, String.valueOf(val) );
    return this;
  }
  public ModifiableSolrParams add( String name, String ... val ) {
    String[] old = vals.put(name, val);
    if( old != null ) {
      if( val == null || val.length < 1 ) {
        String[] both = new String[old.length+1];
        System.arraycopy(old, 0, both, 0, old.length);
        both[old.length] = null;
        vals.put( name, both );
      }
      else {
        String[] both = new String[old.length+val.length];
        System.arraycopy(old, 0, both, 0, old.length);
        System.arraycopy(val, 0, both, old.length, val.length);
        vals.put( name, both );
      }
    }
    return this;
  }
  public void add(SolrParams params)
  {
    Iterator<String> names = params.getParameterNamesIterator();
    while (names.hasNext()) {
      String name = names.next();
      set(name, params.getParams(name));
    }
  }
  public String[] remove( String name )
  {
    return vals.remove( name );
  }
  public void clear()
  {
    vals.clear();
  }
  public boolean remove(String name, String value) {
     String[] tmp = vals.get(name);
     if (tmp==null) return false;
     for (int i=0; i<tmp.length; i++) {
       if (tmp[i].equals(value)) {
         String[] tmp2 = new String[tmp.length-1];
         if (tmp2.length==0) {
           tmp2 = null;
           remove(name);
         } else {
           System.arraycopy(tmp, 0, tmp2, 0, i);
           System.arraycopy(tmp, i+1, tmp2, i, tmp.length-i-1);
           set(name, tmp2);
         }
         return true;
       }
     }
     return false;
  }
  @Override
  public String get(String param) {
    String[] v = vals.get( param );
    if( v!= null && v.length > 0 ) {
      return v[0];
    }
    return null;
  }
  @Override
  public Iterator<String> getParameterNamesIterator() {
    return vals.keySet().iterator();
  }
  public Set<String> getParameterNames() {
    return vals.keySet();
  }
  @Override
  public String[] getParams(String param) {
    return vals.get( param );
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(128);
    try {
      boolean first=true;
      for (Map.Entry<String,String[]> entry : vals.entrySet()) {
        String key = entry.getKey();
        String[] valarr = entry.getValue();
        for (String val : valarr) {
          if (!first) sb.append('&');
          first=false;
          sb.append(key);
          sb.append('=');
          if( val != null ) {
            sb.append( URLEncoder.encode( val, "UTF-8" ) );
          }
        }
      }
    }
    catch (IOException e) {throw new RuntimeException(e);}  
    return sb.toString();
  }
}
