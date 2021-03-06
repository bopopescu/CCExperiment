package org.apache.solr.handler;
import org.apache.solr.util.AbstractSolrTestCase;
public class SpellCheckerRequestHandlerTest 
  extends AbstractSolrTestCase 
{
  @Override
  public String getSchemaFile() { return "solr/conf/schema-spellchecker.xml"; } 
  @Override
  public String getSolrConfigFile() { return "solr/conf/solrconfig-spellchecker.xml"; }
  @Override 
  public void setUp() throws Exception {
      super.setUp();
    }
  private void buildSpellCheckIndex()
  {
    lrf = h.getRequestFactory("spellchecker", 0, 20 );
    lrf.args.put("version","2.0");
    lrf.args.put("sp.query.accuracy",".9");
    assertU("Add some words to the Spell Check Index:",
        adoc("id",  "100",
             "spell", "solr"));
      assertU(adoc("id",  "101",
                   "spell", "cat"));
      assertU(adoc("id",  "102",
                   "spell", "cart"));
      assertU(adoc("id",  "103",
                   "spell", "carp"));
      assertU(adoc("id",  "104",
                   "spell", "cant"));
      assertU(adoc("id",  "105",
                   "spell", "catnip"));
      assertU(adoc("id",  "106",
                   "spell", "cattails"));
      assertU(adoc("id",  "107",
                   "spell", "cod"));
      assertU(adoc("id",  "108",
                   "spell", "corn"));
      assertU(adoc("id",  "109",
                   "spell", "cot"));
      assertU(commit());
      assertU(optimize());
      lrf.args.put("cmd","rebuild");
      assertQ("Need to first build the index:",
              req("cat")
              ,"//str[@name='cmdExecuted'][.='rebuild']"
              ,"//str[@name='words'][.='cat']"
              ,"//str[@name='exist'][.='true']"
              );
      lrf.args.clear();
  }
  public void testSpellCheck_01_correctWords() {
    buildSpellCheckIndex();
    lrf = h.getRequestFactory("spellchecker", 0, 20 );
    lrf.args.put("version","2.0");
    lrf.args.put("sp.query.accuracy",".9");
    assertQ("Failed to spell check",
            req("cat")
            ,"//str[@name='words'][.='cat']"
            ,"//str[@name='exist'][.='true']"
            );
    lrf.args.put("sp.query.accuracy",".4");
    assertQ("Failed to spell check",
            req("cat")
            ,"//str[@name='words'][.='cat']"
            ,"//str[@name='exist'][.='true']"
            ,"//arr/str[.='cot']"
            ,"//arr/str[.='cart']"
            );
    lrf.args.put("sp.query.accuracy",".0");
    assertQ("Failed to spell check",
            req("cat")
            ,"//str[@name='words'][.='cat']"
            ,"//str[@name='exist'][.='true']"
            ,"//arr/str[.='cart']"
            ,"//arr/str[.='cot']"
            ,"//arr/str[.='carp']"
            ,"//arr/str[.='cod']"
            ,"//arr/str[.='corn']"
            );
  }
  public void testSpellCheck_02_incorrectWords() {
    buildSpellCheckIndex();
    lrf = h.getRequestFactory("spellchecker", 0, 20 );
    lrf.args.put("version","2.0");
    lrf.args.put("sp.query.accuracy",".9");
    assertQ("Confirm the index is still valid",
            req("cat")
            ,"//str[@name='words'][.='cat']"
            ,"//str[@name='exist'][.='true']"
            );
    assertQ("Failed to spell check",
            req("coat")
            ,"//str[@name='words'][.='coat']"
            ,"//str[@name='exist'][.='false']"
            ,"//arr[@name='suggestions'][.='']"
            );
    lrf.args.put("sp.query.accuracy",".2");
    assertQ("Failed to spell check",
            req("coat")
            ,"//str[@name='words'][.='coat']"
            ,"//str[@name='exist'][.='false']"
            ,"//arr/str[.='cot']"
            ,"//arr/str[.='cat']"
            ,"//arr/str[.='corn']"
            ,"//arr/str[.='cart']"
            ,"//arr/str[.='cod']"
            ,"//arr/str[.='solr']"
            ,"//arr/str[.='carp']"
            );
    lrf.args.put("sp.query.suggestionCount", "2");
    lrf.args.put("sp.query.accuracy",".2");
    assertQ("Failed to spell check",
            req("coat")
            ,"//str[@name='words'][.='coat']"
            ,"//str[@name='exist'][.='false']"
            ,"//arr/str[.='cot']"
            ,"//arr/str[.='cat']"
            );
  }
  public void testSpellCheck_03_multiWords_correctWords() {
    buildSpellCheckIndex();
    lrf = h.getRequestFactory("spellchecker", 0, 20 );
    lrf.args.put("version","2.0");
    lrf.args.put("sp.query.accuracy",".9");
    assertQ("Confirm the index is still valid",
            req("cat")
            ,"//str[@name='words'][.='cat']"
            ,"//str[@name='exist'][.='true']"
            );
    lrf.args.put("sp.query.extendedResults", "true");
    assertQ("Failed to spell check",
            req("cat")
            ,"//int[@name='numDocs'][.=10]"
            ,"//lst[@name='cat']"
            ,"//lst[@name='cat']/int[@name='frequency'][.>0]"
            ,"//lst[@name='cat']/lst[@name='suggestions']/lst[@name='cat']/int[@name='frequency'][.>0]"
            );
    lrf.args.put("sp.query.accuracy",".2");
    assertQ("Failed to spell check",
            req("cap")
            ,"//int[@name='numDocs'][.=10]"
            ,"//lst[@name='cap']"
            ,"//lst[@name='cap']/int[@name='frequency'][.=0]"
            ,"//lst[@name='cap']/lst[@name='suggestions']/lst[@name='cat']/int[@name='frequency'][.>0]"
            ,"//lst[@name='cap']/lst[@name='suggestions']/lst[@name='cart']/int[@name='frequency'][.>0]"
            ,"//lst[@name='cap']/lst[@name='suggestions']/lst[@name='carp']/int[@name='frequency'][.>0]"
            ,"//lst[@name='cap']/lst[@name='suggestions']/lst[@name='cot']/int[@name='frequency'][.>0]"
            ,"//lst[@name='cap']/lst[@name='suggestions']/lst[@name='cod']/int[@name='frequency'][.>0]"
            ,"//lst[@name='cap']/lst[@name='suggestions']/lst[@name='cant']/int[@name='frequency'][.>0]"
            );
    lrf.args.put("sp.query.suggestionCount", "2");
    lrf.args.put("sp.query.accuracy",".2");
    assertQ("Failed to spell check",
            req("cap")
            ,"//lst[@name='cap']"
            ,"//lst[@name='cap']/int[@name='frequency'][.=0]"
            ,"//lst[@name='cap']/lst[@name='suggestions']/lst[@name='carp']"
            ,"//lst[@name='cap']/lst[@name='suggestions']/lst[@name='cat']"
            );
    lrf.args.put("sp.query.suggestionCount", "2");
    lrf.args.put("sp.query.accuracy",".2");
    assertQ("Failed to spell check",
        req("cad cart carm")
        ,"//lst[@name='cad']"
        ,"//lst[@name='cad']/int[@name='frequency'][.=0]"
        ,"//lst[@name='cad']/lst[@name='suggestions']/lst[@name='cat']"
        ,"//lst[@name='cad']/lst[@name='suggestions']/lst[@name='cod']"
        ,"//lst[@name='cart']"
        ,"//lst[@name='cart']/int[@name='frequency'][.>0]"
        ,"//lst[@name='carm']"
        ,"//lst[@name='carm']/int[@name='frequency'][.=0]"
        ,"//lst[@name='carm']/lst[@name='suggestions']/lst[@name='cart']"
        ,"//lst[@name='carm']/lst[@name='suggestions']/lst[@name='carp']"
    );
  }
  public void testSpellCheck_04_multiWords_incorrectWords() {
    buildSpellCheckIndex();
    lrf = h.getRequestFactory("spellchecker", 0, 20 );
    lrf.args.put("version","2.0");
    lrf.args.put("sp.query.accuracy",".9");
    assertQ("Confirm the index is still valid",
            req("cat")
            ,"//str[@name='words'][.='cat']"
            ,"//str[@name='exist'][.='true']"
            );
    lrf.args.put("sp.query.extendedResults", "true");
    assertQ("Failed to spell check",
            req("coat")
            ,"//int[@name='numDocs'][.=10]"
            ,"//lst[@name='coat']"
            ,"//lst[@name='coat']/int[@name='frequency'][.=0]"
            ,"//lst[@name='coat']/lst[@name='suggestions' and count(lst)=0]"
            );
    lrf.args.put("sp.query.accuracy",".2");
    assertQ("Failed to spell check",
            req("coat")
            ,"//lst[@name='coat']"
            ,"//lst[@name='coat']/int[@name='frequency'][.=0]"
            ,"//lst[@name='coat']/lst[@name='suggestions']/lst[@name='cot']"
            ,"//lst[@name='coat']/lst[@name='suggestions']/lst[@name='cat']"
            ,"//lst[@name='coat']/lst[@name='suggestions']/lst[@name='corn']"
            ,"//lst[@name='coat']/lst[@name='suggestions']/lst[@name='cart']"
            );
    lrf.args.put("sp.query.suggestionCount", "2");
    lrf.args.put("sp.query.accuracy",".2");
    assertQ("Failed to spell check",
            req("coat")
            ,"//lst[@name='coat']"
            ,"//lst[@name='coat']/int[@name='frequency'][.=0]"
            ,"//lst[@name='coat']/lst[@name='suggestions']/lst[@name='cot']"
            ,"//lst[@name='coat']/lst[@name='suggestions']/lst[@name='cat']"
            );
    lrf.args.put("sp.query.suggestionCount", "2");
    lrf.args.put("sp.query.accuracy",".2");
    assertQ("Failed to spell check",
        req("cet cert corp")
        ,"//int[@name='numDocs'][.=10]"
        ,"//lst[@name='cet']"
        ,"//lst[@name='cet']/int[@name='frequency'][.=0]"
        ,"//lst[@name='cet']/lst[@name='suggestions']/lst[1]"
        ,"//lst[@name='cet']/lst[@name='suggestions']/lst[2]"
        ,"//lst[@name='cert']"
        ,"//lst[@name='cert']/int[@name='frequency'][.=0]"
        ,"//lst[@name='cert']/lst[@name='suggestions']/lst[1]"
        ,"//lst[@name='cert']/lst[@name='suggestions']/lst[2]"
        ,"//lst[@name='corp']"
        ,"//lst[@name='corp']/int[@name='frequency'][.=0]"
        ,"//lst[@name='corp']/lst[@name='suggestions']/lst[1]"
        ,"//lst[@name='corp']/lst[@name='suggestions']/lst[2]"
      );
  }
  public void testSpellCheck_05_buildDictionary() {
    lrf = h.getRequestFactory("spellchecker", 0, 20 );
    lrf.args.put("version","2.0");
    lrf.args.put("sp.query.accuracy",".9");
    assertU("Add some words to the Spell Check Index:",
      adoc("id",  "100",
             "spell", "solr cat cart"));
    assertU(adoc("id",  "101",
                   "spell", "cat cart"));
    assertU(adoc("id",  "102",
                   "spell", "cat cart"));
    assertU(adoc("id",  "103",
                   "spell", "cat cart carp"));
    assertU(adoc("id",  "104",
                   "spell", "cat car cant"));
    assertU(adoc("id",  "105",
                   "spell", "cat catnip"));
    assertU(adoc("id",  "106",
                   "spell", "cat cattails"));
    assertU(adoc("id",  "107",
                   "spell", "cat cod"));
    assertU(adoc("id",  "108",
                   "spell", "cat corn"));
    assertU(adoc("id",  "109",
                   "spell", "cat cot"));
    assertU(commit());
    assertU(optimize());
    lrf.args.put("sp.dictionary.threshold", "0.20");
    lrf.args.put("cmd","rebuild");
    assertQ("Need to first build the index:",
            req("cat")
            ,"//str[@name='cmdExecuted'][.='rebuild']"
            ,"//str[@name='words'][.='cat']"
            ,"//str[@name='exist'][.='true']"
            );
    lrf.args.clear();
    lrf.args.put("version","2.0");
    lrf.args.put("sp.query.accuracy",".9");
    assertQ("Confirm index contains only words above threshold",
            req("cat")
            ,"//str[@name='words'][.='cat']"
            ,"//str[@name='exist'][.='true']"
            );
    assertQ("Confirm index contains only words above threshold",
            req("cart")
            ,"//str[@name='words'][.='cart']"
            ,"//str[@name='exist'][.='true']"
            );
    assertQ("Confirm index contains only words above threshold",
            req("cod")
            ,"//str[@name='words'][.='cod']"
            ,"//str[@name='exist'][.='false']"
            );
    assertQ("Confirm index contains only words above threshold",
            req("corn")
            ,"//str[@name='words'][.='corn']"
            ,"//str[@name='exist'][.='false']"
            );
    lrf.args.clear();
  }
}
