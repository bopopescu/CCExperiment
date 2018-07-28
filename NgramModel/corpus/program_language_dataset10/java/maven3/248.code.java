package org.apache.maven.repository.metadata;
import org.apache.maven.artifact.ArtifactScopeEnum;
import org.apache.maven.repository.metadata.ArtifactMetadata;
import org.apache.maven.repository.metadata.ClasspathContainer;
import org.apache.maven.repository.metadata.ClasspathTransformation;
import org.apache.maven.repository.metadata.MetadataGraph;
import org.apache.maven.repository.metadata.MetadataGraphEdge;
import org.apache.maven.repository.metadata.MetadataGraphVertex;
import org.codehaus.plexus.PlexusTestCase;
public class DefaultClasspathTransformationTest
extends PlexusTestCase
{
	ClasspathTransformation transform;
	MetadataGraph graph;
	MetadataGraphVertex v1;
	MetadataGraphVertex v2;
	MetadataGraphVertex v3;
	MetadataGraphVertex v4;
    @Override
	protected void setUp() throws Exception
	{
		super.setUp();
		transform = (ClasspathTransformation) lookup( ClasspathTransformation.ROLE, "default" );
    	graph = new MetadataGraph( 4, 3 );
    	v1 = graph.addVertex(new ArtifactMetadata("g","a1","1.0"));
    	graph.setEntry(v1);
    	v2 = graph.addVertex(new ArtifactMetadata("g","a2","1.0"));
    	v3 = graph.addVertex(new ArtifactMetadata("g","a3","1.0"));
    	v4 = graph.addVertex(new ArtifactMetadata("g","a4","1.0"));
    	graph.addEdge(v1, v2, new MetadataGraphEdge( "1.1", true, null, null, 2, 1 ) );
    	graph.addEdge(v1, v2, new MetadataGraphEdge( "1.2", true, null, null, 2, 2 ) );
    	graph.addEdge(v1, v3, new MetadataGraphEdge( "1.1", true, null, null, 2, 1 ) );
    	graph.addEdge(v1, v3, new MetadataGraphEdge( "1.2", true, null, null, 4, 2 ) );
    	graph.addEdge(v3, v4, new MetadataGraphEdge( "1.1", true, ArtifactScopeEnum.runtime, null, 2, 2 ) );
    	graph.addEdge(v3, v4, new MetadataGraphEdge( "1.2", true, ArtifactScopeEnum.test, null, 2, 2 ) );
	}
    public void testCompileClasspathTransform()
    throws Exception
    {
    	ClasspathContainer res;
    	res = transform.transform( graph, ArtifactScopeEnum.compile, false );
       	assertNotNull("null classpath container after compile transform", res );
       	assertNotNull("null classpath after compile transform", res.getClasspath() );
       	assertEquals("compile classpath should have 3 entries", 3, res.getClasspath().size() );
    }
    public void testRuntimeClasspathTransform()
    throws Exception
    {
    	ClasspathContainer res;
    	res = transform.transform( graph, ArtifactScopeEnum.runtime, false );
       	assertNotNull("null classpath container after runtime transform", res );
       	assertNotNull("null classpath after runtime transform", res.getClasspath() );
       	assertEquals("runtime classpath should have 4 entries", 4, res.getClasspath().size() );
       	ArtifactMetadata md = res.getClasspath().get(3);
       	assertEquals("runtime artifact version should be 1.1", "1.1", md.getVersion() );
    }
    public void testTestClasspathTransform()
    throws Exception
    {
    	ClasspathContainer res;
    	res = transform.transform( graph, ArtifactScopeEnum.test, false );
       	assertNotNull("null classpath container after runtime transform", res );
       	assertNotNull("null classpath after runtime transform", res.getClasspath() );
       	assertEquals("runtime classpath should have 4 entries", 4, res.getClasspath().size() );
       	ArtifactMetadata md = res.getClasspath().get(3);
       	assertEquals("test artifact version should be 1.2", "1.2", md.getVersion() );
    }
}