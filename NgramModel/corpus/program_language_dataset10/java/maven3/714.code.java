package org.apache.maven.model.building;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.apache.maven.model.Profile;
import org.apache.maven.model.resolution.ModelResolver;
public interface ModelBuildingRequest
{
    int VALIDATION_LEVEL_MINIMAL = 0;
    int VALIDATION_LEVEL_MAVEN_2_0 = 20;
    int VALIDATION_LEVEL_MAVEN_3_0 = 30;
    int VALIDATION_LEVEL_MAVEN_3_1 = 31;
    int VALIDATION_LEVEL_STRICT = VALIDATION_LEVEL_MAVEN_3_0;
    ModelSource getModelSource();
    ModelBuildingRequest setModelSource( ModelSource modelSource );
    File getPomFile();
    ModelBuildingRequest setPomFile( File pomFile );
    int getValidationLevel();
    ModelBuildingRequest setValidationLevel( int validationLevel );
    boolean isProcessPlugins();
    ModelBuildingRequest setProcessPlugins( boolean processPlugins );
    boolean isTwoPhaseBuilding();
    ModelBuildingRequest setTwoPhaseBuilding( boolean twoPhaseBuilding );
    boolean isLocationTracking();
    ModelBuildingRequest setLocationTracking( boolean locationTracking );
    List<Profile> getProfiles();
    ModelBuildingRequest setProfiles( List<Profile> profiles );
    List<String> getActiveProfileIds();
    ModelBuildingRequest setActiveProfileIds( List<String> activeProfileIds );
    List<String> getInactiveProfileIds();
    ModelBuildingRequest setInactiveProfileIds( List<String> inactiveProfileIds );
    Properties getSystemProperties();
    ModelBuildingRequest setSystemProperties( Properties systemProperties );
    Properties getUserProperties();
    ModelBuildingRequest setUserProperties( Properties userProperties );
    Date getBuildStartTime();
    ModelBuildingRequest setBuildStartTime( Date buildStartTime );
    ModelResolver getModelResolver();
    ModelBuildingRequest setModelResolver( ModelResolver modelResolver );
    ModelBuildingListener getModelBuildingListener();
    ModelBuildingRequest setModelBuildingListener( ModelBuildingListener modelBuildingListener );
    ModelCache getModelCache();
    ModelBuildingRequest setModelCache( ModelCache modelCache );
}
