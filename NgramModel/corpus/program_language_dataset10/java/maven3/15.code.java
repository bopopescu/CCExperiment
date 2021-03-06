package org.apache.maven.repository.internal;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.ArtifactProperties;
final class VersionsMetadata
    extends MavenMetadata
{
    private final Artifact artifact;
    public VersionsMetadata( Artifact artifact )
    {
        super( createMetadata( artifact ), null );
        this.artifact = artifact;
    }
    public VersionsMetadata( Artifact artifact, File file )
    {
        super( createMetadata( artifact ), file );
        this.artifact = artifact;
    }
    private static Metadata createMetadata( Artifact artifact )
    {
        Versioning versioning = new Versioning();
        versioning.addVersion( artifact.getBaseVersion() );
        if ( !artifact.isSnapshot() )
        {
            versioning.setRelease( artifact.getBaseVersion() );
        }
        if ( "maven-plugin".equals( artifact.getProperty( ArtifactProperties.TYPE, "" ) ) )
        {
            versioning.setLatest( artifact.getBaseVersion() );
        }
        Metadata metadata = new Metadata();
        metadata.setVersioning( versioning );
        metadata.setGroupId( artifact.getGroupId() );
        metadata.setArtifactId( artifact.getArtifactId() );
        return metadata;
    }
    @Override
    protected void merge( Metadata recessive )
    {
        Versioning versioning = metadata.getVersioning();
        versioning.updateTimestamp();
        if ( recessive.getVersioning() != null )
        {
            if ( versioning.getLatest() == null )
            {
                versioning.setLatest( recessive.getVersioning().getLatest() );
            }
            if ( versioning.getRelease() == null )
            {
                versioning.setRelease( recessive.getVersioning().getRelease() );
            }
            Collection<String> versions = new LinkedHashSet<String>( recessive.getVersioning().getVersions() );
            versions.addAll( versioning.getVersions() );
            versioning.setVersions( new ArrayList<String>( versions ) );
        }
    }
    public Object getKey()
    {
        return getGroupId() + ':' + getArtifactId();
    }
    public static Object getKey( Artifact artifact )
    {
        return artifact.getGroupId() + ':' + artifact.getArtifactId();
    }
    public MavenMetadata setFile( File file )
    {
        return new VersionsMetadata( artifact, file );
    }
    public String getGroupId()
    {
        return artifact.getGroupId();
    }
    public String getArtifactId()
    {
        return artifact.getArtifactId();
    }
    public String getVersion()
    {
        return "";
    }
    public Nature getNature()
    {
        return artifact.isSnapshot() ? Nature.RELEASE_OR_SNAPSHOT : Nature.RELEASE;
    }
}
