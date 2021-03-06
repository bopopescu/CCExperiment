package org.apache.lucene.store;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.lucene.util.ThreadInterruptedException;
import org.apache.lucene.util.Constants;
public abstract class FSDirectory extends Directory {
  private static MessageDigest DIGESTER;
  static {
    try {
      DIGESTER = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e.toString(), e);
    }
  }
  private static File getCanonicalPath(File file) throws IOException {
    return new File(file.getCanonicalPath());
  }
  private boolean checked;
  final void createDir() throws IOException {
    if (!checked) {
      if (!directory.exists())
        if (!directory.mkdirs())
          throw new IOException("Cannot create directory: " + directory);
      checked = true;
    }
  }
  protected final void initOutput(String name) throws IOException {
    ensureOpen();
    createDir();
    File file = new File(directory, name);
    if (file.exists() && !file.delete())          
      throw new IOException("Cannot overwrite: " + file);
  }
  protected File directory = null;
  protected FSDirectory(File path, LockFactory lockFactory) throws IOException {
    path = getCanonicalPath(path);
    if (lockFactory == null) {
      lockFactory = new NativeFSLockFactory();
    }
    directory = path;
    if (directory.exists() && !directory.isDirectory())
      throw new NoSuchDirectoryException("file '" + directory + "' exists but is not a directory");
    setLockFactory(lockFactory);
    if (lockFactory instanceof FSLockFactory) {
      final FSLockFactory lf = (FSLockFactory) lockFactory;
      final File dir = lf.getLockDir();
      if (dir == null) {
        lf.setLockDir(this.directory);
        lf.setLockPrefix(null);
      } else if (dir.getCanonicalPath().equals(this.directory.getCanonicalPath())) {
        lf.setLockPrefix(null);
      }
    }
  }
  public static FSDirectory open(File path) throws IOException {
    return open(path, null);
  }
  public static FSDirectory open(File path, LockFactory lockFactory) throws IOException {
    if (Constants.WINDOWS) {
      return new SimpleFSDirectory(path, lockFactory);
    } else {
      return new NIOFSDirectory(path, lockFactory);
    }
  }
  public static String[] listAll(File dir) throws IOException {
    if (!dir.exists())
      throw new NoSuchDirectoryException("directory '" + dir + "' does not exist");
    else if (!dir.isDirectory())
      throw new NoSuchDirectoryException("file '" + dir + "' exists but is not a directory");
    String[] result = dir.list(new FilenameFilter() {
        public boolean accept(File dir, String file) {
          return !new File(dir, file).isDirectory();
        }
      });
    if (result == null)
      throw new IOException("directory '" + dir + "' exists and is a directory, but cannot be listed: list() returned null");
    return result;
  }
  @Override
  public String[] listAll() throws IOException {
    ensureOpen();
    return listAll(directory);
  }
  @Override
  public boolean fileExists(String name) {
    ensureOpen();
    File file = new File(directory, name);
    return file.exists();
  }
  @Override
  public long fileModified(String name) {
    ensureOpen();
    File file = new File(directory, name);
    return file.lastModified();
  }
  public static long fileModified(File directory, String name) {
    File file = new File(directory, name);
    return file.lastModified();
  }
  @Override
  public void touchFile(String name) {
    ensureOpen();
    File file = new File(directory, name);
    file.setLastModified(System.currentTimeMillis());
  }
  @Override
  public long fileLength(String name) {
    ensureOpen();
    File file = new File(directory, name);
    return file.length();
  }
  @Override
  public void deleteFile(String name) throws IOException {
    ensureOpen();
    File file = new File(directory, name);
    if (!file.delete())
      throw new IOException("Cannot delete " + file);
  }
  @Override
  public void sync(String name) throws IOException {
    ensureOpen();
    File fullFile = new File(directory, name);
    boolean success = false;
    int retryCount = 0;
    IOException exc = null;
    while(!success && retryCount < 5) {
      retryCount++;
      RandomAccessFile file = null;
      try {
        try {
          file = new RandomAccessFile(fullFile, "rw");
          file.getFD().sync();
          success = true;
        } finally {
          if (file != null)
            file.close();
        }
      } catch (IOException ioe) {
        if (exc == null)
          exc = ioe;
        try {
          Thread.sleep(5);
        } catch (InterruptedException ie) {
          throw new ThreadInterruptedException(ie);
        }
      }
    }
    if (!success)
      throw exc;
  }
  @Override
  public IndexInput openInput(String name) throws IOException {
    ensureOpen();
    return openInput(name, BufferedIndexInput.BUFFER_SIZE);
  }
  private static final char[] HEX_DIGITS =
  {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
  @Override
  public String getLockID() {
    ensureOpen();
    String dirName;                               
    try {
      dirName = directory.getCanonicalPath();
    } catch (IOException e) {
      throw new RuntimeException(e.toString(), e);
    }
    byte digest[];
    synchronized (DIGESTER) {
      digest = DIGESTER.digest(dirName.getBytes());
    }
    StringBuilder buf = new StringBuilder();
    buf.append("lucene-");
    for (int i = 0; i < digest.length; i++) {
      int b = digest[i];
      buf.append(HEX_DIGITS[(b >> 4) & 0xf]);
      buf.append(HEX_DIGITS[b & 0xf]);
    }
    return buf.toString();
  }
  @Override
  public synchronized void close() {
    isOpen = false;
  }
  @Deprecated
  public File getFile() {
    return getDirectory();
  }
  public File getDirectory() {
    ensureOpen();
    return directory;
  }
  @Override
  public String toString() {
    return this.getClass().getName() + "@" + directory;
  }
  public static final int DEFAULT_READ_CHUNK_SIZE = Constants.JRE_IS_64BIT ? Integer.MAX_VALUE: 100 * 1024 * 1024;
  private int chunkSize = DEFAULT_READ_CHUNK_SIZE;
  public final void setReadChunkSize(int chunkSize) {
    if (chunkSize <= 0) {
      throw new IllegalArgumentException("chunkSize must be positive");
    }
    if (!Constants.JRE_IS_64BIT) {
      this.chunkSize = chunkSize;
    }
  }
  public final int getReadChunkSize() {
    return chunkSize;
  }
}
