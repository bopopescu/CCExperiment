 package org.apache.cassandra.streaming;
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.IOError;
 import java.io.IOException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.apache.cassandra.net.IVerbHandler;
 import org.apache.cassandra.net.Message;
public class StreamRequestVerbHandler implements IVerbHandler
{
    private static Logger logger = LoggerFactory.getLogger(StreamRequestVerbHandler.class);
    public void doVerb(Message message)
    {
        if (logger.isDebugEnabled())
            logger.debug("Received a StreamRequestMessage from {}", message.getFrom());
        byte[] body = message.getMessageBody();
        ByteArrayInputStream bufIn = new ByteArrayInputStream(body);
        try
        {
            StreamRequestMessage srm = StreamRequestMessage.serializer().deserialize(new DataInputStream(bufIn));
            if (logger.isDebugEnabled())
                logger.debug(srm.toString());
            StreamOutSession session = StreamOutSession.create(srm.table, message.getFrom(), srm.sessionId);
            StreamOut.transferRangesForRequest(session, srm.ranges, srm.type);
        }
        catch (IOException ex)
        {
            throw new IOError(ex);
        }
    }
}
