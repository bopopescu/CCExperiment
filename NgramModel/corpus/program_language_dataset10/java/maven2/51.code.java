package org.apache.maven.artifact.resolver.filter;
import org.apache.maven.artifact.Artifact;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
public class ExclusionSetFilter
    implements ArtifactFilter
{
    private Set excludes;
    public ExclusionSetFilter( String[] excludes )
    {
        this.excludes = new HashSet( Arrays.asList( excludes ) );
    }
    public ExclusionSetFilter( Set excludes )
    {
        this.excludes = excludes;
    }
    public boolean include( Artifact artifact )
    {
        if ( excludes.contains( artifact.getArtifactId() ) )
        {
            return false;
        }
        return true;
    }
}
