package org.apache.cassandra.net;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.service.StorageService;
public class MessageDeliveryTask implements Runnable
{
    private static final Logger logger_ = LoggerFactory.getLogger(MessageDeliveryTask.class);    
    private Message message_;
    private final long constructionTime_ = System.currentTimeMillis();
    public MessageDeliveryTask(Message message)
    {
        assert message != null;
        message_ = message;    
    }
    public void run()
    { 
        StorageService.Verb verb = message_.getVerb();
        switch (verb)
        {
            case BINARY:
            case MUTATION:
            case READ:
            case RANGE_SLICE:
            case READ_REPAIR:
            case REQUEST_RESPONSE:
                if (System.currentTimeMillis() > constructionTime_ + DatabaseDescriptor.getRpcTimeout())
                {
                    MessagingService.instance().incrementDroppedMessages(verb);
                    return;
                }
                break;
            case UNUSED_1:
            case UNUSED_2:
            case UNUSED_3:
                return;
            default:
                break;
        }
        IVerbHandler verbHandler = MessagingService.instance().getVerbHandler(verb);
        assert verbHandler != null : "unknown verb " + verb;
        verbHandler.doVerb(message_);
    }
}
