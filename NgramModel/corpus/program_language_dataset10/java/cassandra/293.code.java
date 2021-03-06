package org.apache.cassandra.io.util;
import java.io.Closeable;
import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;
public interface FileDataInput extends DataInput, Closeable
{
    public String getPath();
    public boolean isEOF() throws IOException;
    public long bytesRemaining() throws IOException;
    public FileMark mark();
    public void reset(FileMark mark) throws IOException;
    public int bytesPastMark(FileMark mark);
    public ByteBuffer readBytes(int length) throws IOException;
}
