package org.apache.cassandra.db.columniterator;
import org.apache.cassandra.db.SuperColumn;
import org.apache.cassandra.db.filter.SliceQueryFilter;
import org.apache.cassandra.utils.ByteBufferUtil;
public class IdentityQueryFilter extends SliceQueryFilter
{
    public IdentityQueryFilter()
    {
        super(ByteBufferUtil.EMPTY_BYTE_BUFFER, ByteBufferUtil.EMPTY_BYTE_BUFFER, false, Integer.MAX_VALUE);
    }
    public SuperColumn filterSuperColumn(SuperColumn superColumn, int gcBefore)
    {
        return superColumn;
    }
}
