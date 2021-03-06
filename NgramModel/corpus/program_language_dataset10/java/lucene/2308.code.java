package org.apache.solr.core;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import java.io.IOException;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
public class RAMDirectoryFactory extends StandardDirectoryFactory {
  private Map<String, Directory> directories = new HashMap<String, Directory>();
  @Override
  public Directory open(String path) throws IOException {
    synchronized (this) {
      Directory directory = directories.get(path);
      if (directory == null) {
        directory = openNew(path);
        directories.put(path, directory);
      }
      return directory;
    }
  }
  Directory openNew(String path) throws IOException {
    Directory directory;
    File dirFile = new File(path);
    boolean indexExists = dirFile.canRead();
    if (indexExists) {
      Directory dir = super.open(path);
      directory = new RAMDirectory(dir);
    } else {
      directory = new RAMDirectory();
    }
    return directory;
  }
}
