package org.apache.lucene.benchmark.quality.trec;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.lucene.benchmark.quality.QualityQuery;
public class Trec1MQReader {
  private String name;
  public Trec1MQReader(String name) {
    super();
    this.name = name;
  }
  public QualityQuery[] readQueries(BufferedReader reader) throws IOException {
    ArrayList<QualityQuery> res = new ArrayList<QualityQuery>();
    String line;
    try {
      while (null!=(line=reader.readLine())) {
        line = line.trim();
        if (line.startsWith("#")) {
          continue;
        }
        int k = line.indexOf(":");
        String id = line.substring(0,k).trim();
        String qtext = line.substring(k+1).trim();
        HashMap<String,String> fields = new HashMap<String,String>();
        fields.put(name,qtext);
        QualityQuery topic = new QualityQuery(id,fields);
        res.add(topic);
      }
    } finally {
      reader.close();
    }
    QualityQuery qq[] = res.toArray(new QualityQuery[0]);
    Arrays.sort(qq);
    return qq;
  }
}
