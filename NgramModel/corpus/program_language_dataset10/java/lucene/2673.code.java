package org.apache.solr;
import org.apache.solr.request.*;
import org.apache.solr.util.*;
import java.util.Set;
public class MinimalSchemaTest extends AbstractSolrTestCase {
  public String getSchemaFile() { return "solr/conf/schema-minimal.xml"; } 
  public String getSolrConfigFile() { return "solr/conf/solrconfig.xml"; }
  public void setUp() throws Exception {
    super.setUp();
    assertNull("UniqueKey Field isn't null", 
               h.getCore().getSchema().getUniqueKeyField());
    lrf.args.put("version","2.0");
    assertU("Simple assertion that adding a document works",
            adoc("id",  "4055",
                 "subject", "Hoss",
                 "project", "Solr"));
    assertU(adoc("id",  "4056",
                 "subject", "Yonik",
                 "project", "Solr"));
    assertU(commit());
    assertU(optimize());
  }
  public void testSimpleQueries() {
    assertQ("couldn't find subject hoss",
            req("subject:Hoss")
            ,"//result[@numFound=1]"
            ,"//str[@name='id'][.='4055']"
            );
    assertQ("couldn't find subject Yonik",
            req("subject:Yonik")
            ,"//result[@numFound=1]"
            ,"//str[@name='id'][.='4056']"
            );
  }
  public void testLuke() {
    assertQ("basic luke request failed",
            req("qt", "/admin/luke")
            ,"//int[@name='numDocs'][.='2']"
            ,"//int[@name='numTerms'][.='5']"
            );
    assertQ("luke show schema failed",
            req("qt", "/admin/luke",
                "show","schema")
            ,"//int[@name='numDocs'][.='2']"
            ,"//int[@name='numTerms'][.='5']"
            ,"//null[@name='uniqueKeyField']"
            ,"//null[@name='defaultSearchField']"
            );
  }
  public void testAllConfiguredHandlers() {
    Set<String> handlerNames = h.getCore().getRequestHandlers().keySet();
    for (String handler : handlerNames) {
      try {
        if (handler.startsWith("/update")) {
          continue;
        }
        assertQ("failure w/handler: '" + handler + "'",
                req("qt", handler,
                    "q", "foo:bar")
                ,"//lst[@name='responseHeader']"
                );
      } catch (Exception e) {
        throw new RuntimeException("exception w/handler: '" + handler + "'", 
                                   e);
      }
    }
  }
}
