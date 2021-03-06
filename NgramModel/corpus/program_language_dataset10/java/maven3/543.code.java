package org.apache.maven.toolchain.java;
import java.io.File;
import org.apache.maven.toolchain.MisconfiguredToolchainException;
import org.apache.maven.toolchain.RequirementMatcherFactory;
import org.apache.maven.toolchain.ToolchainFactory;
import org.apache.maven.toolchain.ToolchainPrivate;
import org.apache.maven.toolchain.model.ToolchainModel;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
@Component( role = ToolchainFactory.class, hint = "jdk" )
public class DefaultJavaToolchainFactory
    implements ToolchainFactory
{
    @Requirement
    private Logger logger;
    public DefaultJavaToolchainFactory()
    {
    }
    public ToolchainPrivate createToolchain( ToolchainModel model )
        throws MisconfiguredToolchainException
    {
        if ( model == null )
        {
            return null;
        }
        DefaultJavaToolChain jtc = new DefaultJavaToolChain( model, logger );
        Xpp3Dom dom = (Xpp3Dom) model.getConfiguration();
        Xpp3Dom javahome = dom.getChild( DefaultJavaToolChain.KEY_JAVAHOME );
        if ( javahome == null )
        {
            throw new MisconfiguredToolchainException( "Java toolchain without the "
                + DefaultJavaToolChain.KEY_JAVAHOME + " configuration element." );
        }
        File normal = new File( FileUtils.normalize( javahome.getValue() ) );
        if ( normal.exists() )
        {
            jtc.setJavaHome( FileUtils.normalize( javahome.getValue() ) );
        }
        else
        {
            throw new MisconfiguredToolchainException( "Non-existing JDK home configuration at "
                + normal.getAbsolutePath() );
        }
        dom = (Xpp3Dom) model.getProvides();
        Xpp3Dom[] provides = dom.getChildren();
        for ( int i = 0; i < provides.length; i++ )
        {
            String key = provides[i].getName();
            String value = provides[i].getValue();
            if ( value == null )
            {
                throw new MisconfiguredToolchainException( "Provides token '" + key + "' doesn't have any value configured." );
            }
            if ( "version".equals( key ) )
            {
                jtc.addProvideToken( key,
                    RequirementMatcherFactory.createVersionMatcher( value ) );
            }
            else
            {
                jtc.addProvideToken( key,
                    RequirementMatcherFactory.createExactMatcher( value ) );
            }
        }
        return jtc;
    }
    public ToolchainPrivate createDefaultToolchain()
    {
        return null;
    }
    protected Logger getLogger()
    {
        return logger;
    }
}