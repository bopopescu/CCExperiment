package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.Touchable;
import org.apache.tools.ant.types.resources.Union;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.FileNameMapper;
public class Touch extends Task {
    public interface DateFormatFactory {
        DateFormat getPrimaryFormat();
        DateFormat getFallbackFormat();
    }
    public static final DateFormatFactory DEFAULT_DF_FACTORY
        = new DateFormatFactory() {
        public DateFormat getPrimaryFormat() {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.SHORT, Locale.US);
        }
        public DateFormat getFallbackFormat() {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT,
                DateFormat.MEDIUM, Locale.US);
        }
    };
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private File file;
    private long millis = -1;
    private String dateTime;
    private Vector filesets = new Vector();
    private Union resources;
    private boolean dateTimeConfigured;
    private boolean mkdirs;
    private boolean verbose = true;
    private FileNameMapper fileNameMapper = null;
    private DateFormatFactory dfFactory = DEFAULT_DF_FACTORY;
    public Touch() {
    }
    public void setFile(File file) {
        this.file = file;
    }
    public void setMillis(long millis) {
        this.millis = millis;
    }
    public void setDatetime(String dateTime) {
        if (this.dateTime != null) {
            log("Resetting datetime attribute to " + dateTime, Project.MSG_VERBOSE);
        }
        this.dateTime = dateTime;
        dateTimeConfigured = false;
    }
    public void setMkdirs(boolean mkdirs) {
        this.mkdirs = mkdirs;
    }
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    public void setPattern(final String pattern) {
        dfFactory = new DateFormatFactory() {
            public DateFormat getPrimaryFormat() {
                return new SimpleDateFormat(pattern);
            }
            public DateFormat getFallbackFormat() {
                return null;
            }
        };
    }
    public void addConfiguredMapper(Mapper mapper) {
        add(mapper.getImplementation());
    }
    public void add(FileNameMapper fileNameMapper) throws BuildException {
        if (this.fileNameMapper != null) {
            throw new BuildException("Only one mapper may be added to the "
                + getTaskName() + " task.");
        }
        this.fileNameMapper = fileNameMapper;
    }
    public void addFileset(FileSet set) {
        filesets.add(set);
        add(set);
    }
    public void addFilelist(FileList list) {
        add(list);
    }
    public synchronized void add(ResourceCollection rc) {
        resources = resources == null ? new Union() : resources;
        resources.add(rc);
    }
    protected synchronized void checkConfiguration() throws BuildException {
        if (file == null && resources == null) {
            throw new BuildException("Specify at least one source"
                                   + "--a file or resource collection.");
        }
        if (file != null && file.exists() && file.isDirectory()) {
            throw new BuildException("Use a resource collection to touch directories.");
        }
        if (dateTime != null && !dateTimeConfigured) {
            long workmillis = millis;
            if ("now".equalsIgnoreCase(dateTime)) {
                workmillis = System.currentTimeMillis();
            } else {
                DateFormat df = dfFactory.getPrimaryFormat();
                ParseException pe = null;
                try {
                    workmillis = df.parse(dateTime).getTime();
                } catch (ParseException peOne) {
                    df = dfFactory.getFallbackFormat();
                    if (df == null) {
                        pe = peOne;
                    } else {
                        try {
                            workmillis = df.parse(dateTime).getTime();
                        } catch (ParseException peTwo) {
                            pe = peTwo;
                        }
                    }
                }
                if (pe != null) {
                    throw new BuildException(pe.getMessage(), pe, getLocation());
                }
                if (workmillis < 0) {
                    throw new BuildException("Date of " + dateTime
                            + " results in negative " + "milliseconds value "
                            + "relative to epoch " + "(January 1, 1970, "
                            + "00:00:00 GMT).");
                }
            }
            log("Setting millis to " + workmillis + " from datetime attribute",
                    ((millis < 0) ? Project.MSG_DEBUG : Project.MSG_VERBOSE));
            setMillis(workmillis);
            dateTimeConfigured = true;
        }
    }
    public void execute() throws BuildException {
        checkConfiguration();
        touch();
    }
    protected void touch() throws BuildException {
        long defaultTimestamp = getTimestamp();
        if (file != null) {
            touch(new FileResource(file.getParentFile(), file.getName()),
                  defaultTimestamp);
        }
        if (resources == null) {
            return;
        }
        Iterator iter = resources.iterator();
        while (iter.hasNext()) {
            Resource r = (Resource) iter.next();
            Touchable t = (Touchable) r.as(Touchable.class);
            if (t == null) {
                throw new BuildException("Can't touch " + r);
            }
            touch(r, defaultTimestamp);
        }
        for (int i = 0; i < filesets.size(); i++) {
            FileSet fs = (FileSet) filesets.elementAt(i);
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File fromDir = fs.getDir(getProject());
            String[] srcDirs = ds.getIncludedDirectories();
            for (int j = 0; j < srcDirs.length; j++) {
                touch(new FileResource(fromDir, srcDirs[j]), defaultTimestamp);
            }
        }
    }
    protected void touch(File file) {
        touch(file, getTimestamp());
    }
    private long getTimestamp() {
        return (millis < 0) ? System.currentTimeMillis() : millis;
    }
    private void touch(Resource r, long defaultTimestamp) {
        if (fileNameMapper == null) {
            FileProvider fp = (FileProvider) r.as(FileProvider.class);
            if (fp != null) {
                touch(fp.getFile(), defaultTimestamp);
            } else {
                ((Touchable) r.as(Touchable.class)).touch(defaultTimestamp);
            }
        } else {
            String[] mapped = fileNameMapper.mapFileName(r.getName());
            if (mapped != null && mapped.length > 0) {
                long modTime = defaultTimestamp;
                if (millis < 0 && r.isExists()) {
                    modTime = r.getLastModified();
                }
                for (int i = 0; i < mapped.length; i++) {
                    touch(getProject().resolveFile(mapped[i]), modTime);
                }
            }
        }
    }
    private void touch(File file, long modTime) {
        if (!file.exists()) {
            log("Creating " + file,
                ((verbose) ? Project.MSG_INFO : Project.MSG_VERBOSE));
            try {
                FILE_UTILS.createNewFile(file, mkdirs);
            } catch (IOException ioe) {
                throw new BuildException("Could not create " + file, ioe,
                                         getLocation());
            }
        }
        if (!file.canWrite()) {
            throw new BuildException("Can not change modification date of "
                                     + "read-only file " + file);
        }
        FILE_UTILS.setFileLastModified(file, modTime);
    }
}