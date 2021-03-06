package org.apache.maven.model.resolution;
public class UnresolvableModelException
    extends Exception
{
    private final String groupId;
    private final String artifactId;
    private final String version;
    public UnresolvableModelException( String message, String groupId, String artifactId, String version,
                                       Throwable cause )
    {
        super( message, cause );
        this.groupId = ( groupId != null ) ? groupId : "";
        this.artifactId = ( artifactId != null ) ? artifactId : "";
        this.version = ( version != null ) ? version : "";
    }
    public UnresolvableModelException( String message, String groupId, String artifactId, String version )
    {
        super( message );
        this.groupId = ( groupId != null ) ? groupId : "";
        this.artifactId = ( artifactId != null ) ? artifactId : "";
        this.version = ( version != null ) ? version : "";
    }
    public String getGroupId()
    {
        return groupId;
    }
    public String getArtifactId()
    {
        return artifactId;
    }
    public String getVersion()
    {
        return version;
    }
}
