package org.apache.maven.artifact.manager;
public class WagonB
    extends WagonMock
{
    public String[] getSupportedProtocols()
    {
        return new String[]{ "b1", "b2" };
    }
}
