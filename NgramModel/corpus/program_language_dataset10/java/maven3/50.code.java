package org.apache.maven.artifact;
import java.util.HashMap;
import java.util.Map;
public final class ArtifactStatus
    implements Comparable<ArtifactStatus>
{
    public static final ArtifactStatus NONE = new ArtifactStatus( "none", 0 );
    public static final ArtifactStatus GENERATED = new ArtifactStatus( "generated", 1 );
    public static final ArtifactStatus CONVERTED = new ArtifactStatus( "converted", 2 );
    public static final ArtifactStatus PARTNER = new ArtifactStatus( "partner", 3 );
    public static final ArtifactStatus DEPLOYED = new ArtifactStatus( "deployed", 4 );
    public static final ArtifactStatus VERIFIED = new ArtifactStatus( "verified", 5 );
    private final int rank;
    private final String key;
    private static Map<String, ArtifactStatus> map;
    private ArtifactStatus( String key, int rank )
    {
        this.rank = rank;
        this.key = key;
        if ( map == null )
        {
            map = new HashMap<String, ArtifactStatus>();
        }
        map.put( key, this );
    }
    public static ArtifactStatus valueOf( String status )
    {
        ArtifactStatus retVal = null;
        if ( status != null )
        {
            retVal = map.get( status );
        }
        return retVal != null ? retVal : NONE;
    }
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        final ArtifactStatus that = (ArtifactStatus) o;
        return rank == that.rank;
    }
    public int hashCode()
    {
        return rank;
    }
    public String toString()
    {
        return key;
    }
    public int compareTo( ArtifactStatus s )
    {
        return rank - s.rank;
    }
}
