package org.apache.lucene.benchmark.byTask.programmatic;
import java.io.IOException;
import java.util.Properties;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.tasks.AddDocTask;
import org.apache.lucene.benchmark.byTask.tasks.CloseIndexTask;
import org.apache.lucene.benchmark.byTask.tasks.CreateIndexTask;
import org.apache.lucene.benchmark.byTask.tasks.RepSumByNameTask;
import org.apache.lucene.benchmark.byTask.tasks.TaskSequence;
import org.apache.lucene.benchmark.byTask.utils.Config;
public class Sample {
  public static void main(String[] args) throws Exception {
    Properties p = initProps();
    Config conf = new Config(p);
    PerfRunData runData = new PerfRunData(conf);
    TaskSequence top = new TaskSequence(runData,null,null,false); 
    CreateIndexTask create = new CreateIndexTask(runData);
    top.addTask(create);
    TaskSequence seq1 = new TaskSequence(runData,"AddDocs",top,false);
    seq1.setRepetitions(500);
    seq1.setNoChildReport();
    top.addTask(seq1);
    AddDocTask addDoc = new AddDocTask(runData);
    seq1.addTask(addDoc); 
    CloseIndexTask close = new CloseIndexTask(runData);
    top.addTask(close);
    RepSumByNameTask rep = new RepSumByNameTask(runData);
    top.addTask(rep);
    System.out.println(top.toString());
    top.doLogic();
  }
  private static Properties initProps() {
    Properties p = new Properties();
    p.setProperty ( "task.max.depth.log"  , "3" );
    p.setProperty ( "max.buffered"        , "buf:10:10:100:100:10:10:100:100" );
    p.setProperty ( "doc.maker"           , "org.apache.lucene.benchmark.byTask.feeds.ReutersContentSource" );
    p.setProperty ( "log.step"            , "2000" );
    p.setProperty ( "doc.delete.step"     , "8" );
    p.setProperty ( "analyzer"            , "org.apache.lucene.analysis.standard.StandardAnalyzer" );
    p.setProperty ( "doc.term.vector"     , "false" );
    p.setProperty ( "directory"           , "FSDirectory" );
    p.setProperty ( "query.maker"         , "org.apache.lucene.benchmark.byTask.feeds.ReutersQueryMaker" );
    p.setProperty ( "doc.stored"          , "true" );
    p.setProperty ( "docs.dir"            , "reuters-out" );
    p.setProperty ( "compound"            , "cmpnd:true:true:true:true:false:false:false:false" );
    p.setProperty ( "doc.tokenized"       , "true" );
    p.setProperty ( "merge.factor"        , "mrg:10:100:10:100:10:100:10:100" );
    return p;
  }
}
