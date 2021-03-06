package org.apache.solr.schema;
import java.util.Collection;
import org.apache.solr.core.SolrCore;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.util.AbstractSolrTestCase;
public class RequiredFieldsTest extends AbstractSolrTestCase {
  @Override public String getSchemaFile() { return "schema-required-fields.xml"; }
  @Override public String getSolrConfigFile() { return "solrconfig.xml"; }
  @Override 
  public void setUp() throws Exception {
    super.setUp();
  }
  @Override 
  public void tearDown() throws Exception {
    super.tearDown();
  }
  public void testRequiredFieldsConfig() {
    SolrCore core = h.getCore();
    IndexSchema schema = core.getSchema();
    SchemaField uniqueKey = schema.getUniqueKeyField();
    assertTrue( uniqueKey.isRequired() ); 
    assertTrue( schema.getRequiredFields().contains( uniqueKey ) );
    Collection<SchemaField> requiredFields =schema.getRequiredFields();
    int numDefaultFields = schema.getFieldsWithDefaultValue().size();
    assertEquals( numDefaultFields+1+1, requiredFields.size()); 
  }
  public void testRequiredFieldsSingleAdd() {      
    SolrCore core = h.getCore();     
    assertU("adding document",
      adoc("id", "529", "name", "document with id, name, and subject", "field_t", "what's inside?", "subject", "info"));
    assertU(commit());
    assertQ("should find one", req("id:529") ,"//result[@numFound=1]" );
    assertU("adding a doc without field w/ configured default",
          adoc("id", "530", "name", "document with id and name", "field_t", "what's inside?"));
    assertU(commit());
    String subjectDefault = core.getSchema().getField("subject").getDefaultValue();
    assertNotNull("subject has no default value", subjectDefault);
    assertQ("should find one with subject="+subjectDefault, req("id:530 subject:"+subjectDefault) ,"//result[@numFound=1]" );
    assertNull(core.getSchema().getField("name").getDefaultValue());
    assertFailedU("adding doc without required field",
          adoc("id", "531", "subject", "no name document", "field_t", "what's inside?") );
    assertU(commit());
    assertQ("should not find any", req("id:531") ,"//result[@numFound=0]" );      
  }
  public void testAddMultipleDocumentsWithErrors() {
    assertU("adding 3 documents",
      "<add>" +doc("id", "601", "name", "multiad one", "field_t", "what's inside?", "subject", "info") +
      doc("id", "602", "name", "multiad two", "field_t", "what's inside?", "subject", "info") +
        doc("id", "603", "name", "multiad three", "field_t", "what's inside?", "subject", "info") +
        "</add>");
    assertU(commit());
    assertQ("should find three", req("name:multiad") ,"//result[@numFound=3]" );
    assertU("adding 3 docs, with 2nd one missing a field that has a default value",
      "<add>" +doc("id", "601", "name", "nosubject batch one", "field_t", "what's inside?", "subject", "info") +
      doc("id", "602", "name", "nosubject batch two", "field_t", "what's inside?") +
        doc("id", "603", "name", "nosubject batch three", "field_t", "what's inside?", "subject", "info") +
        "</add>");
    assertU(commit());
    assertQ("should find three", req("name:nosubject") ,"//result[@numFound=3]" );
    assertFailedU("adding 3 documents, with 2nd one with undefined field",
          "<add>" +doc("id", "801", "name", "baddef batch one", "field_t", "what's inside?", "subject", "info") +
          doc("id", "802", "field_t", "name", "baddef batch two", "what's inside?", "subject", "info", "GaRbAgeFiElD", "garbage") +
            doc("id", "803", "name", "baddef batch three", "field_t", "what's inside?", "subject", "info") +
            "</add>");
    assertU(commit());
    assertQ("should find one", req("name:baddef") ,"//result[@numFound=1]" );
    assertFailedU("adding 3 docs, with 2nd one missing required field",
      "<add>" +doc("id", "701", "name", "noname batch one", "field_t", "what's inside?", "subject", "info") +
      doc("id", "702", "field_t", "what's inside?", "subject", "info") +
        doc("id", "703", "name", "noname batch batch three", "field_t", "what's inside?", "subject", "info") +
        "</add>");
    assertU(commit());
    assertQ("should find one", req("name:noname") ,"//result[@numFound=1]" );
  }  
}
