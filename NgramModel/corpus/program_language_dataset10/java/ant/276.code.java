package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.Union;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.MergingMapper;
import org.apache.tools.ant.util.ResourceUtils;
import org.apache.tools.ant.util.SourceFileScanner;
public class UpToDate extends Task implements Condition {
    private String property;
    private String value;
    private File sourceFile;
    private File targetFile;
    private Vector sourceFileSets = new Vector();
    private Union sourceResources = new Union();
    protected Mapper mapperElement = null;
    public void setProperty(final String property) {
        this.property = property;
    }
    public void setValue(final String value) {
        this.value = value;
    }
    private String getValue() {
        return (value != null) ? value : "true";
    }
    public void setTargetFile(final File file) {
        this.targetFile = file;
    }
    public void setSrcfile(final File file) {
        this.sourceFile = file;
    }
    public void addSrcfiles(final FileSet fs) {
        sourceFileSets.addElement(fs);
    }
    public Union createSrcResources() {
        return sourceResources;
    }
    public Mapper createMapper() throws BuildException {
        if (mapperElement != null) {
            throw new BuildException("Cannot define more than one mapper",
                                     getLocation());
        }
        mapperElement = new Mapper(getProject());
        return mapperElement;
    }
    public void add(FileNameMapper fileNameMapper) {
        createMapper().add(fileNameMapper);
    }
    public boolean eval() {
        if (sourceFileSets.size() == 0 && sourceResources.size() == 0
            && sourceFile == null) {
            throw new BuildException("At least one srcfile or a nested "
                                     + "<srcfiles> or <srcresources> element "
                                     + "must be set.");
        }
        if ((sourceFileSets.size() > 0 || sourceResources.size() > 0)
            && sourceFile != null) {
            throw new BuildException("Cannot specify both the srcfile "
                                     + "attribute and a nested <srcfiles> "
                                     + "or <srcresources> element.");
        }
        if (targetFile == null && mapperElement == null) {
            throw new BuildException("The targetfile attribute or a nested "
                                     + "mapper element must be set.");
        }
        if (targetFile != null && !targetFile.exists()) {
            log("The targetfile \"" + targetFile.getAbsolutePath()
                    + "\" does not exist.", Project.MSG_VERBOSE);
            return false;
        }
        if (sourceFile != null && !sourceFile.exists()) {
            throw new BuildException(sourceFile.getAbsolutePath()
                                     + " not found.");
        }
        boolean upToDate = true;
        if (sourceFile != null) {
            if (mapperElement == null) {
                upToDate = targetFile.lastModified() >= sourceFile.lastModified();
            } else {
                SourceFileScanner sfs = new SourceFileScanner(this);
                upToDate = sfs.restrict(new String[] {sourceFile.getAbsolutePath()},
                                  null, null,
                                  mapperElement.getImplementation()).length == 0;
            }
            if (!upToDate) {
                log(sourceFile.getAbsolutePath()
                    + " is newer than (one of) its target(s).",
                    Project.MSG_VERBOSE);
            }
        }
        Enumeration e = sourceFileSets.elements();
        while (upToDate && e.hasMoreElements()) {
            FileSet fs = (FileSet) e.nextElement();
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            upToDate = scanDir(fs.getDir(getProject()),
                                           ds.getIncludedFiles());
        }
        if (upToDate) {
            Resource[] r = sourceResources.listResources();
            if (r.length > 0) {
                upToDate = ResourceUtils.selectOutOfDateSources(
                        this, r, getMapper(), getProject()).length == 0;
            }
        }
        return upToDate;
    }
    public void execute() throws BuildException {
        if (property == null) {
            throw new BuildException("property attribute is required.",
                                     getLocation());
        }
        boolean upToDate = eval();
        if (upToDate) {
            getProject().setNewProperty(property, getValue());
            if (mapperElement == null) {
                log("File \"" + targetFile.getAbsolutePath()
                    + "\" is up-to-date.", Project.MSG_VERBOSE);
            } else {
                log("All target files are up-to-date.",
                    Project.MSG_VERBOSE);
            }
        }
    }
    protected boolean scanDir(File srcDir, String[] files) {
        SourceFileScanner sfs = new SourceFileScanner(this);
        FileNameMapper mapper = getMapper();
        File dir = srcDir;
        if (mapperElement == null) {
            dir = null;
        }
        return sfs.restrict(files, srcDir, dir, mapper).length == 0;
    }
    private FileNameMapper getMapper() {
        FileNameMapper mapper = null;
        if (mapperElement == null) {
            MergingMapper mm = new MergingMapper();
            mm.setTo(targetFile.getAbsolutePath());
            mapper = mm;
        } else {
            mapper = mapperElement.getImplementation();
        }
        return mapper;
    }
}