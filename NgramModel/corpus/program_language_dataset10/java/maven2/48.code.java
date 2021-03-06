package org.apache.maven.artifact.resolver.filter;
import org.apache.maven.artifact.Artifact;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class AndArtifactFilter
    implements ArtifactFilter
{
    private final List filters = new ArrayList();
    public boolean include( Artifact artifact )
    {
        boolean include = true;
        for ( Iterator i = filters.iterator(); i.hasNext() && include; )
        {
            ArtifactFilter filter = (ArtifactFilter) i.next();
            if ( !filter.include( artifact ) )
            {
                include = false;
            }
        }
        return include;
    }
    public void add( ArtifactFilter artifactFilter )
    {
        filters.add( artifactFilter );
    }
}
