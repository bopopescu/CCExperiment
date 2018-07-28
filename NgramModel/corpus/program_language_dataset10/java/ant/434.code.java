package org.apache.tools.ant.taskdefs.optional.extension;
import java.io.File;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.Manifest;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
public class JarLibAvailableTask extends Task {
    private File libraryFile;
    private final Vector extensionFileSets = new Vector();
    private String propertyName;
    private ExtensionAdapter requiredExtension;
    public void setProperty(final String property) {
        this.propertyName = property;
    }
    public void setFile(final File file) {
        this.libraryFile = file;
    }
    public void addConfiguredExtension(final ExtensionAdapter extension) {
        if (null != requiredExtension) {
            final String message = "Can not specify extension to "
                + "search for multiple times.";
            throw new BuildException(message);
        }
        requiredExtension = extension;
    }
    public void addConfiguredExtensionSet(final ExtensionSet extensionSet) {
        extensionFileSets.addElement(extensionSet);
    }
    public void execute() throws BuildException {
        validate();
        final Extension test = requiredExtension.toExtension();
        if (!extensionFileSets.isEmpty()) {
            final Iterator iterator = extensionFileSets.iterator();
            while (iterator.hasNext()) {
                final ExtensionSet extensionSet
                    = (ExtensionSet) iterator.next();
                final Extension[] extensions =
                    extensionSet.toExtensions(getProject());
                for (int i = 0; i < extensions.length; i++) {
                    final Extension extension = extensions[ i ];
                    if (extension.isCompatibleWith(test)) {
                        getProject().setNewProperty(propertyName, "true");
                    }
                }
            }
        } else {
            final Manifest manifest = ExtensionUtil.getManifest(libraryFile);
            final Extension[] extensions = Extension.getAvailable(manifest);
            for (int i = 0; i < extensions.length; i++) {
                final Extension extension = extensions[ i ];
                if (extension.isCompatibleWith(test)) {
                    getProject().setNewProperty(propertyName, "true");
                }
            }
        }
    }
    private void validate() throws BuildException {
        if (null == requiredExtension) {
            final String message = "Extension element must be specified.";
            throw new BuildException(message);
        }
        if (null == libraryFile && extensionFileSets.isEmpty()) {
            final String message = "File attribute not specified.";
            throw new BuildException(message);
        }
        if (null != libraryFile && !libraryFile.exists()) {
            final String message = "File '" + libraryFile + "' does not exist.";
            throw new BuildException(message);
        }
        if (null != libraryFile && !libraryFile.isFile()) {
            final String message = "\'" + libraryFile + "\' is not a file.";
            throw new BuildException(message);
        }
    }
}