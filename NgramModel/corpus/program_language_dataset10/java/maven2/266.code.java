package org.apache.maven.profiles;
import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationFile;
import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Repository;
import java.util.Iterator;
import java.util.List;
public class ProfilesConversionUtils
{
    private ProfilesConversionUtils()
    {
    }
    public static Profile convertFromProfileXmlProfile( org.apache.maven.profiles.Profile profileXmlProfile )
    {
        Profile profile = new Profile();
        profile.setId( profileXmlProfile.getId() );
        profile.setSource( "profiles.xml" );
        org.apache.maven.profiles.Activation profileActivation = profileXmlProfile.getActivation();
        if ( profileActivation != null )
        {
            Activation activation = new Activation();
            activation.setActiveByDefault( profileActivation.isActiveByDefault() );
            activation.setJdk( profileActivation.getJdk() );
            org.apache.maven.profiles.ActivationProperty profileProp = profileActivation.getProperty();
            if ( profileProp != null )
            {
                ActivationProperty prop = new ActivationProperty();
                prop.setName( profileProp.getName() );
                prop.setValue( profileProp.getValue() );
                activation.setProperty( prop );
            }
            ActivationOS profileOs = profileActivation.getOs();
            if ( profileOs != null )
            {
                org.apache.maven.model.ActivationOS os = new org.apache.maven.model.ActivationOS();
                os.setArch( profileOs.getArch() );
                os.setFamily( profileOs.getFamily() );
                os.setName( profileOs.getName() );
                os.setVersion( profileOs.getVersion() );
                activation.setOs( os );
            }
            org.apache.maven.profiles.ActivationFile profileFile = profileActivation.getFile();
            if ( profileFile != null )
            {
                ActivationFile file = new ActivationFile();
                file.setExists( profileFile.getExists() );
                file.setMissing( profileFile.getMissing() );
                activation.setFile( file );
            }
            profile.setActivation( activation );
        }
        profile.setProperties( profileXmlProfile.getProperties() );
        List repos = profileXmlProfile.getRepositories();
        if ( repos != null )
        {
            for ( Iterator it = repos.iterator(); it.hasNext(); )
            {
                profile
                    .addRepository(
                        convertFromProfileXmlRepository( (org.apache.maven.profiles.Repository) it.next() ) );
            }
        }
        List pluginRepos = profileXmlProfile.getPluginRepositories();
        if ( pluginRepos != null )
        {
            for ( Iterator it = pluginRepos.iterator(); it.hasNext(); )
            {
                profile.addPluginRepository( convertFromProfileXmlRepository( (org.apache.maven.profiles.Repository) it
                    .next() ) );
            }
        }
        return profile;
    }
    private static Repository convertFromProfileXmlRepository( org.apache.maven.profiles.Repository profileXmlRepo )
    {
        Repository repo = new Repository();
        repo.setId( profileXmlRepo.getId() );
        repo.setLayout( profileXmlRepo.getLayout() );
        repo.setName( profileXmlRepo.getName() );
        repo.setUrl( profileXmlRepo.getUrl() );
        if ( profileXmlRepo.getSnapshots() != null )
        {
            repo.setSnapshots( convertRepositoryPolicy( profileXmlRepo.getSnapshots() ) );
        }
        if ( profileXmlRepo.getReleases() != null )
        {
            repo.setReleases( convertRepositoryPolicy( profileXmlRepo.getReleases() ) );
        }
        return repo;
    }
    private static org.apache.maven.model.RepositoryPolicy convertRepositoryPolicy( RepositoryPolicy profileXmlRepo )
    {
        org.apache.maven.model.RepositoryPolicy policy = new org.apache.maven.model.RepositoryPolicy();
        policy.setEnabled( profileXmlRepo.isEnabled() );
        policy.setUpdatePolicy( profileXmlRepo.getUpdatePolicy() );
        policy.setChecksumPolicy( profileXmlRepo.getChecksumPolicy() );
        return policy;
    }
}
