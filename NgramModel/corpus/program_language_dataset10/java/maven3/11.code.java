package org.apache.maven.repository.internal;
import java.io.File;
import java.util.Map;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.AbstractArtifact;
final class RelocatedArtifact
    extends AbstractArtifact
{
    private final Artifact artifact;
    private final String groupId;
    private final String artifactId;
    private final String version;
    public RelocatedArtifact( Artifact artifact, String groupId, String artifactId, String version )
    {
        if ( artifact == null )
        {
            throw new IllegalArgumentException( "no artifact specified" );
        }
        this.artifact = artifact;
        this.groupId = ( groupId != null && groupId.length() > 0 ) ? groupId : null;
        this.artifactId = ( artifactId != null && artifactId.length() > 0 ) ? artifactId : null;
        this.version = ( version != null && version.length() > 0 ) ? version : null;
    }
    public String getGroupId()
    {
        if ( groupId != null )
        {
            return groupId;
        }
        else
        {
            return artifact.getGroupId();
        }
    }
    public String getArtifactId()
    {
        if ( artifactId != null )
        {
            return artifactId;
        }
        else
        {
            return artifact.getArtifactId();
        }
    }
    public String getVersion()
    {
        if ( version != null )
        {
            return version;
        }
        else
        {
            return artifact.getVersion();
        }
    }
    public String getBaseVersion()
    {
        return toBaseVersion( getVersion() );
    }
    public boolean isSnapshot()
    {
        return isSnapshot( getVersion() );
    }
    public String getClassifier()
    {
        return artifact.getClassifier();
    }
    public String getExtension()
    {
        return artifact.getExtension();
    }
    public File getFile()
    {
        return artifact.getFile();
    }
    public String getProperty( String key, String defaultValue )
    {
        return artifact.getProperty( key, defaultValue );
    }
    public Map<String, String> getProperties()
    {
        return artifact.getProperties();
    }
}
