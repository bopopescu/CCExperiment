package org.apache.cassandra.service;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ReadCommand;
import org.apache.cassandra.db.ReadResponse;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.net.IAsyncCallback;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.utils.WrappedRunnable;
class ConsistencyChecker implements Runnable
{
    private static Logger logger_ = LoggerFactory.getLogger(ConsistencyChecker.class);
    private static ScheduledExecutorService executor_ = new ScheduledThreadPoolExecutor(1); 
    private final Row row_;
    protected final List<InetAddress> replicas_;
    private final ReadCommand readCommand_;
    private final InetAddress dataSource;
    public ConsistencyChecker(ReadCommand command, Row row, List<InetAddress> endpoints, InetAddress dataSource)
    {
        row_ = row;
        replicas_ = endpoints;
        readCommand_ = command;
        this.dataSource = dataSource;
    }
    public void run()
	{
        ReadCommand readCommandDigestOnly = constructReadMessage(true);
		try
		{
			Message message = readCommandDigestOnly.makeReadMessage();
            if (logger_.isDebugEnabled())
              logger_.debug("Reading consistency digest for " + readCommand_.key + " from " + message.getMessageId() + "@[" + StringUtils.join(replicas_, ", ") + "]");
            MessagingService.instance().addCallback(new DigestResponseHandler(), message.getMessageId());
            for (InetAddress endpoint : replicas_)
            {
                if (!endpoint.equals(dataSource))
                    MessagingService.instance().sendOneWay(message, endpoint);
            }
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}
    private ReadCommand constructReadMessage(boolean isDigestQuery)
    {
        ReadCommand readCommand = readCommand_.copy();
        readCommand.setDigestQuery(isDigestQuery);
        return readCommand;
    }
    class DigestResponseHandler implements IAsyncCallback
	{
        private boolean repairInvoked;
        private final ByteBuffer localDigest = ColumnFamily.digest(row_.cf);
        public synchronized void response(Message response)
		{
            if (repairInvoked)
                return;
            try
            {
                byte[] body = response.getMessageBody();
                ByteArrayInputStream bufIn = new ByteArrayInputStream(body);
                ReadResponse result = ReadResponse.serializer().deserialize(new DataInputStream(bufIn));
                ByteBuffer digest = result.digest();
                if (!localDigest.equals(digest))
                {
                    ReadCommand readCommand = constructReadMessage(false);
                    Message message = readCommand.makeReadMessage();
                    if (logger_.isDebugEnabled())
                        logger_.debug("Digest mismatch; re-reading " + readCommand_.key + " from " + message.getMessageId() + "@[" + StringUtils.join(replicas_, ", ") + "]");                         
                    MessagingService.instance().addCallback(new DataRepairHandler(), message.getMessageId());
                    for (InetAddress endpoint : replicas_)
                    {
                        if (!endpoint.equals(dataSource))
                            MessagingService.instance().sendOneWay(message, endpoint);
                    }
                    repairInvoked = true;
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException("Error handling responses for " + row_, e);
            }
        }
    }
    class DataRepairHandler implements IAsyncCallback
	{
		private final ReadResponseResolver readResponseResolver_;
		private final int majority_;
        public DataRepairHandler() throws IOException
        {
            readResponseResolver_ = new ReadResponseResolver(readCommand_.table, readCommand_.key);
            majority_ = (replicas_.size() / 2) + 1;
            ReadResponse readResponse = new ReadResponse(row_);
            Message fakeMessage = new Message(dataSource, StorageService.Verb.INTERNAL_RESPONSE, ArrayUtils.EMPTY_BYTE_ARRAY);
            readResponseResolver_.injectPreProcessed(fakeMessage, readResponse);
        }
		public synchronized void response(Message message)
		{
			if (logger_.isDebugEnabled())
			  logger_.debug("Received response in DataRepairHandler : " + message.toString());
            readResponseResolver_.preprocess(message);
            if (readResponseResolver_.getMessageCount() == majority_)
            {
                Runnable runnable = new WrappedRunnable()
                {
                    public void runMayThrow() throws IOException, DigestMismatchException
                    {
                        readResponseResolver_.resolve();
                    }
                };
                executor_.schedule(runnable, DatabaseDescriptor.getRpcTimeout(), TimeUnit.MILLISECONDS);
            }
        }
    }
}
