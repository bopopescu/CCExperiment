package org.apache.lucene.store;
import java.io.IOException;
public class MockRAMOutputStream extends RAMOutputStream {
  private MockRAMDirectory dir;
  private boolean first=true;
  private final String name;
  byte[] singleByte = new byte[1];
  public MockRAMOutputStream(MockRAMDirectory dir, RAMFile f, String name) {
    super(f);
    this.dir = dir;
    this.name = name;
  }
  @Override
  public void close() throws IOException {
    super.close();
    long size = dir.getRecomputedActualSizeInBytes();
    if (size > dir.maxUsedSize) {
      dir.maxUsedSize = size;
    }
  }
  @Override
  public void flush() throws IOException {
    dir.maybeThrowDeterministicException();
    super.flush();
  }
  @Override
  public void writeByte(byte b) throws IOException {
    singleByte[0] = b;
    writeBytes(singleByte, 0, 1);
  }
  @Override
  public void writeBytes(byte[] b, int offset, int len) throws IOException {
    long freeSpace = dir.maxSize - dir.sizeInBytes();
    long realUsage = 0;
    if (dir.crashed)
      throw new IOException("MockRAMDirectory was crashed; cannot write to " + name);
    if (dir.maxSize != 0 && freeSpace <= len) {
      realUsage = dir.getRecomputedActualSizeInBytes();
      freeSpace = dir.maxSize - realUsage;
    }
    if (dir.maxSize != 0 && freeSpace <= len) {
      if (freeSpace > 0 && freeSpace < len) {
        realUsage += freeSpace;
        super.writeBytes(b, offset, (int) freeSpace);
      }
      if (realUsage > dir.maxUsedSize) {
        dir.maxUsedSize = realUsage;
      }
      throw new IOException("fake disk full at " + dir.getRecomputedActualSizeInBytes() + " bytes when writing " + name);
    } else {
      super.writeBytes(b, offset, len);
    }
    dir.maybeThrowDeterministicException();
    if (first) {
      first = false;
      dir.maybeThrowIOException();
    }
  }
}
