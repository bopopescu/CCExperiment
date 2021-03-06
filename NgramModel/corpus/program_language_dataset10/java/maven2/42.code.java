package org.apache.maven.artifact.resolver;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.artifact.Artifact;
public class MultipleArtifactsNotFoundException
    extends ArtifactResolutionException
{
    private final List resolvedArtifacts;
    private final List missingArtifacts;
    public MultipleArtifactsNotFoundException( Artifact originatingArtifact, List missingArtifacts, List remoteRepositories )
    {
        this( originatingArtifact, new ArrayList(), missingArtifacts, remoteRepositories );
    }
    public MultipleArtifactsNotFoundException( Artifact originatingArtifact, List resolvedArtifacts,
                                               List missingArtifacts, List remoteRepositories )
    {
        super( constructMessage( missingArtifacts ), originatingArtifact, remoteRepositories );
        this.resolvedArtifacts = resolvedArtifacts;
        this.missingArtifacts = missingArtifacts;
    }
    public List getResolvedArtifacts()
    {
        return resolvedArtifacts;
    }
    public List getMissingArtifacts()
    {
        return missingArtifacts;
    }
    private static String constructMessage( List artifacts )
    {
        StringBuffer buffer = new StringBuffer( "Missing:\n" );
        buffer.append( "----------\n" );
        int counter = 0;
        for ( Iterator i = artifacts.iterator(); i.hasNext(); )
        {
            Artifact artifact = (Artifact) i.next();
            String message = ( ++counter ) + ") " + artifact.getId();
            buffer.append( constructMissingArtifactMessage( message, "  ", artifact.getGroupId(), artifact
                .getArtifactId(), artifact.getVersion(), artifact.getType(), artifact.getClassifier(), artifact.getDownloadUrl(), artifact
                .getDependencyTrail() ) );
        }
        buffer.append( "----------\n" );
        int size = artifacts.size();
        buffer.append( size ).append( " required artifact" );
        if ( size > 1 )
        {
            buffer.append( "s are" );
        }
        else
        {
            buffer.append( " is" );
        }
        buffer.append( " missing.\n\nfor artifact: " );
        return buffer.toString();
    }
}
