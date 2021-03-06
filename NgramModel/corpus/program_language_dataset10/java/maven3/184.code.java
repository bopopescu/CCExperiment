package org.apache.maven.artifact.deployer;
import java.io.File;
import org.apache.maven.artifact.AbstractArtifactComponentTestCase;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.util.FileUtils;
public class ArtifactDeployerTest
    extends AbstractArtifactComponentTestCase
{
    private ArtifactDeployer artifactDeployer;
    protected void setUp()
        throws Exception
    {
        super.setUp();
        artifactDeployer = (ArtifactDeployer) lookup( ArtifactDeployer.ROLE );
    }
    protected String component()
    {
        return "deployer";
    }
    public void testArtifactInstallation()
        throws Exception
    {
        String artifactBasedir = new File( getBasedir(), "src/test/resources/artifact-install" ).getAbsolutePath();
        Artifact artifact = createArtifact( "artifact", "1.0" );
        File file = new File( artifactBasedir, "artifact-1.0.jar" );
        assertEquals( "dummy", FileUtils.fileRead( file, "UTF-8" ).trim() );
        artifactDeployer.deploy( file, artifact, remoteRepository(), localRepository() );
        ArtifactRepository remoteRepository = remoteRepository();
        File deployedFile = new File( remoteRepository.getBasedir(), remoteRepository.pathOf( artifact ) );
        assertTrue( deployedFile.exists() );
        assertEquals( "dummy", FileUtils.fileRead( deployedFile, "UTF-8" ).trim() );
    }
}