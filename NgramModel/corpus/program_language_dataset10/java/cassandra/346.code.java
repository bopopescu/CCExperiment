package org.apache.cassandra.security.streaming;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.apache.cassandra.security.SSLFactory;
import org.apache.cassandra.streaming.FileStreamTask;
import org.apache.cassandra.streaming.StreamHeader;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.utils.Pair;
public class SSLFileStreamTask extends FileStreamTask
{
    private DataOutputStream output;
    private Socket socket;
    public SSLFileStreamTask(StreamHeader header, InetAddress to)
    {
        super(header, to);
    }
    @Override
    protected long write(FileChannel fc, Pair<Long, Long> section, long length, long bytesTransferred) throws IOException
    {
        int toTransfer = (int)Math.min(CHUNK_SIZE, length - bytesTransferred);
        fc.position(section.left + bytesTransferred);
        ByteBuffer buf = ByteBuffer.allocate(toTransfer);
        fc.read(buf);
        buf.flip();
        output.write(buf.array(), 0, buf.limit());
        output.flush();
        return buf.limit();
    }
    @Override
    protected void writeHeader(ByteBuffer buffer) throws IOException
    {
        output.write(buffer.array(), 0, buffer.limit());
        output.flush();
    }
    @Override
    protected void bind() throws IOException
    {
        socket = SSLFactory.getSocket(DatabaseDescriptor.getEncryptionOptions());
        socket.bind(new InetSocketAddress(FBUtilities.getLocalAddress(), 0));
    }
    @Override
    protected void connect() throws IOException
    {
        socket.connect(new InetSocketAddress(to, DatabaseDescriptor.getStoragePort()));
        output = new DataOutputStream(socket.getOutputStream());
    }
    @Override
    protected void close() throws IOException
    {
        socket.close();
    }
}
