package org.apache.maven.model.profile.activation;
import java.io.File;
import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationFile;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblem.Severity;
import org.apache.maven.model.path.PathTranslator;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.interpolation.AbstractValueSource;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.codehaus.plexus.util.StringUtils;
@Component( role = ProfileActivator.class, hint = "file" )
public class FileProfileActivator
    implements ProfileActivator
{
    @Requirement
    private PathTranslator pathTranslator;
    public FileProfileActivator setPathTranslator( PathTranslator pathTranslator )
    {
        this.pathTranslator = pathTranslator;
        return this;
    }
    public boolean isActive( Profile profile, ProfileActivationContext context, ModelProblemCollector problems )
    {
        Activation activation = profile.getActivation();
        if ( activation == null )
        {
            return false;
        }
        ActivationFile file = activation.getFile();
        if ( file == null )
        {
            return false;
        }
        String path;
        boolean missing;
        if ( StringUtils.isNotEmpty( file.getExists() ) )
        {
            path = file.getExists();
            missing = false;
        }
        else if ( StringUtils.isNotEmpty( file.getMissing() ) )
        {
            path = file.getMissing();
            missing = true;
        }
        else
        {
            return false;
        }
        RegexBasedInterpolator interpolator = new RegexBasedInterpolator();
        final File basedir = context.getProjectDirectory();
        if ( basedir != null )
        {
            interpolator.addValueSource( new AbstractValueSource( false )
            {
                public Object getValue( String expression )
                {
                    if ( "basedir".equals( expression ) )
                    {
                        return basedir.getAbsolutePath();
                    }
                    return null;
                }
            } );
        }
        else if ( path.indexOf( "${basedir}" ) >= 0 )
        {
            return false;
        }
        interpolator.addValueSource( new MapBasedValueSource( context.getUserProperties() ) );
        interpolator.addValueSource( new MapBasedValueSource( context.getSystemProperties() ) );
        try
        {
            path = interpolator.interpolate( path, "" );
        }
        catch ( Exception e )
        {
            problems.add( Severity.ERROR, "Failed to interpolate file location " + path + " for profile "
                + profile.getId() + ": " + e.getMessage(), file.getLocation( missing ? "missing" : "exists" ), e );
            return false;
        }
        path = pathTranslator.alignToBaseDirectory( path, basedir );
        File f = new File( path );
        if ( !f.isAbsolute() )
        {
            return false;
        }
        boolean fileExists = f.exists();
        return missing ? !fileExists : fileExists;
    }
}
