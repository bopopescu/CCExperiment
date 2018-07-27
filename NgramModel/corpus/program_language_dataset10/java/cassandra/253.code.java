package org.apache.cassandra.gms;
import java.net.InetAddress;
public interface IFailureNotification
{   
    public void convict(InetAddress ep);
    public void revive(InetAddress ep);
}
