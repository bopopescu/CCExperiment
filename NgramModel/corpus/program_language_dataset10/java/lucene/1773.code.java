package org.apache.lucene.store;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
public class NIOFSDirectory extends FSDirectory {
  public NIOFSDirectory(File path, LockFactory lockFactory) throws IOException {
    super(path, lockFactory);
  }
  public NIOFSDirectory(File path) throws IOException {
    super(path, null);
  }
  @Override
  public IndexInput openInput(String name, int bufferSize) throws IOException {
    ensureOpen();
    return new NIOFSIndexInput(new File(getDirectory(), name), bufferSize, getReadChunkSize());
  }
  @Override
  public IndexOutput createOutput(String name) throws IOException {
    initOutput(name);
    return new SimpleFSDirectory.SimpleFSIndexOutput(new File(directory, name));
  }
  protected static class NIOFSIndexInput extends SimpleFSDirectory.SimpleFSIndexInput {
    private ByteBuffer byteBuf; 
    private byte[] otherBuffer;
    private ByteBuffer otherByteBuf;
    final FileChannel channel;
    public NIOFSIndexInput(File path, int bufferSize, int chunkSize) throws IOException {
      super(path, bufferSize, chunkSize);
      channel = file.getChannel();
    }
    @Override
    protected void newBuffer(byte[] newBuffer) {
      super.newBuffer(newBuffer);
      byteBuf = ByteBuffer.wrap(newBuffer);
    }
    @Override
    public void close() throws IOException {
      if (!isClone && file.isOpen) {
        try {
          channel.close();
        } finally {
          file.close();
        }
      }
    }
    @Override
    protected void readInternal(byte[] b, int offset, int len) throws IOException {
      final ByteBuffer bb;
      if (b == buffer && 0 == offset) {
        assert byteBuf != null;
        byteBuf.clear();
        byteBuf.limit(len);
        bb = byteBuf;
      } else {
        if (offset == 0) {
          if (otherBuffer != b) {
            otherBuffer = b;
            otherByteBuf = ByteBuffer.wrap(b);
          } else
            otherByteBuf.clear();
          otherByteBuf.limit(len);
          bb = otherByteBuf;
        } else {
          bb = ByteBuffer.wrap(b, offset, len);
        }
      }
      int readOffset = bb.position();
      int readLength = bb.limit() - readOffset;
      assert readLength == len;
      long pos = getFilePointer();
      try {
        while (readLength > 0) {
          final int limit;
          if (readLength > chunkSize) {
            limit = readOffset + chunkSize;
          } else {
            limit = readOffset + readLength;
          }
          bb.limit(limit);
          int i = channel.read(bb, pos);
          if (i == -1) {
            throw new IOException("read past EOF");
          }
          pos += i;
          readOffset += i;
          readLength -= i;
        }
      } catch (OutOfMemoryError e) {
        final OutOfMemoryError outOfMemoryError = new OutOfMemoryError(
              "OutOfMemoryError likely caused by the Sun VM Bug described in "
              + "https://issues.apache.org/jira/browse/LUCENE-1566; try calling FSDirectory.setReadChunkSize "
              + "with a a value smaller than the current chunk size (" + chunkSize + ")");
        outOfMemoryError.initCause(e);
        throw outOfMemoryError;
      }
    }
  }
}
