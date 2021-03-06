package org.apache.cassandra.db.commitlog;
import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import org.apache.cassandra.io.ICompactSerializer2;
import org.apache.cassandra.io.util.FileUtils;
public class CommitLogHeader
{
    public static String getHeaderPathFromSegment(CommitLogSegment segment)
    {
        return getHeaderPathFromSegmentPath(segment.getPath());
    }
    public static String getHeaderPathFromSegmentPath(String segmentPath)
    {
        return segmentPath + ".header";
    }
    public static CommitLogHeaderSerializer serializer = new CommitLogHeaderSerializer();
    private Map<Integer, Integer> cfDirtiedAt; 
    CommitLogHeader()
    {
        this(new HashMap<Integer, Integer>());
    }
    private CommitLogHeader(Map<Integer, Integer> cfDirtiedAt)
    {
        this.cfDirtiedAt = cfDirtiedAt;
    }
    boolean isDirty(Integer cfId)
    {
        return cfDirtiedAt.containsKey(cfId);
    } 
    int getPosition(Integer cfId)
    {
        Integer x = cfDirtiedAt.get(cfId);
        return x == null ? 0 : x;
    }
    void turnOn(Integer cfId, long position)
    {
        assert position >= 0 && position <= Integer.MAX_VALUE;
        cfDirtiedAt.put(cfId, (int)position);
    }
    void turnOff(Integer cfId)
    {
        cfDirtiedAt.remove(cfId);
    }
    boolean isSafeToDelete() throws IOException
    {
        return cfDirtiedAt.isEmpty();
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder("");
        sb.append("CLH(dirty+flushed={");
        for (Map.Entry<Integer, Integer> entry : cfDirtiedAt.entrySet())
        {       
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
        }
        sb.append("})");
        return sb.toString();
    }
    public String dirtyString()
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : cfDirtiedAt.entrySet())
            sb.append(entry.getKey()).append(", ");
        return sb.toString();
    }
    static void writeCommitLogHeader(CommitLogHeader header, String headerFile) throws IOException
    {
        DataOutputStream out = null;
        try
        {
            out = new DataOutputStream(new FileOutputStream(headerFile));
            serializer.serialize(header, out);
        }
        finally
        {
            if (out != null)
                out.close();
        }
    }
    static CommitLogHeader readCommitLogHeader(String headerFile) throws IOException
    {
        DataInputStream reader = null;
        try
        {
            reader = new DataInputStream(new BufferedInputStream(new FileInputStream(headerFile)));
            return serializer.deserialize(reader);
        }
        finally
        {
            FileUtils.closeQuietly(reader);
        }
    }
    int getReplayPosition()
    {
        return cfDirtiedAt.isEmpty() ? -1 : Collections.min(cfDirtiedAt.values());
    }
    static class CommitLogHeaderSerializer implements ICompactSerializer2<CommitLogHeader>
    {
        public void serialize(CommitLogHeader clHeader, DataOutput dos) throws IOException
        {
            Checksum checksum = new CRC32();
            dos.writeInt(clHeader.cfDirtiedAt.size()); 
            checksum.update(clHeader.cfDirtiedAt.size());
            dos.writeLong(checksum.getValue());
            for (Map.Entry<Integer, Integer> entry : clHeader.cfDirtiedAt.entrySet())
            {
                dos.writeInt(entry.getKey()); 
                checksum.update(entry.getKey());
                dos.writeInt(entry.getValue()); 
                checksum.update(entry.getValue());
            }
            dos.writeLong(checksum.getValue());
        }
        public CommitLogHeader deserialize(DataInput dis) throws IOException
        {
            Checksum checksum = new CRC32();
            int lastFlushedAtSize = dis.readInt();
            checksum.update(lastFlushedAtSize);
            if (checksum.getValue() != dis.readLong())
            {
                throw new IOException("Invalid or corrupt commitlog header");
            }
            Map<Integer, Integer> lastFlushedAt = new HashMap<Integer, Integer>();
            for (int i = 0; i < lastFlushedAtSize; i++)
            {
                int key = dis.readInt();
                checksum.update(key);
                int value = dis.readInt();
                checksum.update(value);
                lastFlushedAt.put(key, value);
            }
            if (checksum.getValue() != dis.readLong())
            {
                throw new IOException("Invalid or corrupt commitlog header");
            }
            return new CommitLogHeader(lastFlushedAt);
        }
    }
}
