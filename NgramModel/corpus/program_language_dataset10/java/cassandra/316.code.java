package org.apache.cassandra.locator;
import java.net.InetAddress;
public class RackInferringSnitch extends AbstractNetworkTopologySnitch
{
    public String getRack(InetAddress endpoint)
    {
        return Byte.toString(endpoint.getAddress()[2]);
    }
    public String getDatacenter(InetAddress endpoint)
    {
        return Byte.toString(endpoint.getAddress()[1]);
    }
}
