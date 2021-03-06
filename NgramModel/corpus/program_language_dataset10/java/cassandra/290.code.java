package org.apache.cassandra.io.util;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
public class BufferedSegmentedFile extends SegmentedFile
{
    public BufferedSegmentedFile(String path, long length)
    {
        super(path, length);
    }
    public static class Builder extends SegmentedFile.Builder
    {
        public void addPotentialBoundary(long boundary)
        {
        }
        public SegmentedFile complete(String path)
        {
            long length = new File(path).length();
            return new BufferedSegmentedFile(path, length);
        }
    }
    public FileDataInput getSegment(long position, int bufferSize)
    {
        try
        {
            BufferedRandomAccessFile file = new BufferedRandomAccessFile(path, "r", bufferSize);
            file.seek(position);
            return file;
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
    }
}
