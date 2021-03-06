package org.apache.solr.common.params;
import junit.framework.TestCase;
import java.util.HashMap;
import java.util.Map;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.DefaultSolrParams;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
public class SolrParamTest extends TestCase 
{  
  public void testGetParams() {
    Map<String,String> pmap = new HashMap<String, String>();
    pmap.put( "str"        , "string"   );
    pmap.put( "bool"       , "true"     );
    pmap.put( "true-0"     , "true"     );
    pmap.put( "true-1"     , "yes"      );
    pmap.put( "true-2"     , "on"       );
    pmap.put( "false-0"    , "false"    );
    pmap.put( "false-1"    , "off"      );
    pmap.put( "false-2"    , "no"       );
    pmap.put( "int"        , "100"      );
    pmap.put( "float"      , "10.6"     );
    pmap.put( "f.fl.str"   , "string"   );
    pmap.put( "f.fl.bool"  , "true"     );
    pmap.put( "f.fl.int"   , "100"      );
    pmap.put( "f.fl.float" , "10.6"     );
    pmap.put( "f.bad.bool" , "notbool"  );
    pmap.put( "f.bad.int"  , "notint"   );
    pmap.put( "f.bad.float", "notfloat" );
    final SolrParams params = new MapSolrParams( pmap );
    assertEquals(  "string"   , params.get( "str"       ) );
    assertEquals(  "true"     , params.get( "bool"      ) );
    assertEquals(  "100"      , params.get( "int"       ) );
    assertEquals(  "10.6"     , params.get( "float"     ) );
    assertEquals(  "string"   , params.get( "f.fl.str"    ) );
    assertEquals(  "true"     , params.get( "f.fl.bool"   ) );
    assertEquals(  "100"      , params.get( "f.fl.int"    ) );
    assertEquals(  "10.6"     , params.get( "f.fl.float"  ) );
    assertEquals(  "notbool"  , params.get( "f.bad.bool"  ) );
    assertEquals(  "notint"   , params.get( "f.bad.int"   ) );
    assertEquals(  "notfloat" , params.get( "f.bad.float" ) );
    final String  pstr = "string";
    final Boolean pbool = Boolean.TRUE;
    final Integer pint = new Integer( 100 );
    final Float   pfloat = new Float( 10.6f );
    assertEquals( pstr   , params.get(      "str"      ) );
    assertEquals( pbool  , params.getBool(  "bool"     ) );
    assertEquals( pint   , params.getInt(   "int"      ) );
    assertEquals( pfloat , params.getFloat( "float"    ) );
    assertEquals( pbool  , params.getBool(  "f.fl.bool"  ) );
    assertEquals( pint   , params.getInt(   "f.fl.int"   ) );
    assertEquals( pfloat , params.getFloat( "f.fl.float" ) );
    assertEquals( pstr   , params.getFieldParam( "fl", "str"  ) );
    assertEquals( pbool  , params.getFieldBool(  "fl", "bool" ) );
    assertEquals( pint   , params.getFieldInt(   "fl", "int"  ) );
    assertEquals( pfloat , params.getFieldFloat( "fl", "float" ) );
    assertEquals( pint   , params.getFieldInt( "fff",  "int"      ) );
    for( int i=0; i<3; i++ ) {
      assertEquals( Boolean.TRUE,  params.getBool( "true-"+i  ) );
      assertEquals( Boolean.FALSE, params.getBool( "false-"+i ) );
    }
    assertEquals( 400, getReturnCode( new Runnable() { public void run() { params.getInt(   "f.bad.int" ); } } ) );
    assertEquals( 400, getReturnCode( new Runnable() { public void run() { params.getBool(  "f.bad.bool" ); } } ) );
    assertEquals( 400, getReturnCode( new Runnable() { public void run() { params.getFloat( "f.bad.float" ); } } ) );
    assertNull( params.get( "asagdsaga" ) );
    assertNull( params.getBool( "asagdsaga" ) );
    assertNull( params.getInt( "asagdsaga" ) );
    assertNull( params.getFloat( "asagdsaga" ) );
    assertEquals( pstr                  , params.get(          "xxx", pstr   ) );
    assertEquals( pbool.booleanValue()  , params.getBool(      "xxx", pbool   ) );
    assertEquals( pint.intValue()       , params.getInt(       "xxx", pint   ) );
    assertEquals( pfloat.floatValue()   , params.getFloat(     "xxx", pfloat  ) );
    assertEquals( pbool.booleanValue()  , params.getFieldBool( "xxx", "bool", pbool ) );
    assertEquals( pint.intValue()       , params.getFieldInt(  "xxx", "int", pint  ) );
    assertEquals( pfloat.floatValue()   , params.getFieldFloat("xxx", "float", pfloat  ) );
    assertEquals( pstr                  , params.getFieldParam("xxx", "str", pstr  ) );
    final SolrParams required = params.required();
    assertEquals( pstr   , required.get(      "str"      ) );
    assertEquals( pbool  , required.getBool(  "bool"     ) );
    assertEquals( pint   , required.getInt(   "int"      ) );
    assertEquals( pfloat , required.getFloat( "float"    ) );
    assertEquals( pbool  , required.getFieldBool(  "fl", "bool" ) );
    assertEquals( pstr   , required.getFieldParams("fakefield", "str")[0] );
    assertEquals( pstr   , required.getFieldParam( "fakefield", "str"   ) );
    assertEquals( pbool  , required.getFieldBool(  "fakefield", "bool"  ) );
    assertEquals( pint   , required.getFieldInt(   "fakefield", "int"   ) );
    assertEquals( pfloat , required.getFieldFloat( "fakefield", "float" ) );
    assertEquals( 400, getReturnCode( new Runnable() { public void run() { required.get( "aaaa" ); } } ) );
    assertEquals( 400, getReturnCode( new Runnable() { public void run() { required.getInt(   "f.bad.int" ); } } ) );
    assertEquals( 400, getReturnCode( new Runnable() { public void run() { required.getBool(  "f.bad.bool" ); } } ) );
    assertEquals( 400, getReturnCode( new Runnable() { public void run() { required.getFloat( "f.bad.float" ); } } ) );
    assertEquals( 400, getReturnCode( new Runnable() { public void run() { required.getInt(   "aaa" ); } } ) );
    assertEquals( 400, getReturnCode( new Runnable() { public void run() { required.getBool(  "aaa" ); } } ) );
    assertEquals( 400, getReturnCode( new Runnable() { public void run() { required.getFloat( "aaa" ); } } ) );
    assertEquals( 400, getReturnCode( new Runnable() { public void run() { params.getFieldBool(  "bad", "bool" ); } } ) );
    assertEquals( 400, getReturnCode( new Runnable() { public void run() { params.getFieldInt(   "bad", "int"  ); } } ) );
    assertEquals(
        params.get(   "aaaa", "str" ), 
        required.get( "aaaa", "str" ) );
    assertEquals(
        params.getInt(   "f.bad.nnnn", pint ), 
        required.getInt( "f.bad.nnnn", pint ) );
    Map<String,String> dmap = new HashMap<String, String>();
    dmap.put( "dstr"               , "default"   );
    dmap.put( "dint"               , "123"       );
    dmap.put( "int"                , "456"       );
    SolrParams defaults = new DefaultSolrParams( params, new MapSolrParams( dmap ) );
    assertEquals( pstr                  , defaults.get( "str"      ) );
    assertEquals( "default"             , defaults.get( "dstr"      ) );
    assertEquals( new Integer(123)      , defaults.getInt(  "dint"     ) );
    assertEquals( pint                  , defaults.getInt(   "int"      ) );
    assertNull( defaults.get( "asagdsaga" ) );
  }
  public static int getReturnCode( Runnable runnable )
  {
    try {
      runnable.run();
    }
    catch( SolrException sx ) {
      return sx.code();
    }
    catch( Exception ex ) {
      ex.printStackTrace();
      return 500;
    }
    return 200;
  }
}
