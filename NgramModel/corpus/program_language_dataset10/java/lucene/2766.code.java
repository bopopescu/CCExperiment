package org.apache.solr.common;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import junit.framework.TestCase;
public class SolrDocumentTest extends TestCase 
{
  public void testSimple() 
  {
    Float fval = new Float( 10.01f );
    Boolean bval = Boolean.TRUE;
    String sval = "12qwaszx";
    SolrDocument doc = new SolrDocument();
    doc.addField( "f", fval );
    doc.addField( "b", bval );
    doc.addField( "s", sval );
    doc.addField( "f", 100 ); 
    assertEquals( fval, doc.getFirstValue( "f" ) );
    assertEquals( fval, doc.getFieldValues( "f" ).iterator().next() );
    assertEquals( fval, ((Collection<Object>)doc.getFieldValue( "f" )).iterator().next() );
    assertEquals( bval, doc.getFieldValue( "b" ) );
    assertEquals( sval, doc.getFieldValue( "s" ) );
    assertEquals( 2, doc.getFieldValues( "f" ).size() );
    assertNull( doc.getFieldValue( "xxxxx" ) );
    assertNull( doc.getFieldValues( "xxxxx" ) );
    List<String> keys = new ArrayList<String>();
    for( String s : doc.getFieldNames() ) {
      keys.add( s );
    }
    Collections.sort( keys );
    assertEquals( 3, keys.size() );
    assertEquals( "[b, f, s]", keys.toString() );
    doc.setField( "f", fval );
    assertEquals( 1, doc.getFieldValues( "f" ).size() );
    assertEquals( fval, doc.getFieldValue( "f" ) );
    doc.setField( "n", null );
    assertEquals( null, doc.getFieldValue( "n" ) );
    assertEquals( true, doc.removeFields( "f" ) );
    assertEquals( false, doc.removeFields( "asdgsadgas" ) );
    assertNull( doc.getFieldValue( "f" ) );
    assertNull( doc.getFieldValues( "f" ) );
  }
  public void testUnsupportedStuff()
  {
    SolrDocument doc = new SolrDocument();
    try { doc.getFieldValueMap().clear();               fail( "should be unsupported!" ); } catch( UnsupportedOperationException ex ){}
    try { doc.getFieldValueMap().containsValue( null ); fail( "should be unsupported!" ); } catch( UnsupportedOperationException ex ){}
    try { doc.getFieldValueMap().entrySet();            fail( "should be unsupported!" ); } catch( UnsupportedOperationException ex ){}
    try { doc.getFieldValueMap().putAll( null );        fail( "should be unsupported!" ); } catch( UnsupportedOperationException ex ){}
    try { doc.getFieldValueMap().values();              fail( "should be unsupported!" ); } catch( UnsupportedOperationException ex ){}
    try { doc.getFieldValueMap().remove( "key" );       fail( "should be unsupported!" ); } catch( UnsupportedOperationException ex ){}
    try { doc.getFieldValueMap().put( "key", "value" ); fail( "should be unsupported!" ); } catch( UnsupportedOperationException ex ){}
    try { doc.getFieldValuesMap().clear();               fail( "should be unsupported!" ); } catch( UnsupportedOperationException ex ){}
    try { doc.getFieldValuesMap().containsValue( null ); fail( "should be unsupported!" ); } catch( UnsupportedOperationException ex ){}
    try { doc.getFieldValuesMap().entrySet();            fail( "should be unsupported!" ); } catch( UnsupportedOperationException ex ){}
    try { doc.getFieldValuesMap().putAll( null );        fail( "should be unsupported!" ); } catch( UnsupportedOperationException ex ){}
    try { doc.getFieldValuesMap().values();              fail( "should be unsupported!" ); } catch( UnsupportedOperationException ex ){}
    try { doc.getFieldValuesMap().remove( "key" );       fail( "should be unsupported!" ); } catch( UnsupportedOperationException ex ){}
    try { doc.getFieldValueMap().put( "key", Collections.EMPTY_LIST ); fail( "should be unsupported!" ); } catch( UnsupportedOperationException ex ){}
    assertEquals( null, doc.getFieldValueMap().get( "aaa" ) );
    doc.setField( "aaa", "bbb" );
    assertEquals( "bbb", doc.getFieldValueMap().get( "aaa" ) );
  }
  public void testAddCollections()
  {
    final List<String> c0 = new ArrayList<String>();
    c0.add( "aaa" );
    c0.add( "aaa" );
    c0.add( "aaa" );
    c0.add( "bbb" );
    c0.add( "ccc" );
    c0.add( "ddd" );
    SolrDocument doc = new SolrDocument();
    doc.addField( "v", c0 );
    assertEquals( c0.size(), doc.getFieldValues("v").size() );
    assertEquals( c0.get(0), doc.getFirstValue( "v" ) );
    Object[] arr = new Object[] { "aaa", "aaa", "aaa", 10, 'b' };
    doc = new SolrDocument();
    doc.addField( "v", arr );
    assertEquals( arr.length, doc.getFieldValues("v").size() );
    doc.setField( "v", arr );
    assertEquals( arr.length, doc.getFieldValues("v").size() );
    doc.clear();
    assertEquals( 0, doc.getFieldNames().size() );
    Iterable iter = new Iterable() {
      public Iterator iterator() {
        return c0.iterator();
      }
    };
    doc.addField( "v", iter );
    assertEquals( c0.size(), doc.getFieldValues("v").size() );
    doc.addField( "v", iter );
    assertEquals( c0.size()*2, doc.getFieldValues("v").size() );
    doc.setField( "empty", new ArrayList<String>() );
    assertNull( doc.getFirstValue( "empty" ) );
    assertFalse( doc.getFieldValueMap().isEmpty() );
    assertFalse( doc.getFieldValuesMap().isEmpty() );
    assertEquals( 2, doc.getFieldValueMap().size() );
    assertEquals( 2, doc.getFieldValuesMap().size() );
    assertTrue( doc.getFieldValueMap().containsKey( "v" ) );
    assertTrue( doc.getFieldValuesMap().containsKey( "v" ) );
    assertTrue( doc.getFieldValueMap().keySet().contains( "v" ) );
    assertTrue( doc.getFieldValuesMap().keySet().contains( "v" ) );
    assertFalse( doc.getFieldValueMap().containsKey( "g" ) );
    assertFalse( doc.getFieldValuesMap().containsKey( "g" ) );
    assertFalse( doc.getFieldValueMap().keySet().contains( "g" ) );
    assertFalse( doc.getFieldValuesMap().keySet().contains( "g" ) );
  }
  public void testDuplicate() 
  {
    Float fval0 = new Float( 10.01f );
    Float fval1 = new Float( 11.01f );
    Float fval2 = new Float( 12.01f );
    SolrInputDocument doc = new SolrInputDocument();
    for( int i=0; i<5; i++ ) {
      doc.addField( "f", fval0, 1.0f );
      doc.addField( "f", fval1, 1.0f );
      doc.addField( "f", fval2, 1.0f );
    }
    assertEquals( (3*5), doc.getField("f").getValueCount() );
  }
  public void testMapInterface()
  {
    SolrDocument doc = new SolrDocument();
    assertTrue( doc instanceof Map );
    assertTrue( Map.class.isAssignableFrom( SolrDocument.class ) );
    SolrInputDocument indoc = new SolrInputDocument();
    assertTrue( indoc instanceof Map );
    assertTrue( Map.class.isAssignableFrom( indoc.getClass() ) );
  }
}
