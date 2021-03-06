package org.apache.solr.handler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.SimpleFSLockFactory;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.IndexDeletionPolicyWrapper;
import org.apache.solr.core.SolrCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class SnapShooter {
  private static final Logger LOG = LoggerFactory.getLogger(SnapShooter.class.getName());
  private String snapDir = null;
  private SolrCore solrCore;
  private SimpleFSLockFactory lockFactory;
  public SnapShooter(SolrCore core, String location) throws IOException {
    solrCore = core;
    if (location == null) snapDir = core.getDataDir();
    else  {
      File base = new File(core.getCoreDescriptor().getInstanceDir());
      snapDir = org.apache.solr.common.util.FileUtils.resolvePath(base, location).getAbsolutePath();
      File dir = new File(snapDir);
      if (!dir.exists())  dir.mkdirs();
    }
    lockFactory = new SimpleFSLockFactory(snapDir);
  }
  void createSnapAsync(final IndexCommit indexCommit, final ReplicationHandler replicationHandler) {
    replicationHandler.core.getDeletionPolicy().saveCommitPoint(indexCommit.getVersion());
    new Thread() {
      public void run() {
        createSnapshot(indexCommit, replicationHandler);
      }
    }.start();
  }
  void createSnapshot(final IndexCommit indexCommit, ReplicationHandler replicationHandler) {
    NamedList details = new NamedList();
    details.add("startTime", new Date().toString());
    File snapShotDir = null;
    String directoryName = null;
    Lock lock = null;
    try {
      SimpleDateFormat fmt = new SimpleDateFormat(DATE_FMT);
      directoryName = "snapshot." + fmt.format(new Date());
      lock = lockFactory.makeLock(directoryName + ".lock");
      if (lock.isLocked()) return;
      snapShotDir = new File(snapDir, directoryName);
      if (!snapShotDir.mkdir()) {
        LOG.warn("Unable to create snapshot directory: " + snapShotDir.getAbsolutePath());
        return;
      }
      Collection<String> files = indexCommit.getFileNames();
      FileCopier fileCopier = new FileCopier(solrCore.getDeletionPolicy(), indexCommit);
      fileCopier.copyFiles(files, snapShotDir);
      details.add("fileCount", files.size());
      details.add("status", "success");
      details.add("snapshotCompletedAt", new Date().toString());
    } catch (Exception e) {
      SnapPuller.delTree(snapShotDir);
      LOG.error("Exception while creating snapshot", e);
      details.add("snapShootException", e.getMessage());
    } finally {
        replicationHandler.core.getDeletionPolicy().releaseCommitPoint(indexCommit.getVersion());   
        replicationHandler.snapShootDetails = details;
      if (lock != null) {
        try {
          lock.release();
        } catch (IOException e) {
          LOG.error("Unable to release snapshoot lock: " + directoryName + ".lock");
        }
      }
    }
  }
  public static final String SNAP_DIR = "snapDir";
  public static final String DATE_FMT = "yyyyMMddhhmmss";
  private class FileCopier {
    private static final int DEFAULT_BUFFER_SIZE = 32768;
    private byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    private IndexCommit indexCommit;
    private IndexDeletionPolicyWrapper delPolicy;
    public FileCopier(IndexDeletionPolicyWrapper delPolicy, IndexCommit commit) {
      this.delPolicy = delPolicy;
      this.indexCommit = commit;
    }
    public void copyFiles(Collection<String> files, File destDir) throws IOException {
      for (String indexFile : files) {
        File source = new File(solrCore.getIndexDir(), indexFile);
        copyFile(source, new File(destDir, source.getName()), true);
      }
    }
    public void copyFile(File source, File destination, boolean preserveFileDate)
        throws IOException {
      if (!source.exists()) {
        String message = "File " + source + " does not exist";
        throw new FileNotFoundException(message);
      }
      if (destination.getParentFile() != null
          && !destination.getParentFile().exists()) {
        destination.getParentFile().mkdirs();
      }
      if (destination.exists() && !destination.canWrite()) {
        String message = "Unable to open file " + destination + " for writing.";
        throw new IOException(message);
      }
      FileInputStream input = null;
      FileOutputStream output = null;
      try {
        input = new FileInputStream(source);
        output = new FileOutputStream(destination);
        int count = 0;
        int n = 0;
        int rcnt = 0;
        while (-1 != (n = input.read(buffer))) {
          output.write(buffer, 0, n);
          count += n;
          rcnt++;
        }
      } finally {
        try {
          IOUtils.closeQuietly(input);
        } finally {
          IOUtils.closeQuietly(output);
        }
      }
      if (source.length() != destination.length()) {
        String message = "Failed to copy full contents from " + source + " to "
            + destination;
        throw new IOException(message);
      }
      if (preserveFileDate) {
        destination.setLastModified(source.lastModified());
      }
    }
  }
}
