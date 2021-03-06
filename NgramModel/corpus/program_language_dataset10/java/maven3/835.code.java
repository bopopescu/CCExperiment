package org.apache.maven.settings.crypto;
import java.util.List;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
public interface SettingsDecryptionRequest
{
    List<Server> getServers();
    SettingsDecryptionRequest setServers( List<Server> servers );
    List<Proxy> getProxies();
    SettingsDecryptionRequest setProxies( List<Proxy> proxies );
}
