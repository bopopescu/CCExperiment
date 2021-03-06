package org.apache.cassandra.streaming;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.io.sstable.Descriptor;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.utils.FBUtilities;
public class StreamIn
{
    private static Logger logger = LoggerFactory.getLogger(StreamIn.class);
    public static void requestRanges(InetAddress source, String tableName, Collection<Range> ranges, OperationType type)
    {
        requestRanges(source, tableName, ranges, null, type);
    }
    public static void requestRanges(InetAddress source, String tableName, Collection<Range> ranges, Runnable callback, OperationType type)
    {
        assert ranges.size() > 0;
        if (logger.isDebugEnabled())
            logger.debug("Requesting from {} ranges {}", source, StringUtils.join(ranges, ", "));
        StreamInSession session = StreamInSession.create(source, callback);
        Message message = new StreamRequestMessage(FBUtilities.getLocalAddress(), ranges, tableName, session.getSessionId(), type).makeMessage();
        MessagingService.instance().sendOneWay(message, source);
    }
    public static PendingFile getContextMapping(PendingFile remote) throws IOException
    {
        Descriptor remotedesc = remote.desc;
        Table table = Table.open(remotedesc.ksname);
        ColumnFamilyStore cfStore = table.getColumnFamilyStore(remotedesc.cfname);
        Descriptor localdesc = Descriptor.fromFilename(cfStore.getFlushPath());
        return new PendingFile(localdesc, remote);
     }
}
