package org.apache.cassandra.service;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.config.DatabaseDescriptor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
public class StorageServiceServerTest
{
    @Test
    public void testRegularMode() throws IOException, InterruptedException, ConfigurationException
    {
        CleanupHelper.mkdirs();
        CleanupHelper.cleanup();
        StorageService.instance.initServer();
        for (String path : DatabaseDescriptor.getAllDataFileLocations())
        {
            assertTrue(new File(path).exists());
        }
        StorageService.instance.stopClient();
    }
    @Test
    public void testGetAllRangesEmpty()
    {
        List<Token> toks = Collections.emptyList();
        assertEquals(Collections.emptyList(), StorageService.instance.getAllRanges(toks));
    }
    @Test
    public void testSnapshot() throws IOException
    {
        StorageService.instance.takeAllSnapshot(null);
    }
}
