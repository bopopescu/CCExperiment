package org.apache.batik.ext.awt.image.codec.util;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
public final class MemoryCacheSeekableStream extends SeekableStream {
    private InputStream src;
    private long pointer = 0;
    private static final int SECTOR_SHIFT = 9;
    private static final int SECTOR_SIZE = 1 << SECTOR_SHIFT;
    private static final int SECTOR_MASK = SECTOR_SIZE - 1;
    private List data = new ArrayList();
    int sectors = 0;
    int length = 0;
    boolean foundEOS = false;
    public MemoryCacheSeekableStream(InputStream src) {
        this.src = src;
    }
    private long readUntil(long pos) throws IOException {
        if (pos < length) {
            return pos;
        }
        if (foundEOS) {
            return length;
        }
        int sector = (int)(pos >> SECTOR_SHIFT);
        int startSector = length >> SECTOR_SHIFT;
        for (int i = startSector; i <= sector; i++) {
            byte[] buf = new byte[SECTOR_SIZE];
            data.add(buf);
            int len = SECTOR_SIZE;
            int off = 0;
            while (len > 0) {
                int nbytes = src.read(buf, off, len);
                if (nbytes == -1) {
                    foundEOS = true;
                    return length;
                }
                off += nbytes;
                len -= nbytes;
                length += nbytes;
            }
        }
        return length;
    }
    public boolean canSeekBackwards() {
        return true;
    }
    public long getFilePointer() {
        return pointer;
    }
    public void seek(long pos) throws IOException {
        if (pos < 0) {
            throw new IOException(PropertyUtil.getString("MemoryCacheSeekableStream0"));
        }
        pointer = pos;
    }
    public int read() throws IOException {
        long next = pointer + 1;
        long pos = readUntil(next);
        if (pos >= next) {
            byte[] buf =
                (byte[])data.get((int)(pointer >> SECTOR_SHIFT));
            return buf[(int)(pointer++ & SECTOR_MASK)] & 0xff;
        } else {
            return -1;
        }
    }
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if ((off < 0) || (len < 0) || (off + len > b.length)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        long pos = readUntil(pointer + len);
        if (pos <= pointer) {
            return -1;
        }
        byte[] buf = (byte[])data.get((int)(pointer >> SECTOR_SHIFT));
        int nbytes = Math.min(len, SECTOR_SIZE - (int)(pointer & SECTOR_MASK));
        System.arraycopy(buf, (int)(pointer & SECTOR_MASK),
                         b, off, nbytes);
        pointer += nbytes;
        return nbytes;
    }
}
