package org.apache.tools.ant.taskdefs.optional.extension;
import java.io.File;
import java.util.ArrayList;
import java.util.jar.Manifest;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.optional.extension.resolvers.AntResolver;
import org.apache.tools.ant.taskdefs.optional.extension.resolvers.LocationResolver;
import org.apache.tools.ant.taskdefs.optional.extension.resolvers.URLResolver;
public class JarLibResolveTask extends Task {
    private String propertyName;
    private Extension requiredExtension;
    private final ArrayList resolvers = new ArrayList();
    private boolean checkExtension = true;
    private boolean failOnError = true;
    public void setProperty(final String property) {
        this.propertyName = property;
    }
    public void setCheckExtension(final boolean checkExtension) {
        this.checkExtension = checkExtension;
    }
    public void setFailOnError(final boolean failOnError) {
        this.failOnError = failOnError;
    }
    public void addConfiguredLocation(final LocationResolver loc) {
        resolvers.add(loc);
    }
    public void addConfiguredUrl(final URLResolver url) {
        resolvers.add(url);
    }
    public void addConfiguredAnt(final AntResolver ant) {
        resolvers.add(ant);
    }
    public void addConfiguredExtension(final ExtensionAdapter extension) {
        if (null != requiredExtension) {
            final String message = "Can not specify extension to "
                + "resolve multiple times.";
            throw new BuildException(message);
        }
        requiredExtension = extension.toExtension();
    }
    public void execute() throws BuildException {
        validate();
        getProject().log("Resolving extension: " + requiredExtension, Project.MSG_VERBOSE);
        String candidate = getProject().getProperty(propertyName);
        if (null != candidate) {
            final String message = "Property Already set to: " + candidate;
            if (failOnError) {
                throw new BuildException(message);
            }
            getProject().log(message, Project.MSG_ERR);
            return;
        }
        final int size = resolvers.size();
        for (int i = 0; i < size; i++) {
            final ExtensionResolver resolver =
                (ExtensionResolver) resolvers.get(i);
            getProject().log("Searching for extension using Resolver:" + resolver,
                    Project.MSG_VERBOSE);
            try {
                final File file = resolver.resolve(requiredExtension, getProject());
                try {
                    checkExtension(file);
                    return;
                } catch (final BuildException be) {
                    final String message = "File " + file + " returned by "
                            + "resolver failed to satisfy extension due to: " + be.getMessage();
                    getProject().log(message, Project.MSG_WARN);
                }
            } catch (final BuildException be) {
                final String message = "Failed to resolve extension to file " + "using resolver "
                        + resolver + " due to: " + be;
                getProject().log(message, Project.MSG_WARN);
            }
        }
        missingExtension();
    }
    private void missingExtension() {
        final String message = "Unable to resolve extension to a file";
        if (failOnError) {
            throw new BuildException(message);
        }
        getProject().log(message, Project.MSG_ERR);
    }
    private void checkExtension(final File file) {
        if (!file.exists()) {
            throw new BuildException("File " + file + " does not exist");
        }
        if (!file.isFile()) {
            throw new BuildException("File " + file + " is not a file");
        }
        if (!checkExtension) {
            getProject().log("Setting property to " + file
                    + " without verifying library satisfies extension", Project.MSG_VERBOSE);
            setLibraryProperty(file);
        } else {
            getProject().log("Checking file " + file + " to see if it satisfies extension",
                    Project.MSG_VERBOSE);
            final Manifest manifest = ExtensionUtil.getManifest(file);
            final Extension[] extensions = Extension.getAvailable(manifest);
            for (int i = 0; i < extensions.length; i++) {
                final Extension extension = extensions[ i ];
                if (extension.isCompatibleWith(requiredExtension)) {
                    setLibraryProperty(file);
                    return;
                }
            }
            final String message = "File " + file + " skipped as it "
                + "does not satisfy extension";
            getProject().log(message, Project.MSG_VERBOSE);
            throw new BuildException(message);
        }
    }
    private void setLibraryProperty(final File file) {
        getProject().setNewProperty(propertyName, file.getAbsolutePath());
    }
    private void validate() throws BuildException {
        if (null == propertyName) {
            final String message = "Property attribute must be specified.";
            throw new BuildException(message);
        }
        if (null == requiredExtension) {
            final String message = "Extension element must be specified.";
            throw new BuildException(message);
        }
    }
}