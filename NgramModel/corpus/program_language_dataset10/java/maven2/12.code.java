package org.apache.maven.artifact.handler;
public interface ArtifactHandler
{
    String ROLE = ArtifactHandler.class.getName();
    String getExtension();
    String getDirectory();
    String getClassifier();
    String getPackaging();
    boolean isIncludesDependencies();
    String getLanguage();
    boolean isAddedToClasspath();
}
