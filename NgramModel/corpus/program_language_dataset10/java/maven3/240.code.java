package org.apache.maven.repository.legacy;
import org.apache.maven.wagon.Wagon;
import org.codehaus.plexus.component.annotations.Component;
@Component(role=Wagon.class,hint="c")
public class WagonC
    extends WagonMock
{
    public String[] getSupportedProtocols()
    {
        return new String[]{ "c" };
    }
}
