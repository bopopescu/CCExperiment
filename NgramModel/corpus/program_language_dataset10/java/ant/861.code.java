package org.apache.tools.tar;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
public class TarInputStream extends FilterInputStream {
    private static final int SMALL_BUFFER_SIZE = 256;
    private static final int BUFFER_SIZE = 8 * 1024;
    private static final int LARGE_BUFFER_SIZE = 32 * 1024;
    private static final int BYTE_MASK = 0xFF;
    protected boolean debug;
    protected boolean hasHitEOF;
    protected long entrySize;
    protected long entryOffset;
    protected byte[] readBuf;
    protected TarBuffer buffer;
    protected TarEntry currEntry;
    protected byte[] oneBuf;
    public TarInputStream(InputStream is) {
        this(is, TarBuffer.DEFAULT_BLKSIZE, TarBuffer.DEFAULT_RCDSIZE);
    }
    public TarInputStream(InputStream is, int blockSize) {
        this(is, blockSize, TarBuffer.DEFAULT_RCDSIZE);
    }
    public TarInputStream(InputStream is, int blockSize, int recordSize) {
        super(is);
        this.buffer = new TarBuffer(is, blockSize, recordSize);
        this.readBuf = null;
        this.oneBuf = new byte[1];
        this.debug = false;
        this.hasHitEOF = false;
    }
    public void setDebug(boolean debug) {
        this.debug = debug;
        buffer.setDebug(debug);
    }
    public void close() throws IOException {
        buffer.close();
    }
    public int getRecordSize() {
        return buffer.getRecordSize();
    }
    public int available() throws IOException {
        if (entrySize - entryOffset > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) (entrySize - entryOffset);
    }
    public long skip(long numToSkip) throws IOException {
        byte[] skipBuf = new byte[BUFFER_SIZE];
        long skip = numToSkip;
        while (skip > 0) {
            int realSkip = (int) (skip > skipBuf.length ? skipBuf.length : skip);
            int numRead = read(skipBuf, 0, realSkip);
            if (numRead == -1) {
                break;
            }
            skip -= numRead;
        }
        return (numToSkip - skip);
    }
    public boolean markSupported() {
        return false;
    }
    public void mark(int markLimit) {
    }
    public void reset() {
    }
    public TarEntry getNextEntry() throws IOException {
        if (hasHitEOF) {
            return null;
        }
        if (currEntry != null) {
            long numToSkip = entrySize - entryOffset;
            if (debug) {
                System.err.println("TarInputStream: SKIP currENTRY '"
                        + currEntry.getName() + "' SZ "
                        + entrySize + " OFF "
                        + entryOffset + "  skipping "
                        + numToSkip + " bytes");
            }
            while (numToSkip > 0) {
                long skipped = skip(numToSkip);
                if (skipped <= 0) {
                    throw new RuntimeException("failed to skip current tar"
                                               + " entry");
                }
                numToSkip -= skipped;
            }
            readBuf = null;
        }
        byte[] headerBuf = buffer.readRecord();
        if (headerBuf == null) {
            if (debug) {
                System.err.println("READ NULL RECORD");
            }
            hasHitEOF = true;
        } else if (buffer.isEOFRecord(headerBuf)) {
            if (debug) {
                System.err.println("READ EOF RECORD");
            }
            hasHitEOF = true;
        }
        if (hasHitEOF) {
            currEntry = null;
        } else {
            currEntry = new TarEntry(headerBuf);
            if (debug) {
                System.err.println("TarInputStream: SET CURRENTRY '"
                        + currEntry.getName()
                        + "' size = "
                        + currEntry.getSize());
            }
            entryOffset = 0;
            entrySize = currEntry.getSize();
        }
        if (currEntry != null && currEntry.isGNULongNameEntry()) {
            StringBuffer longName = new StringBuffer();
            byte[] buf = new byte[SMALL_BUFFER_SIZE];
            int length = 0;
            while ((length = read(buf)) >= 0) {
                longName.append(new String(buf, 0, length));
            }
            getNextEntry();
            if (currEntry == null) {
                return null;
            }
            if (longName.length() > 0
                && longName.charAt(longName.length() - 1) == 0) {
                longName.deleteCharAt(longName.length() - 1);
            }
            currEntry.setName(longName.toString());
        }
        return currEntry;
    }
    public int read() throws IOException {
        int num = read(oneBuf, 0, 1);
        return num == -1 ? -1 : ((int) oneBuf[0]) & BYTE_MASK;
    }
    public int read(byte[] buf, int offset, int numToRead) throws IOException {
        int totalRead = 0;
        if (entryOffset >= entrySize) {
            return -1;
        }
        if ((numToRead + entryOffset) > entrySize) {
            numToRead = (int) (entrySize - entryOffset);
        }
        if (readBuf != null) {
            int sz = (numToRead > readBuf.length) ? readBuf.length
                    : numToRead;
            System.arraycopy(readBuf, 0, buf, offset, sz);
            if (sz >= readBuf.length) {
                readBuf = null;
            } else {
                int newLen = readBuf.length - sz;
                byte[] newBuf = new byte[newLen];
                System.arraycopy(readBuf, sz, newBuf, 0, newLen);
                readBuf = newBuf;
            }
            totalRead += sz;
            numToRead -= sz;
            offset += sz;
        }
        while (numToRead > 0) {
            byte[] rec = buffer.readRecord();
            if (rec == null) {
                throw new IOException("unexpected EOF with " + numToRead
                        + " bytes unread");
            }
            int sz = numToRead;
            int recLen = rec.length;
            if (recLen > sz) {
                System.arraycopy(rec, 0, buf, offset, sz);
                readBuf = new byte[recLen - sz];
                System.arraycopy(rec, sz, readBuf, 0, recLen - sz);
            } else {
                sz = recLen;
                System.arraycopy(rec, 0, buf, offset, recLen);
            }
            totalRead += sz;
            numToRead -= sz;
            offset += sz;
        }
        entryOffset += totalRead;
        return totalRead;
    }
    public void copyEntryContents(OutputStream out) throws IOException {
        byte[] buf = new byte[LARGE_BUFFER_SIZE];
        while (true) {
            int numRead = read(buf, 0, buf.length);
            if (numRead == -1) {
                break;
            }
            out.write(buf, 0, numRead);
        }
    }
}
