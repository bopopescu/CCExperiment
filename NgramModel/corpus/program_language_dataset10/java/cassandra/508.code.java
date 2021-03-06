package org.apache.cassandra.locator;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.service.StorageService;
import org.junit.Test;
import org.apache.cassandra.utils.FBUtilities;
public class DynamicEndpointSnitchTest
{
    @Test
    public void testSnitch() throws InterruptedException, IOException, ConfigurationException
    {
        StorageService.instance.initClient();
        int sleeptime = 150;
        DynamicEndpointSnitch dsnitch = new DynamicEndpointSnitch(new SimpleSnitch());
        InetAddress self = FBUtilities.getLocalAddress();
        ArrayList<InetAddress> order = new ArrayList<InetAddress>();
        InetAddress host1 = InetAddress.getByName("127.0.0.1");
        InetAddress host2 = InetAddress.getByName("127.0.0.2");
        InetAddress host3 = InetAddress.getByName("127.0.0.3");
        for (int i = 0; i < 5; i++)
        {
            dsnitch.receiveTiming(host1, 1.0);
            dsnitch.receiveTiming(host2, 1.0);
            dsnitch.receiveTiming(host3, 1.0);
        }
        Thread.sleep(sleeptime);
        order.add(host1);
        order.add(host2);
        order.add(host3);
        assert dsnitch.getSortedListByProximity(self, order).equals(order);
        dsnitch.receiveTiming(host1, 2.0);
        Thread.sleep(sleeptime);
        order.clear();
        order.add(host2);
        order.add(host3);
        order.add(host1);
        assert dsnitch.getSortedListByProximity(self, order).equals(order);
        dsnitch.receiveTiming(host2, 2.0);
        Thread.sleep(sleeptime);
        order.clear();
        order.add(host3);
        order.add(host1);
        order.add(host2);
        assert dsnitch.getSortedListByProximity(self, order).equals(order);
        for (int i = 0; i < 2; i++)
        {
            dsnitch.receiveTiming(host3, 2.0);
        }
        Thread.sleep(sleeptime);
        order.clear();
        order.add(host1);
        order.add(host2);
        order.add(host3);
        assert dsnitch.getSortedListByProximity(self, order).equals(order);
        for (int i = 0; i < 2; i++)
        {
            dsnitch.receiveTiming(host3, 1.0);
        }
        Thread.sleep(sleeptime);
        order.clear();
        order.add(host1);
        order.add(host2);
        order.add(host3);
        assert dsnitch.getSortedListByProximity(self, order).equals(order);
    }
}
