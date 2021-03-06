package org.apache.maven.model.building;
import java.util.List;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
public interface ModelBuildingResult
{
    List<String> getModelIds();
    Model getEffectiveModel();
    Model getRawModel();
    Model getRawModel( String modelId );
    List<Profile> getActivePomProfiles( String modelId );
    List<Profile> getActiveExternalProfiles();
    List<ModelProblem> getProblems();
}
