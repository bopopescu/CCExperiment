package org.apache.cassandra.io.util;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class MmappedSegmentedFile extends SegmentedFile
{
    public static long MAX_SEGMENT_SIZE = Integer.MAX_VALUE;
    private final Segment[] segments;
    public MmappedSegmentedFile(String path, long length, Segment[] segments)
    {
        super(path, length);
        this.segments = segments;
    }
    private Segment floor(long position)
    {
        assert 0 <= position && position < length: position + " vs " + length;
        Segment seg = new Segment(position, null);
        int idx = Arrays.binarySearch(segments, seg);
        assert idx != -1 : "Bad position " + position + " in segments " + Arrays.toString(segments);
        if (idx < 0)
            idx = -(idx + 2);
        return segments[idx];
    }
    public FileDataInput getSegment(long position, int bufferSize)
    {
        Segment segment = floor(position);
        if (segment.right != null)
        {
            return new MappedFileDataInput(segment.right, path, (int) (position - segment.left));
        }
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
    static class Builder extends SegmentedFile.Builder
    {
        private final List<Long> boundaries;
        private long currentStart = 0;
        private long currentSize = 0;
        public Builder()
        {
            super();
            boundaries = new ArrayList<Long>();
            boundaries.add(0L);
        }
        @Override
        public void addPotentialBoundary(long boundary)
        {
            if (boundary - currentStart <= MAX_SEGMENT_SIZE)
            {
                currentSize = boundary - currentStart;
                return;
            }
            if (currentSize > 0)
            {
                currentStart += currentSize;
                boundaries.add(currentStart);
            }
            currentSize = boundary - currentStart;
            if (currentSize > MAX_SEGMENT_SIZE)
            {
                currentStart = boundary;
                boundaries.add(currentStart);
                currentSize = 0;
            }
        }
        @Override
        public SegmentedFile complete(String path)
        {
            long length = new File(path).length();
            boundaries.add(Long.valueOf(length));
            return new MmappedSegmentedFile(path, length, createSegments(path));
        }
        private Segment[] createSegments(String path)
        {
            int segcount = boundaries.size() - 1;
            Segment[] segments = new Segment[segcount];
            RandomAccessFile raf = null;
            try
            {
                raf = new RandomAccessFile(path, "r");
                for (int i = 0; i < segcount; i++)
                {
                    long start = boundaries.get(i);
                    long size = boundaries.get(i + 1) - start;
                    MappedByteBuffer segment = size <= MAX_SEGMENT_SIZE
                                               ? raf.getChannel().map(FileChannel.MapMode.READ_ONLY, start, size)
                                               : null;
                    segments[i] = new Segment(start, segment);
                }
            }
            catch (IOException e)
            {
                throw new IOError(e);
            }
            finally
            {
                FileUtils.closeQuietly(raf);
            }
            return segments;
        }
    }
}
