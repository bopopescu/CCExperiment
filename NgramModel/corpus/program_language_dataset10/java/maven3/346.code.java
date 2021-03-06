package org.apache.maven.execution;
import org.apache.maven.settings.Settings;
public interface MavenExecutionRequestPopulator
{
    MavenExecutionRequest populateFromSettings( MavenExecutionRequest request, Settings settings )
        throws MavenExecutionRequestPopulationException;
    MavenExecutionRequest populateDefaults( MavenExecutionRequest request )
        throws MavenExecutionRequestPopulationException;
}
