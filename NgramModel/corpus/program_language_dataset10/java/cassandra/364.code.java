package org.apache.cassandra.service;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import com.google.common.collect.AbstractIterator;
import org.apache.commons.collections.iterators.CollatingIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.RangeSliceReply;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.utils.Pair;
import org.apache.cassandra.utils.ReducingIterator;
public class RangeSliceResponseResolver implements IResponseResolver<List<Row>>
{
    private static final Logger logger_ = LoggerFactory.getLogger(RangeSliceResponseResolver.class);
    private final String table;
    private final List<InetAddress> sources;
    protected final Collection<Message> responses = new LinkedBlockingQueue<Message>();;
    public RangeSliceResponseResolver(String table, List<InetAddress> sources)
    {
        assert sources.size() > 0;
        this.sources = sources;
        this.table = table;
    }
    public List<Row> getData() throws IOException
    {
        Message response = responses.iterator().next();
        RangeSliceReply reply = RangeSliceReply.read(response.getMessageBody());
        return reply.rows;
    }
    public List<Row> resolve() throws IOException
    {
        CollatingIterator collator = new CollatingIterator(new Comparator<Pair<Row,InetAddress>>()
        {
            public int compare(Pair<Row,InetAddress> o1, Pair<Row,InetAddress> o2)
            {
                return o1.left.key.compareTo(o2.left.key);
            }
        });
        int n = 0;
        for (Message response : responses)
        {
            RangeSliceReply reply = RangeSliceReply.read(response.getMessageBody());
            n = Math.max(n, reply.rows.size());
            collator.addIterator(new RowIterator(reply.rows.iterator(), response.getFrom()));
        }
        ReducingIterator<Pair<Row,InetAddress>, Row> iter = new ReducingIterator<Pair<Row,InetAddress>, Row>(collator)
        {
            List<ColumnFamily> versions = new ArrayList<ColumnFamily>(sources.size());
            List<InetAddress> versionSources = new ArrayList<InetAddress>(sources.size());
            DecoratedKey key;
            @Override
            protected boolean isEqual(Pair<Row, InetAddress> o1, Pair<Row, InetAddress> o2)
            {
                return o1.left.key.equals(o2.left.key);
            }
            public void reduce(Pair<Row,InetAddress> current)
            {
                key = current.left.key;
                versions.add(current.left.cf);
                versionSources.add(current.right);
            }
            protected Row getReduced()
            {
                ColumnFamily resolved = ReadResponseResolver.resolveSuperset(versions);
                ReadResponseResolver.maybeScheduleRepairs(resolved, table, key, versions, versionSources);
                versions.clear();
                versionSources.clear();
                return new Row(key, resolved);
            }
        };
        List<Row> resolvedRows = new ArrayList<Row>(n);
        while (iter.hasNext())
            resolvedRows.add(iter.next());
        return resolvedRows;
    }
    public void preprocess(Message message)
    {
        responses.add(message);
    }
    public boolean isDataPresent()
    {
        return !responses.isEmpty();
    }
    private static class RowIterator extends AbstractIterator<Pair<Row,InetAddress>>
    {
        private final Iterator<Row> iter;
        private final InetAddress source;
        private RowIterator(Iterator<Row> iter, InetAddress source)
        {
            this.iter = iter;
            this.source = source;
        }
        @Override
        protected Pair<Row,InetAddress> computeNext()
        {
            return iter.hasNext() ? new Pair<Row, InetAddress>(iter.next(), source) : endOfData();
        }
    }
    public Iterable<Message> getMessages()
    {
        return responses;
    }
    public int getMessageCount()
    {
        return responses.size();
    }
}
