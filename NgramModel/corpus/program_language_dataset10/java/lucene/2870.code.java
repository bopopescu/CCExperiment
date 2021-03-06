package org.apache.solr.update;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.SolrIndexReader;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.AbstractSolrTestCase;
import org.apache.solr.util.RefCounted;
public class DirectUpdateHandlerTest extends AbstractSolrTestCase {
  public String getSchemaFile() { return "schema12.xml"; }
  public String getSolrConfigFile() { return "solrconfig.xml"; }
  public void testRequireUniqueKey() throws Exception 
  {
    SolrCore core = h.getCore();
    UpdateHandler updater = core.getUpdateHandler();
    AddUpdateCommand cmd = new AddUpdateCommand();
    cmd.overwriteCommitted = true;
    cmd.overwritePending = true;
    cmd.allowDups = false;
    cmd.doc = new Document();
    cmd.doc.add( new Field( "id", "AAA", Store.YES, Index.NOT_ANALYZED ) );
    cmd.doc.add( new Field( "subject", "xxxxx", Store.YES, Index.NOT_ANALYZED ) );
    updater.addDoc( cmd );
    cmd.indexedId = null;  
    cmd.doc = new Document();
    cmd.doc.add( new Field( "id", "AAA", Store.YES, Index.NOT_ANALYZED ) );
    cmd.doc.add( new Field( "id", "BBB", Store.YES, Index.NOT_ANALYZED ) );
    cmd.doc.add( new Field( "subject", "xxxxx", Store.YES, Index.NOT_ANALYZED ) );
    try {
      updater.addDoc( cmd );
      fail( "added a document with multiple ids" );
    }
    catch( SolrException ex ) { } 
    cmd.indexedId = null;  
    cmd.doc = new Document();
    cmd.doc.add( new Field( "subject", "xxxxx", Store.YES, Index.NOT_ANALYZED ) );
    try {
      updater.addDoc( cmd );
      fail( "added a document without an ids" );
    }
    catch( SolrException ex ) { } 
  }
  public void testUncommit() throws Exception {
    addSimpleDoc("A");
    Map<String,String> args = new HashMap<String, String>();
    args.put( CommonParams.Q, "id:A" );
    args.put( "indent", "true" );
    SolrQueryRequest req = new LocalSolrQueryRequest( h.getCore(), new MapSolrParams( args) );
    assertQ("\"A\" should not be found.", req
            ,"//*[@numFound='0']"
            );
  }
  public void testAddCommit() throws Exception {
    addSimpleDoc("A");
    SolrCore core = h.getCore();
    UpdateHandler updater = core.getUpdateHandler();
    CommitUpdateCommand cmtCmd = new CommitUpdateCommand(false);
    cmtCmd.waitSearcher = true;
    updater.commit(cmtCmd);
    Map<String,String> args = new HashMap<String, String>();
    args.put( CommonParams.Q, "id:A" );
    args.put( "indent", "true" );
    SolrQueryRequest req = new LocalSolrQueryRequest( core, new MapSolrParams( args) );
    assertQ("\"A\" should be found.", req
            ,"//*[@numFound='1']"
            ,"//result/doc[1]/str[@name='id'][.='A']"
            );
  }
  public void testDeleteCommit() throws Exception {
    addSimpleDoc("A");
    addSimpleDoc("B");
    SolrCore core = h.getCore();
    UpdateHandler updater = core.getUpdateHandler();
    CommitUpdateCommand cmtCmd = new CommitUpdateCommand(false);
    cmtCmd.waitSearcher = true;
    updater.commit(cmtCmd);
    Map<String,String> args = new HashMap<String, String>();
    args.put( CommonParams.Q, "id:A OR id:B" );
    args.put( "indent", "true" );
    SolrQueryRequest req = new LocalSolrQueryRequest( core, new MapSolrParams( args) );
    assertQ("\"A\" and \"B\" should be found.", req
            ,"//*[@numFound='2']"
            ,"//result/doc[1]/str[@name='id'][.='A']"
            ,"//result/doc[2]/str[@name='id'][.='B']"
            );
    deleteSimpleDoc("B");
    assertQ("\"A\" and \"B\" should be found.", req
            ,"//*[@numFound='2']"
            ,"//result/doc[1]/str[@name='id'][.='A']"
            ,"//result/doc[2]/str[@name='id'][.='B']"
            );
    updater.commit(cmtCmd);
    assertQ("\"B\" should not be found.", req
        ,"//*[@numFound='1']"
        ,"//result/doc[1]/str[@name='id'][.='A']"
        );
  }
  public void testAddRollback() throws Exception {
    addSimpleDoc("A");
    SolrCore core = h.getCore();
    UpdateHandler updater = core.getUpdateHandler();
    assertTrue( updater instanceof DirectUpdateHandler2 );
    DirectUpdateHandler2 duh2 = (DirectUpdateHandler2)updater;
    CommitUpdateCommand cmtCmd = new CommitUpdateCommand(false);
    cmtCmd.waitSearcher = true;
    assertEquals( 1, duh2.addCommands.get() );
    assertEquals( 1, duh2.addCommandsCumulative.get() );
    assertEquals( 0, duh2.commitCommands.get() );
    updater.commit(cmtCmd);
    assertEquals( 0, duh2.addCommands.get() );
    assertEquals( 1, duh2.addCommandsCumulative.get() );
    assertEquals( 1, duh2.commitCommands.get() );
    addSimpleDoc("B");
    RollbackUpdateCommand rbkCmd = new RollbackUpdateCommand();
    assertEquals( 1, duh2.addCommands.get() );
    assertEquals( 2, duh2.addCommandsCumulative.get() );
    assertEquals( 0, duh2.rollbackCommands.get() );
    updater.rollback(rbkCmd);
    assertEquals( 0, duh2.addCommands.get() );
    assertEquals( 1, duh2.addCommandsCumulative.get() );
    assertEquals( 1, duh2.rollbackCommands.get() );
    Map<String,String> args = new HashMap<String, String>();
    args.put( CommonParams.Q, "id:A OR id:B" );
    args.put( "indent", "true" );
    SolrQueryRequest req = new LocalSolrQueryRequest( core, new MapSolrParams( args) );
    assertQ("\"B\" should not be found.", req
            ,"//*[@numFound='1']"
            ,"//result/doc[1]/str[@name='id'][.='A']"
            );
    addSimpleDoc("ZZZ");
    assertU(commit());
    assertQ("\"ZZZ\" must be found.", req("q", "id:ZZZ")
            ,"//*[@numFound='1']"
            ,"//result/doc[1]/str[@name='id'][.='ZZZ']"
            );
  }
  public void testDeleteRollback() throws Exception {
    addSimpleDoc("A");
    addSimpleDoc("B");
    SolrCore core = h.getCore();
    UpdateHandler updater = core.getUpdateHandler();
    assertTrue( updater instanceof DirectUpdateHandler2 );
    DirectUpdateHandler2 duh2 = (DirectUpdateHandler2)updater;
    CommitUpdateCommand cmtCmd = new CommitUpdateCommand(false);
    cmtCmd.waitSearcher = true;
    assertEquals( 2, duh2.addCommands.get() );
    assertEquals( 2, duh2.addCommandsCumulative.get() );
    assertEquals( 0, duh2.commitCommands.get() );
    updater.commit(cmtCmd);
    assertEquals( 0, duh2.addCommands.get() );
    assertEquals( 2, duh2.addCommandsCumulative.get() );
    assertEquals( 1, duh2.commitCommands.get() );
    Map<String,String> args = new HashMap<String, String>();
    args.put( CommonParams.Q, "id:A OR id:B" );
    args.put( "indent", "true" );
    SolrQueryRequest req = new LocalSolrQueryRequest( core, new MapSolrParams( args) );
    assertQ("\"A\" and \"B\" should be found.", req
            ,"//*[@numFound='2']"
            ,"//result/doc[1]/str[@name='id'][.='A']"
            ,"//result/doc[2]/str[@name='id'][.='B']"
            );
    deleteSimpleDoc("B");
    assertQ("\"A\" and \"B\" should be found.", req
        ,"//*[@numFound='2']"
        ,"//result/doc[1]/str[@name='id'][.='A']"
        ,"//result/doc[2]/str[@name='id'][.='B']"
        );
    RollbackUpdateCommand rbkCmd = new RollbackUpdateCommand();
    assertEquals( 1, duh2.deleteByIdCommands.get() );
    assertEquals( 1, duh2.deleteByIdCommandsCumulative.get() );
    assertEquals( 0, duh2.rollbackCommands.get() );
    updater.rollback(rbkCmd);
    assertEquals( 0, duh2.deleteByIdCommands.get() );
    assertEquals( 0, duh2.deleteByIdCommandsCumulative.get() );
    assertEquals( 1, duh2.rollbackCommands.get() );
    assertQ("\"B\" should be found.", req
        ,"//*[@numFound='2']"
        ,"//result/doc[1]/str[@name='id'][.='A']"
        ,"//result/doc[2]/str[@name='id'][.='B']"
        );
    addSimpleDoc("ZZZ");
    assertU(commit());
    assertQ("\"ZZZ\" must be found.", req("q", "id:ZZZ")
            ,"//*[@numFound='1']"
            ,"//result/doc[1]/str[@name='id'][.='ZZZ']"
            );
  }
  public void testExpungeDeletes() throws Exception {
    assertU(adoc("id","1"));
    assertU(adoc("id","2"));
    assertU(commit());
    assertU(adoc("id","3"));
    assertU(adoc("id","2"));
    assertU(adoc("id","4"));
    assertU(commit());
    SolrQueryRequest sr = req("q","foo");
    SolrIndexReader r = sr.getSearcher().getReader();
    assertTrue(r.maxDoc() > r.numDocs());   
    assertTrue(r.getLeafReaders().length > 1);  
    sr.close();
    assertU(commit("expungeDeletes","true"));
    sr = req("q","foo");
    r = sr.getSearcher().getReader();
    assertEquals(r.maxDoc(), r.numDocs());  
    assertEquals(4,r.maxDoc());             
    assertTrue(r.getLeafReaders().length > 1);  
    sr.close();
  }
  private void addSimpleDoc(String id) throws Exception {
    SolrCore core = h.getCore();
    UpdateHandler updater = core.getUpdateHandler();
    AddUpdateCommand cmd = new AddUpdateCommand();
    cmd.overwriteCommitted = true;
    cmd.overwritePending = true;
    cmd.allowDups = false;
    cmd.doc = new Document();
    cmd.doc.add( new Field( "id", id, Store.YES, Index.NOT_ANALYZED ) );
    updater.addDoc( cmd );
  }
  private void deleteSimpleDoc(String id) throws Exception {
    SolrCore core = h.getCore();
    UpdateHandler updater = core.getUpdateHandler();
    DeleteUpdateCommand cmd = new DeleteUpdateCommand();
    cmd.id = id;
    cmd.fromCommitted = true;
    cmd.fromPending = true;
    updater.delete(cmd);
  }
}
