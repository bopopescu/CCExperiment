package org.apache.cassandra.service;
import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import java.io.File;
import java.io.IOException;
public class StorageServiceClientTest
{
    @Test
    public void testClientOnlyMode() throws IOException, ConfigurationException
    {
        CleanupHelper.mkdirs();
        CleanupHelper.cleanup();
        StorageService.instance.initClient();
        for (String path : DatabaseDescriptor.getAllDataFileLocations())
        {
            assertFalse(new File(path).exists());
        }
        StorageService.instance.stopClient();
    }
}
