package org.apache.solr.analysis;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.util.plugin.ResourceLoaderAware;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.fr.*;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.TokenFilter;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.io.IOException;
public class ElisionFilterFactory extends BaseTokenFilterFactory implements ResourceLoaderAware {
  private Set articles;
  public void inform(ResourceLoader loader) {
    String articlesFile = args.get("articles");
    if (articlesFile != null) {
      try {
        List<String> wlist = loader.getLines(articlesFile);
        articles = StopFilter.makeStopSet((String[])wlist.toArray(new String[0]), false);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      throw new RuntimeException("No articles specified for ElisionFilterFactory");
    }
  }
  public ElisionFilter create(TokenStream input) {
    assureMatchVersion();
    return new ElisionFilter(luceneMatchVersion,input,articles);
  }
}
