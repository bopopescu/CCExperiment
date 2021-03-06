package org.apache.tools.tar;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Arrays;
public class TarBuffer {
    public static final int DEFAULT_RCDSIZE = (512);
    public static final int DEFAULT_BLKSIZE = (DEFAULT_RCDSIZE * 20);
    private InputStream     inStream;
    private OutputStream    outStream;
    private byte[]          blockBuffer;
    private int             currBlkIdx;
    private int             currRecIdx;
    private int             blockSize;
    private int             recordSize;
    private int             recsPerBlock;
    private boolean         debug;
    public TarBuffer(InputStream inStream) {
        this(inStream, TarBuffer.DEFAULT_BLKSIZE);
    }
    public TarBuffer(InputStream inStream, int blockSize) {
        this(inStream, blockSize, TarBuffer.DEFAULT_RCDSIZE);
    }
    public TarBuffer(InputStream inStream, int blockSize, int recordSize) {
        this.inStream = inStream;
        this.outStream = null;
        this.initialize(blockSize, recordSize);
    }
    public TarBuffer(OutputStream outStream) {
        this(outStream, TarBuffer.DEFAULT_BLKSIZE);
    }
    public TarBuffer(OutputStream outStream, int blockSize) {
        this(outStream, blockSize, TarBuffer.DEFAULT_RCDSIZE);
    }
    public TarBuffer(OutputStream outStream, int blockSize, int recordSize) {
        this.inStream = null;
        this.outStream = outStream;
        this.initialize(blockSize, recordSize);
    }
    private void initialize(int blockSize, int recordSize) {
        this.debug = false;
        this.blockSize = blockSize;
        this.recordSize = recordSize;
        this.recsPerBlock = (this.blockSize / this.recordSize);
        this.blockBuffer = new byte[this.blockSize];
        if (this.inStream != null) {
            this.currBlkIdx = -1;
            this.currRecIdx = this.recsPerBlock;
        } else {
            this.currBlkIdx = 0;
            this.currRecIdx = 0;
        }
    }
    public int getBlockSize() {
        return this.blockSize;
    }
    public int getRecordSize() {
        return this.recordSize;
    }
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    public boolean isEOFRecord(byte[] record) {
        for (int i = 0, sz = getRecordSize(); i < sz; ++i) {
            if (record[i] != 0) {
                return false;
            }
        }
        return true;
    }
    public void skipRecord() throws IOException {
        if (debug) {
            System.err.println("SkipRecord: recIdx = " + currRecIdx
                               + " blkIdx = " + currBlkIdx);
        }
        if (inStream == null) {
            throw new IOException("reading (via skip) from an output buffer");
        }
        if (currRecIdx >= recsPerBlock) {
            if (!readBlock()) {
                return;    
            }
        }
        currRecIdx++;
    }
    public byte[] readRecord() throws IOException {
        if (debug) {
            System.err.println("ReadRecord: recIdx = " + currRecIdx
                               + " blkIdx = " + currBlkIdx);
        }
        if (inStream == null) {
            throw new IOException("reading from an output buffer");
        }
        if (currRecIdx >= recsPerBlock) {
            if (!readBlock()) {
                return null;
            }
        }
        byte[] result = new byte[recordSize];
        System.arraycopy(blockBuffer,
                         (currRecIdx * recordSize), result, 0,
                         recordSize);
        currRecIdx++;
        return result;
    }
    private boolean readBlock() throws IOException {
        if (debug) {
            System.err.println("ReadBlock: blkIdx = " + currBlkIdx);
        }
        if (inStream == null) {
            throw new IOException("reading from an output buffer");
        }
        currRecIdx = 0;
        int offset = 0;
        int bytesNeeded = blockSize;
        while (bytesNeeded > 0) {
            long numBytes = inStream.read(blockBuffer, offset,
                                               bytesNeeded);
            if (numBytes == -1) {
                if (offset == 0) {
                    return false;
                }
                Arrays.fill(blockBuffer, offset, offset + bytesNeeded, (byte) 0);
                break;
            }
            offset += numBytes;
            bytesNeeded -= numBytes;
            if (numBytes != blockSize) {
                if (debug) {
                    System.err.println("ReadBlock: INCOMPLETE READ "
                                       + numBytes + " of " + blockSize
                                       + " bytes read.");
                }
            }
        }
        currBlkIdx++;
        return true;
    }
    public int getCurrentBlockNum() {
        return currBlkIdx;
    }
    public int getCurrentRecordNum() {
        return currRecIdx - 1;
    }
    public void writeRecord(byte[] record) throws IOException {
        if (debug) {
            System.err.println("WriteRecord: recIdx = " + currRecIdx
                               + " blkIdx = " + currBlkIdx);
        }
        if (outStream == null) {
            throw new IOException("writing to an input buffer");
        }
        if (record.length != recordSize) {
            throw new IOException("record to write has length '"
                                  + record.length
                                  + "' which is not the record size of '"
                                  + recordSize + "'");
        }
        if (currRecIdx >= recsPerBlock) {
            writeBlock();
        }
        System.arraycopy(record, 0, blockBuffer,
                         (currRecIdx * recordSize),
                         recordSize);
        currRecIdx++;
    }
    public void writeRecord(byte[] buf, int offset) throws IOException {
        if (debug) {
            System.err.println("WriteRecord: recIdx = " + currRecIdx
                               + " blkIdx = " + currBlkIdx);
        }
        if (outStream == null) {
            throw new IOException("writing to an input buffer");
        }
        if ((offset + recordSize) > buf.length) {
            throw new IOException("record has length '" + buf.length
                                  + "' with offset '" + offset
                                  + "' which is less than the record size of '"
                                  + recordSize + "'");
        }
        if (currRecIdx >= recsPerBlock) {
            writeBlock();
        }
        System.arraycopy(buf, offset, blockBuffer,
                         (currRecIdx * recordSize),
                         recordSize);
        currRecIdx++;
    }
    private void writeBlock() throws IOException {
        if (debug) {
            System.err.println("WriteBlock: blkIdx = " + currBlkIdx);
        }
        if (outStream == null) {
            throw new IOException("writing to an input buffer");
        }
        outStream.write(blockBuffer, 0, blockSize);
        outStream.flush();
        currRecIdx = 0;
        currBlkIdx++;
        Arrays.fill(blockBuffer, (byte) 0);
    }
    void flushBlock() throws IOException {
        if (debug) {
            System.err.println("TarBuffer.flushBlock() called.");
        }
        if (outStream == null) {
            throw new IOException("writing to an input buffer");
        }
        if (currRecIdx > 0) {
            writeBlock();
        }
    }
    public void close() throws IOException {
        if (debug) {
            System.err.println("TarBuffer.closeBuffer().");
        }
        if (outStream != null) {
            flushBlock();
            if (outStream != System.out
                    && outStream != System.err) {
                outStream.close();
                outStream = null;
            }
        } else if (inStream != null) {
            if (inStream != System.in) {
                inStream.close();
                inStream = null;
            }
        }
    }
}
