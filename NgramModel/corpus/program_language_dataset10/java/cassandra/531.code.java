package org.apache.cassandra.streaming;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.net.InetAddress;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.service.StorageService;
public class StreamUtil
{
    static public void finishStreamRequest(Message msg, InetAddress to) 
    {
        byte[] body = msg.getMessageBody();
        ByteArrayInputStream bufIn = new ByteArrayInputStream(body);
        try
        {
            StreamRequestMessage srm = StreamRequestMessage.serializer().deserialize(new DataInputStream(bufIn));
            StreamInSession session = StreamInSession.get(to, srm.sessionId);
            session.closeIfFinished();
        }
        catch (Exception e)
        {
            System.err.println(e); 
            e.printStackTrace();
        }
    }
}
