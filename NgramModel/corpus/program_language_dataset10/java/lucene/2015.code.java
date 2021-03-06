package org.apache.lucene.store;
import java.io.IOException;
import java.util.HashMap;
import org.apache.lucene.util.LuceneTestCase;
public class TestHugeRamFile extends LuceneTestCase {
  private static final long MAX_VALUE = (long) 2 * (long) Integer.MAX_VALUE;
  private static class DenseRAMFile extends RAMFile {
    private long capacity = 0;
    private HashMap<Integer,byte[]> singleBuffers = new HashMap<Integer,byte[]>();
    @Override
    protected byte[] newBuffer(int size) {
      capacity += size;
      if (capacity <= MAX_VALUE) {
        byte buf[] = singleBuffers.get(Integer.valueOf(size));
        if (buf==null) {
          buf = new byte[size]; 
          singleBuffers.put(Integer.valueOf(size),buf);
        }
        return buf;
      }
      return new byte[size];
    }
  }
  public void testHugeFile() throws IOException {
    DenseRAMFile f = new DenseRAMFile();
    RAMOutputStream out = new RAMOutputStream(f);
    byte b1[] = new byte[RAMOutputStream.BUFFER_SIZE];
    byte b2[] = new byte[RAMOutputStream.BUFFER_SIZE / 3];
    for (int i = 0; i < b1.length; i++) {
      b1[i] = (byte) (i & 0x0007F);
    }
    for (int i = 0; i < b2.length; i++) {
      b2[i] = (byte) (i & 0x0003F);
    }
    long n = 0;
    assertEquals("output length must match",n,out.length());
    while (n <= MAX_VALUE - b1.length) {
      out.writeBytes(b1,0,b1.length);
      out.flush();
      n += b1.length;
      assertEquals("output length must match",n,out.length());
    }
    int m = b2.length;
    long L = 12;
    for (int j=0; j<L; j++) {
      for (int i = 0; i < b2.length; i++) {
        b2[i]++;
      }
      out.writeBytes(b2,0,m);
      out.flush();
      n += m;
      assertEquals("output length must match",n,out.length());
    }
    out.close();
    RAMInputStream in = new RAMInputStream(f);
    assertEquals("input length must match",n,in.length());
    for (int j=0; j<L; j++) {
      long loc = n - (L-j)*m; 
      in.seek(loc/3);
      in.seek(loc);
      for (int i=0; i<m; i++) {
        byte bt = in.readByte();
        byte expected = (byte) (1 + j + (i & 0x0003F));
        assertEquals("must read same value that was written! j="+j+" i="+i,expected,bt);
      }
    }
  }
}
