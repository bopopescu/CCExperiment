package org.apache.solr.common;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
public interface ResourceLoader
{
  public InputStream openResource(String resource) throws IOException;
  public List<String> getLines(String resource) throws IOException;
  public Object newInstance(String cname, String ... subpackages);
}