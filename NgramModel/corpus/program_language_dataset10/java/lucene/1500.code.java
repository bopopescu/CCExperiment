package org.apache.lucene.index;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.BufferedIndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;
import java.util.HashMap;
import java.io.IOException;
class CompoundFileReader extends Directory {
    private int readBufferSize;
    private static final class FileEntry {
        long offset;
        long length;
    }
    private Directory directory;
    private String fileName;
    private IndexInput stream;
    private HashMap<String,FileEntry> entries = new HashMap<String,FileEntry>();
  public CompoundFileReader(Directory dir, String name) throws IOException {
    this(dir, name, BufferedIndexInput.BUFFER_SIZE);
  }
  public CompoundFileReader(Directory dir, String name, int readBufferSize)
    throws IOException
    {
        directory = dir;
        fileName = name;
        this.readBufferSize = readBufferSize;
        boolean success = false;
        try {
            stream = dir.openInput(name, readBufferSize);
            int count = stream.readVInt();
            FileEntry entry = null;
            for (int i=0; i<count; i++) {
                long offset = stream.readLong();
                String id = stream.readString();
                if (entry != null) {
                    entry.length = offset - entry.offset;
                }
                entry = new FileEntry();
                entry.offset = offset;
                entries.put(id, entry);
            }
            if (entry != null) {
                entry.length = stream.length() - entry.offset;
            }
            success = true;
        } finally {
            if (! success && (stream != null)) {
                try {
                    stream.close();
                } catch (IOException e) { }
            }
        }
    }
    public Directory getDirectory() {
        return directory;
    }
    public String getName() {
        return fileName;
    }
    @Override
    public synchronized void close() throws IOException {
        if (stream == null)
            throw new IOException("Already closed");
        entries.clear();
        stream.close();
        stream = null;
    }
    @Override
    public synchronized IndexInput openInput(String id)
    throws IOException
    {
      return openInput(id, readBufferSize);
    }
    @Override
    public synchronized IndexInput openInput(String id, int readBufferSize)
    throws IOException
    {
        if (stream == null)
            throw new IOException("Stream closed");
        FileEntry entry = entries.get(id);
        if (entry == null)
            throw new IOException("No sub-file with id " + id + " found");
        return new CSIndexInput(stream, entry.offset, entry.length, readBufferSize);
    }
    @Override
    public String[] listAll() {
        String res[] = new String[entries.size()];
        return entries.keySet().toArray(res);
    }
    @Override
    public boolean fileExists(String name) {
        return entries.containsKey(name);
    }
    @Override
    public long fileModified(String name) throws IOException {
        return directory.fileModified(fileName);
    }
    @Override
    public void touchFile(String name) throws IOException {
        directory.touchFile(fileName);
    }
    @Override
    public void deleteFile(String name)
    {
        throw new UnsupportedOperationException();
    }
    public void renameFile(String from, String to)
    {
        throw new UnsupportedOperationException();
    }
    @Override
    public long fileLength(String name)
    throws IOException
    {
        FileEntry e = entries.get(name);
        if (e == null)
            throw new IOException("File " + name + " does not exist");
        return e.length;
    }
    @Override
    public IndexOutput createOutput(String name)
    {
        throw new UnsupportedOperationException();
    }
    @Override
    public Lock makeLock(String name)
    {
        throw new UnsupportedOperationException();
    }
    static final class CSIndexInput extends BufferedIndexInput {
        IndexInput base;
        long fileOffset;
        long length;
        CSIndexInput(final IndexInput base, final long fileOffset, final long length)
        {
            this(base, fileOffset, length, BufferedIndexInput.BUFFER_SIZE);
        }
        CSIndexInput(final IndexInput base, final long fileOffset, final long length, int readBufferSize)
        {
            super(readBufferSize);
            this.base = (IndexInput)base.clone();
            this.fileOffset = fileOffset;
            this.length = length;
        }
        @Override
        public Object clone() {
          CSIndexInput clone = (CSIndexInput)super.clone();
          clone.base = (IndexInput)base.clone();
          clone.fileOffset = fileOffset;
          clone.length = length;
          return clone;
        }
        @Override
        protected void readInternal(byte[] b, int offset, int len)
        throws IOException
        {
          long start = getFilePointer();
          if(start + len > length)
            throw new IOException("read past EOF");
          base.seek(fileOffset + start);
          base.readBytes(b, offset, len, false);
        }
        @Override
        protected void seekInternal(long pos) {}
        @Override
        public void close() throws IOException {
          base.close();
        }
        @Override
        public long length() {
          return length;
        }
    }
}
