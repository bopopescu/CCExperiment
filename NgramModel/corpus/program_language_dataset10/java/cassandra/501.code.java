package org.apache.cassandra.io.sstable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.columniterator.SSTableNamesIterator;
import org.apache.cassandra.utils.FBUtilities;
import org.junit.BeforeClass;
import org.junit.Test;
public class LegacySSTableTest extends CleanupHelper
{
    public static final String LEGACY_SSTABLE_PROP = "legacy-sstable-root";
    public static final String KSNAME = "Keyspace1";
    public static final String CFNAME = "Standard1";
    public static Set<String> TEST_DATA;
    public static File LEGACY_SSTABLE_ROOT;
    @BeforeClass
    public static void beforeClass()
    {
        String scp = System.getProperty(LEGACY_SSTABLE_PROP);
        assert scp != null;
        LEGACY_SSTABLE_ROOT = new File(scp).getAbsoluteFile();
        assert LEGACY_SSTABLE_ROOT.isDirectory();
        TEST_DATA = new HashSet<String>();
        for (int i = 100; i < 1000; ++i)
            TEST_DATA.add(Integer.toString(i));
    }
    protected Descriptor getDescriptor(String ver) throws IOException
    {
        File directory = new File(LEGACY_SSTABLE_ROOT + File.separator + ver + File.separator + KSNAME);
        return new Descriptor(ver, directory, KSNAME, CFNAME, 0, false);
    }
    @Test
    public void testVersions() throws Throwable
    {
        for (File version : LEGACY_SSTABLE_ROOT.listFiles())
            if (Descriptor.versionValidate(version.getName()))
                testVersion(version.getName());
    }
    public void testVersion(String version) throws Throwable
    {
        try
        {
            SSTableReader reader = SSTableReader.open(getDescriptor(version));
            for (String keystring : TEST_DATA)
            {
                ByteBuffer key = ByteBuffer.wrap(keystring.getBytes());
                DecoratedKey dk = reader.partitioner.decorateKey(key);
                SSTableNamesIterator iter = new SSTableNamesIterator(reader, dk, FBUtilities.singleton(key));
                assert iter.next().name().equals(key);
            }
        }
        catch (Throwable e)
        {
            System.err.println("Failed to read " + version);
            throw e;
        }
    }
}
