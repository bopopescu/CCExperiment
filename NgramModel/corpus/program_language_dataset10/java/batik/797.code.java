package org.apache.batik.ext.awt.image.codec.util;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
public final class FileCacheSeekableStream extends SeekableStream {
    private InputStream stream;
    private File cacheFile;
    private RandomAccessFile cache;
    private int bufLen = 1024;
    private byte[] buf = new byte[bufLen];
    private long length = 0;
    private long pointer = 0;
    private boolean foundEOF = false;
    public FileCacheSeekableStream(InputStream stream)
        throws IOException {
        this.stream = stream;
        this.cacheFile = File.createTempFile("jai-FCSS-", ".tmp");
        cacheFile.deleteOnExit();
        this.cache = new RandomAccessFile(cacheFile, "rw");
    }
    private long readUntil(long pos) throws IOException {
        if (pos < length) {
            return pos;
        }
        if (foundEOF) {
            return length;
        }
        long len = pos - length;
        cache.seek(length);
        while (len > 0) {
            int nbytes = stream.read(buf, 0, (int)Math.min(len, bufLen));
            if (nbytes == -1) {
                foundEOF = true;
                return length;
            }
            cache.setLength(cache.length() + nbytes);
            cache.write(buf, 0, nbytes);
            len -= nbytes;
            length += nbytes;
        }
        return pos;
    }
    public boolean canSeekBackwards() {
        return true;
    }
    public long getFilePointer() {
        return pointer;
    }
    public void seek(long pos) throws IOException {
        if (pos < 0) {
            throw new IOException(PropertyUtil.getString("FileCacheSeekableStream0"));
        }
        pointer = pos;
    }
    public int read() throws IOException {
        long next = pointer + 1;
        long pos = readUntil(next);
        if (pos >= next) {
            cache.seek(pointer++);
            return cache.read();
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
        len = (int)Math.min(len, pos - pointer);
        if (len > 0) {
            cache.seek(pointer);
            cache.readFully(b, off, len);
            pointer += len;
            return len;
        } else {
            return -1;
        }
    }
    public void close() throws IOException {
        super.close();
        cache.close();
        cacheFile.delete();
    }
}
