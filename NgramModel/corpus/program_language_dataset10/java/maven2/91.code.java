package org.apache.maven.artifact.resolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.codehaus.plexus.logging.Logger;
import java.util.Set;
import java.util.HashSet;
public class DebugResolutionListener
    implements ResolutionListener, ResolutionListenerForDepMgmt
{
    private Logger logger;
    private String indent = "";
    private static Set ignoredArtifacts = new HashSet();
    public DebugResolutionListener( Logger logger )
    {
        this.logger = logger;
    }
    public void testArtifact( Artifact node )
    {
    }
    public void startProcessChildren( Artifact artifact )
    {
        indent += "  ";
    }
    public void endProcessChildren( Artifact artifact )
    {
        indent = indent.substring( 2 );
    }
    public void includeArtifact( Artifact artifact )
    {
        logger.debug( indent + artifact + " (selected for " + artifact.getScope() + ")" );
    }
    public void omitForNearer( Artifact omitted, Artifact kept )
    {
        String omittedVersion = omitted.getVersion();
        String keptVersion = kept.getVersion();
        if ( omittedVersion != null ? !omittedVersion.equals( keptVersion ) : keptVersion != null )
        {
            logger.debug( indent + omitted + " (removed - nearer found: " + kept.getVersion() + ")" );
        }
    }
    public void omitForCycle( Artifact omitted )
    {
        logger.debug( indent + omitted + " (removed - causes a cycle in the graph)" );
    }
    public void updateScopeCurrentPom( Artifact artifact, String ignoredScope )
    {
        logger.debug( indent + artifact + " (not setting scope to: " + ignoredScope + "; local scope "
                      + artifact.getScope() + " wins)" );
        if ( !ignoredArtifacts.contains( artifact ) )
        {
            logger.warn( "\n\tArtifact " + artifact + " retains local scope '" + artifact.getScope()
                + "' overriding broader scope '" + ignoredScope + "'\n"
                + "\tgiven by a dependency. If this is not intended, modify or remove the local scope.\n" );
            ignoredArtifacts.add( artifact );
        }
    }
    public void updateScope( Artifact artifact, String scope )
    {
        logger.debug( indent + artifact + " (setting scope to: " + scope + ")" );
    }
    public void selectVersionFromRange( Artifact artifact )
    {
        logger.debug( indent + artifact + " (setting version to: " + artifact.getVersion() + " from range: "
            + artifact.getVersionRange() + ")" );
    }
    public void restrictRange( Artifact artifact, Artifact replacement, VersionRange newRange )
    {
        logger.debug( indent + artifact + " (range restricted from: " + artifact.getVersionRange() + " and: "
            + replacement.getVersionRange() + " to: " + newRange + " )" );
    }
    public void manageArtifact( Artifact artifact, Artifact replacement )
    {
        String msg = indent + artifact;
        msg += " (";
        if ( replacement.getVersion() != null )
        {
            msg += "applying version: " + replacement.getVersion() + ";";
        }
        if ( replacement.getScope() != null )
        {
            msg += "applying scope: " + replacement.getScope();
        }
        msg += ")";
        logger.debug( msg );
    }
    public void manageArtifactVersion( Artifact artifact, Artifact replacement )
    {
        if ( !replacement.getVersion().equals( artifact.getVersion() ) )
        {
            String msg = indent + artifact + " (applying version: " + replacement.getVersion() + ")";
            logger.debug( msg );
        }
    }
    public void manageArtifactScope( Artifact artifact, Artifact replacement )
    {
        if ( !replacement.getScope().equals( artifact.getScope() ) )
        {
            String msg = indent + artifact + " (applying scope: " + replacement.getScope() + ")";
            logger.debug( msg );
        }
    }
}