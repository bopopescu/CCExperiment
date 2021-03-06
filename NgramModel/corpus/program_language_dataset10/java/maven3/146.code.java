package org.apache.maven.repository.legacy.resolver.conflict;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
@Component( role = ConflictResolverFactory.class )
public class DefaultConflictResolverFactory
    implements ConflictResolverFactory, Contextualizable
{
    @Requirement
    private PlexusContainer container;
    public ConflictResolver getConflictResolver( String type )
        throws ConflictResolverNotFoundException
    {
        try
        {
            return (ConflictResolver) container.lookup( ConflictResolver.ROLE, type );
        }
        catch ( ComponentLookupException exception )
        {
            throw new ConflictResolverNotFoundException( "Cannot find conflict resolver of type: " + type );
        }
    }
    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}
