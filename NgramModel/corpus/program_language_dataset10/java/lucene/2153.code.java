package org.apache.solr.common;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
public class SolrDocument implements Map<String,Object>, Iterable<Map.Entry<String, Object>>, Serializable
{
  private final Map<String,Object> _fields;
  public SolrDocument()
  {
    _fields = new LinkedHashMap<String,Object>();
  }
  public Collection<String> getFieldNames() {
    return _fields.keySet();
  }
  public void clear()
  {
    _fields.clear();
  }
  public boolean removeFields(String name) 
  {
    return _fields.remove( name ) != null;
  }
  @SuppressWarnings("unchecked")
  public void setField(String name, Object value) 
  {
    if( value instanceof Object[] ) {
      value = new ArrayList(Arrays.asList( (Object[])value ));
    }
    else if( value instanceof Collection ) {
    }
    else if( value instanceof Iterable ) {
      ArrayList<Object> lst = new ArrayList<Object>();
      for( Object o : (Iterable)value ) {
        lst.add( o );
      }
      value = lst;
    }
    _fields.put(name, value);
  }
  @SuppressWarnings("unchecked")
  public void addField(String name, Object value) 
  { 
    Object existing = _fields.get(name);
    if (existing == null) {
      this.setField( name, value );
      return;
    }
    Collection<Object> vals = null;
    if( existing instanceof Collection ) {
      vals = (Collection<Object>)existing;
    }
    else {
      vals = new ArrayList<Object>( 3 );
      vals.add( existing );
    }
    if( value instanceof Iterable ) {
      for( Object o : (Iterable<Object>)value ) {
        vals.add( o );
      }
    }
    else if( value instanceof Object[] ) {
      for( Object o : (Object[])value ) {
        vals.add( o );
      }
    }
    else {
      vals.add( value );
    }
    _fields.put( name, vals );
  }
  public Object getFirstValue(String name) {
    Object v = _fields.get( name );
    if (v == null || !(v instanceof Collection)) return v;
    Collection c = (Collection)v;
    if (c.size() > 0 ) {
      return c.iterator().next();
    }
    return null;
  }
  public Object getFieldValue(String name) {
    return _fields.get( name );
  }
  @SuppressWarnings("unchecked")
  public Collection<Object> getFieldValues(String name) {
    Object v = _fields.get( name );
    if( v instanceof Collection ) {
      return (Collection<Object>)v;
    }
    if( v != null ) {
      ArrayList<Object> arr = new ArrayList<Object>(1);
      arr.add( v );
      return arr;
    }
    return null;
  }
  @Override
  public String toString()
  {
    return "SolrDocument["+_fields.toString()+"]";
  }
  public Iterator<Entry<String, Object>> iterator() {
    return _fields.entrySet().iterator();
  }
  public Map<String,Collection<Object>> getFieldValuesMap()
  {
    return new Map<String,Collection<Object>>() {
      public Collection<Object> get(Object key) { 
        return getFieldValues( (String)key ); 
      }
      public boolean containsKey(Object key) { return _fields.containsKey( key ); }
      public Set<String>  keySet()           { return _fields.keySet();  }
      public int          size()             { return _fields.size();    }
      public boolean      isEmpty()          { return _fields.isEmpty(); }
      public void clear() { throw new UnsupportedOperationException(); }
      public boolean containsValue(Object value) {throw new UnsupportedOperationException();}
      public Set<java.util.Map.Entry<String, Collection<Object>>> entrySet() {throw new UnsupportedOperationException();}
      public void putAll(Map<? extends String, ? extends Collection<Object>> t) {throw new UnsupportedOperationException();}
      public Collection<Collection<Object>> values() {throw new UnsupportedOperationException();}
      public Collection<Object> put(String key, Collection<Object> value) {throw new UnsupportedOperationException();}
      public Collection<Object> remove(Object key) {throw new UnsupportedOperationException();}
      public String toString() {return _fields.toString();}
    };
  }
  public Map<String,Object> getFieldValueMap() {
    return new Map<String,Object>() {
      public Object get(Object key) { 
        return getFirstValue( (String)key ); 
      }
      public boolean containsKey(Object key) { return _fields.containsKey( key ); }
      public Set<String>  keySet()           { return _fields.keySet();  }
      public int          size()             { return _fields.size();    }
      public boolean      isEmpty()          { return _fields.isEmpty(); }
      public void clear() { throw new UnsupportedOperationException(); }
      public boolean containsValue(Object value) {throw new UnsupportedOperationException();}
      public Set<java.util.Map.Entry<String, Object>> entrySet() {throw new UnsupportedOperationException();}
      public void putAll(Map<? extends String, ? extends Object> t) {throw new UnsupportedOperationException();}
      public Collection<Object> values() {throw new UnsupportedOperationException();}
      public Collection<Object> put(String key, Object value) {throw new UnsupportedOperationException();}
      public Collection<Object> remove(Object key) {throw new UnsupportedOperationException();}      
      public String toString() {return _fields.toString();}
   };
  }
  public boolean containsKey(Object key) {
    return _fields.containsKey(key);
  }
  public boolean containsValue(Object value) {
    return _fields.containsValue(value);
  }
  public Set<Entry<String, Object>> entrySet() {
    return _fields.entrySet();
  }
  public Object get(Object key) {
    return _fields.get(key);
  }
  public boolean isEmpty() {
    return _fields.isEmpty();
  }
  public Set<String> keySet() {
    return _fields.keySet();
  }
  public Object put(String key, Object value) {
    return _fields.put(key, value);
  }
  public void putAll(Map<? extends String, ? extends Object> t) {
    _fields.putAll( t );
  }
  public Object remove(Object key) {
    return _fields.remove(key);
  }
  public int size() {
    return _fields.size();
  }
  public Collection<Object> values() {
    return _fields.values();
  }
}
