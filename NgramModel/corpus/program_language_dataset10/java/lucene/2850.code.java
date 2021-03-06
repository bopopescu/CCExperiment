package org.apache.solr.search;
import org.apache.solr.util.AbstractSolrTestCase;
public class TestQueryTypes extends AbstractSolrTestCase {
  public String getSchemaFile() { return "schema11.xml"; }
  public String getSolrConfigFile() { return "solrconfig.xml"; }
  public String getCoreName() { return "basic"; }
  public void setUp() throws Exception {
    super.setUp();
  }
  public void tearDown() throws Exception {
    super.tearDown();
  }
  public void testQueryTypes() {
    assertU(adoc("id","1", "v_t","Hello Dude"));
    assertU(adoc("id","2", "v_t","Hello Yonik"));
    assertU(adoc("id","3", "v_s","{!literal}"));
    assertU(adoc("id","4", "v_s","other stuff"));
    assertU(adoc("id","5", "v_f","3.14159"));
    assertU(adoc("id","6", "v_f","8983"));
    assertU(adoc("id","7", "v_f","1.5"));
    assertU(adoc("id","8", "v_ti","5"));
    assertU(adoc("id","9", "v_s","internal\"quote"));
    Object[] arr = new Object[] {
    "id",999.0
    ,"v_s","wow dude"
    ,"v_t","wow"
    ,"v_ti",-1
    ,"v_tis",-1
    ,"v_tl",-1234567891234567890L
    ,"v_tls",-1234567891234567890L
    ,"v_tf",-2.0f
    ,"v_tfs",-2.0f
    ,"v_td",-2.0
    ,"v_tds",-2.0
    ,"v_tdt","2000-05-10T01:01:01Z"
    ,"v_tdts","2002-08-26T01:01:01Z"
    };
    String[] sarr = new String[arr.length];
    for (int i=0; i<arr.length; i++) {
      sarr[i] = arr[i].toString();
    }
    assertU(adoc(sarr));
    assertU(optimize());
    for (int i=0; i<arr.length; i+=2) {
      String f = arr[i].toString();
      String v = arr[i+1].toString();
      assertQ(req( "q",f+":\""+v+'"')
              ,"//result[@numFound='1']"
              ,"//*[@name='id'][.='999.0']"
              ,"//*[@name='" + f + "'][.='" + v + "']"
              );
      assertQ(req( "q", "{!field f="+f+"}"+v)
              ,"//result[@numFound='1']"
              );
      assertQ(req( "q", f + ":[\"" + v + "\" TO \"" + v + "\"]" )
              ,"//result[@numFound='1']"
              );
      assertQ(req( "q", "{!frange v="+f+" l='"+v+"' u='"+v+"'}" )
              ,"//result[@numFound='1']"
              );
       assertQ(req( "q", "+id:999 _val_:\"" + f + "\"")
            ,"//result[@numFound='1']"
        );
    }
    assertQ("test prefix query",
            req("q","{!prefix f=v_t}hel")
            ,"//result[@numFound='2']"
            );
    assertQ("test raw query",
            req("q","{!raw f=v_t}hello")
            ,"//result[@numFound='2']"
            );
    assertQ("test raw query",
            req("q","{!raw f=v_t}Hello")
            ,"//result[@numFound='0']"
            );
    assertQ("test raw query",
            req("q","{!raw f=v_f}1.5")
            ,"//result[@numFound='0']"
            );
    assertQ(req("q","{!raw f=v_s}internal\"quote")
            ,"//result[@numFound='1']"
            );
    assertQ(req("q","{!raw f=v_s v='internal\"quote'}")
            ,"//result[@numFound='1']"
            );
    assertQ(req("q","{!raw f=v_s v='internal\\\"quote'}")
            ,"//result[@numFound='1']"
            );
    assertQ(req("q","{!raw f=v_s v=\"internal\\u0022quote\"}")
            ,"//result[@numFound='1']"
            );
    assertQ(req("q","{!raw f=v_s v=\"internal\\\"quote\"}")
            ,"//result[@numFound='1']"
            );
    assertQ("test custom plugin query",
            req("q","{!foo f=v_t}hello")
            ,"//result[@numFound='2']"
            );
    assertQ("test single term field query on text type",
            req("q","{!field f=v_t}HELLO")
            ,"//result[@numFound='2']"
            );
    assertQ("test single term field query on type with diff internal rep",
            req("q","{!field f=v_f}1.5")
            ,"//result[@numFound='1']"
            );    
    assertQ(
            req("q","{!field f=v_ti}5")
            ,"//result[@numFound='1']"
            );
     assertQ("test multi term field query on text type",
            req("q","{!field f=v_t}Hello  DUDE")
            ,"//result[@numFound='1']"
            );
    assertQ("test prefix query with value in local params",
            req("q","{!prefix f=v_t v=hel}")
            ,"//result[@numFound='2']"
    );
    assertQ("test optional quotes",
            req("q","{!prefix f='v_t' v=\"hel\"}")
            ,"//result[@numFound='2']"
    );
    assertQ("test extra whitespace",
            req("q","{!prefix   f=v_t   v=hel   }")
            ,"//result[@numFound='2']"
    );
    assertQ("test literal with {! in it",
            req("q","{!prefix f=v_s}{!lit")
            ,"//result[@numFound='1']"
    );
    assertQ("test param subst",
            req("q","{!prefix f=$myf v=$my.v}"
                ,"myf","v_t", "my.v", "hel"
            )
            ,"//result[@numFound='2']"
    );
    assertQ("test param subst with literal",
            req("q","{!prefix f=$myf v=$my.v}"
                ,"myf","v_s", "my.v", "{!lit"
            )
            ,"//result[@numFound='1']"
    );
   assertQ("test lucene query",
            req("q","{!lucene}v_t:hel*")
            ,"//result[@numFound='2']"
            );
   assertQ("test lucene default field",
            req("q","{!df=v_t}hel*")
            ,"//result[@numFound='2']"
            );
   assertQ("test lucene operator",
            req("q","{!q.op=OR df=v_t}Hello Yonik")
            ,"//result[@numFound='2']"
            );
   assertQ("test lucene operator",
            req("q","{!q.op=AND df=v_t}Hello Yonik")
            ,"//result[@numFound='1']"
            );
    assertQ("test boost",
            req("q","{!boost b=sum(v_f,1)}id:[5 TO 6]"
                ,"fl","*,score"
            )
            ,"//result[@numFound='2']"
            ,"//doc[./float[@name='v_f']='3.14159' and ./float[@name='score']='4.14159']"
    );
    assertQ("test boost and default type of func",
            req("q","{!boost v=$q1 b=$q2}"
                ,"q1", "{!func}v_f", "q2","v_f"
                ,"fl","*,score"
            )
            ,"//doc[./float[@name='v_f']='1.5' and ./float[@name='score']='2.25']"
    );
    assertQ("test dismax query",
             req("q","{!dismax}hello"
                ,"qf","v_t"
                ,"bf","sqrt(v_f)^100 log(sum(v_f,1))^50"
                ,"bq","{!prefix f=v_t}he"
                ,"debugQuery","on"
             )
             ,"//result[@numFound='2']"
             );
    assertQ("test dismax query w/ local params",
             req("q","{!dismax qf=v_t}hello"
                ,"qf","v_f"
             )
             ,"//result[@numFound='2']"
             );
    assertQ("test nested query",
            req("q","_query_:\"{!query v=$q1}\"", "q1","{!prefix f=v_t}hel")
            ,"//result[@numFound='2']"
            );
    assertQ("test nested nested query",
            req("q","_query_:\"{!query defType=query v=$q1}\"", "q1","{!v=$q2}","q2","{!prefix f=v_t v=$qqq}","qqq","hel")
            ,"//result[@numFound='2']"
            );
  }
}