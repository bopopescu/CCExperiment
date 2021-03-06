package org.apache.cassandra.db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.net.IVerbHandler;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
public class SchemaCheckVerbHandler implements IVerbHandler
{
    private final Logger logger = LoggerFactory.getLogger(SchemaCheckVerbHandler.class);
    public void doVerb(Message message)
    {
        logger.debug("Received schema check request.");
        Message response = message.getInternalReply(DatabaseDescriptor.getDefsVersion().toString().getBytes());
        MessagingService.instance().sendOneWay(response, message.getFrom());
    }
}
