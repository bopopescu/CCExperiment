package org.apache.tools.ant.taskdefs.optional.net;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Locale;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.LoaderUtils;
import org.apache.tools.ant.util.Retryable;
import org.apache.tools.ant.util.SplitClassLoader;
public class FTPTask extends Task implements FTPTaskConfig {
    public static final int SEND_FILES = 0;
    public static final int GET_FILES = 1;
    public static final int DEL_FILES = 2;
    public static final int LIST_FILES = 3;
    public static final int MK_DIR = 4;
    public static final int CHMOD = 5;
    public static final int RM_DIR = 6;
    public static final int SITE_CMD = 7;
    private static final long GRANULARITY_MINUTE = 60000L;
    public static final int DEFAULT_FTP_PORT = 21;
    private String remotedir;
    private String server;
    private String userid;
    private String password;
    private String account;
    private File listing;
    private boolean binary = true;
    private boolean passive = false;
    private boolean verbose = false;
    private boolean newerOnly = false;
    private long timeDiffMillis = 0;
    private long granularityMillis = 0L;
    private boolean timeDiffAuto = false;
    private int action = SEND_FILES;
    private Vector filesets = new Vector();
    private String remoteFileSep = "/";
    private int port = DEFAULT_FTP_PORT;
    private boolean skipFailedTransfers = false;
    private boolean ignoreNoncriticalErrors = false;
    private boolean preserveLastModified = false;
    private String chmod = null;
    private String umask = null;
    private FTPSystemType systemTypeKey = FTPSystemType.getDefault();
    private String defaultDateFormatConfig = null;
    private String recentDateFormatConfig = null;
    private String serverLanguageCodeConfig = null;
    private String serverTimeZoneConfig = null;
    private String shortMonthNamesConfig = null;
    private Granularity timestampGranularity = Granularity.getDefault();
    private boolean isConfigurationSet = false;
    private int retriesAllowed = 0;
    private String siteCommand = null;
    private String initialSiteCommand = null;
    private boolean enableRemoteVerification = true;
    private Path classpath;
    private ClassLoader mirrorLoader;
    private FTPTaskMirror delegate = null;
    public static final String[] ACTION_STRS = {
        "sending",
        "getting",
        "deleting",
        "listing",
        "making directory",
        "chmod",
        "removing",
        "site"
    };
    public static final String[] COMPLETED_ACTION_STRS = {
        "sent",
        "retrieved",
        "deleted",
        "listed",
        "created directory",
        "mode changed",
        "removed",
        "site command executed"
    };
    public static final String[] ACTION_TARGET_STRS = {
        "files",
        "files",
        "files",
        "files",
        "directory",
        "files",
        "directories",
        "site command"
    };
    public void setRemotedir(String dir) {
        this.remotedir = dir;
    }
    public String getRemotedir() {
        return remotedir;
    }
    public void setServer(String server) {
        this.server = server;
    }
    public String getServer() {
        return server;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public int getPort() {
        return port;
    }
    public void setUserid(String userid) {
        this.userid = userid;
    }
    public String getUserid() {
        return userid;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() {
        return password;
    }
    public void setAccount(String pAccount) {
        this.account = pAccount;
    }
    public String getAccount() {
        return account;
    }
    public void setBinary(boolean binary) {
        this.binary = binary;
    }
    public boolean isBinary() {
        return binary;
    }
    public void setPassive(boolean passive) {
        this.passive = passive;
    }
    public boolean isPassive() {
        return passive;
    }
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    public boolean isVerbose() {
        return verbose;
    }
    public void setNewer(boolean newer) {
        this.newerOnly = newer;
    }
    public boolean isNewer() {
        return newerOnly;
    }
    public void setTimeDiffMillis(long timeDiffMillis) {
        this.timeDiffMillis = timeDiffMillis;
    }
    public long getTimeDiffMillis() {
        return timeDiffMillis;
    }
    public void setTimeDiffAuto(boolean timeDiffAuto) {
        this.timeDiffAuto = timeDiffAuto;
    }
    public boolean isTimeDiffAuto() {
        return timeDiffAuto;
    }
    public void setPreserveLastModified(boolean preserveLastModified) {
        this.preserveLastModified = preserveLastModified;
    }
    public boolean isPreserveLastModified() {
        return preserveLastModified;
    }
    public void setDepends(boolean depends) {
        this.newerOnly = depends;
    }
    public void setSeparator(String separator) {
        remoteFileSep = separator;
    }
    public String getSeparator() {
        return remoteFileSep;
    }
    public void setChmod(String theMode) {
        this.chmod = theMode;
    }
    public String getChmod() {
        return chmod;
    }
    public void setUmask(String theUmask) {
        this.umask = theUmask;
    }
    public String getUmask() {
        return umask;
    }
    public void addFileset(FileSet set) {
        filesets.addElement(set);
    }
    public Vector getFilesets() {
        return filesets;
    }
    public void setAction(String action) throws BuildException {
        log("DEPRECATED - The setAction(String) method has been deprecated."
            + " Use setAction(FTP.Action) instead.");
        Action a = new Action();
        a.setValue(action);
        this.action = a.getAction();
    }
    public void setAction(Action action) throws BuildException {
        this.action = action.getAction();
    }
    public int getAction() {
        return this.action;
    }
    public void setListing(File listing) {
        this.listing = listing;
    }
    public File getListing() {
        return listing;
    }
    public void setSkipFailedTransfers(boolean skipFailedTransfers) {
        this.skipFailedTransfers = skipFailedTransfers;
    }
    public boolean isSkipFailedTransfers() {
        return skipFailedTransfers;
    }
    public void setIgnoreNoncriticalErrors(boolean ignoreNoncriticalErrors) {
        this.ignoreNoncriticalErrors = ignoreNoncriticalErrors;
    }
    public boolean isIgnoreNoncriticalErrors() {
        return ignoreNoncriticalErrors;
    }
    private void configurationHasBeenSet() {
        this.isConfigurationSet = true;
    }
    public boolean isConfigurationSet() {
        return this.isConfigurationSet;
    }
    public void setSystemTypeKey(FTPSystemType systemKey) {
        if (systemKey != null && !systemKey.getValue().equals("")) {
            this.systemTypeKey = systemKey;
            configurationHasBeenSet();
        }
    }
    public void setDefaultDateFormatConfig(String defaultDateFormat) {
        if (defaultDateFormat != null && !defaultDateFormat.equals("")) {
            this.defaultDateFormatConfig = defaultDateFormat;
            configurationHasBeenSet();
        }
    }
    public void setRecentDateFormatConfig(String recentDateFormat) {
        if (recentDateFormat != null && !recentDateFormat.equals("")) {
            this.recentDateFormatConfig = recentDateFormat;
            configurationHasBeenSet();
        }
    }
    public void setServerLanguageCodeConfig(String serverLanguageCode) {
        if (serverLanguageCode != null && !"".equals(serverLanguageCode)) {
            this.serverLanguageCodeConfig = serverLanguageCode;
            configurationHasBeenSet();
        }
    }
    public void setServerTimeZoneConfig(String serverTimeZoneId) {
        if (serverTimeZoneId != null && !serverTimeZoneId.equals("")) {
            this.serverTimeZoneConfig = serverTimeZoneId;
            configurationHasBeenSet();
        }
    }
    public void setShortMonthNamesConfig(String shortMonthNames) {
        if (shortMonthNames != null && !shortMonthNames.equals("")) {
            this.shortMonthNamesConfig = shortMonthNames;
            configurationHasBeenSet();
        }
    }
    public void setRetriesAllowed(String retriesAllowed) {
        if ("FOREVER".equalsIgnoreCase(retriesAllowed)) {
            this.retriesAllowed = Retryable.RETRY_FOREVER;
        } else {
            try {
                int retries = Integer.parseInt(retriesAllowed);
                if (retries < Retryable.RETRY_FOREVER) {
                    throw new BuildException(
                                             "Invalid value for retriesAllowed attribute: "
                                             + retriesAllowed);
                }
                this.retriesAllowed = retries;
            } catch (NumberFormatException px) {
                throw new BuildException(
                                         "Invalid value for retriesAllowed attribute: "
                                         + retriesAllowed);
            }
        }
    }
    public int getRetriesAllowed() {
        return retriesAllowed;
    }
    public String getSystemTypeKey() {
        return systemTypeKey.getValue();
    }
    public String getDefaultDateFormatConfig() {
        return defaultDateFormatConfig;
    }
    public String getRecentDateFormatConfig() {
        return recentDateFormatConfig;
    }
    public String getServerLanguageCodeConfig() {
        return serverLanguageCodeConfig;
    }
    public String getServerTimeZoneConfig() {
        return serverTimeZoneConfig;
    }
    public String getShortMonthNamesConfig() {
        return shortMonthNamesConfig;
    }
    public Granularity getTimestampGranularity() {
        return timestampGranularity;
    }
    public void setTimestampGranularity(Granularity timestampGranularity) {
        if (null == timestampGranularity || "".equals(timestampGranularity.getValue())) {
            return;
        }
        this.timestampGranularity = timestampGranularity;
    }
    public void setSiteCommand(String siteCommand) {
        this.siteCommand = siteCommand;
    }
    public String getSiteCommand() {
        return siteCommand;
    }
    public void setInitialSiteCommand(String initialCommand) {
        this.initialSiteCommand = initialCommand;
    }
    public String getInitialSiteCommand() {
        return initialSiteCommand;
    }
    public long getGranularityMillis() {
        return this.granularityMillis;
    }
    public void setGranularityMillis(long granularity) {
        this.granularityMillis = granularity;
    }
    public void setEnableRemoteVerification(boolean b) {
        enableRemoteVerification = b;
    }
    public boolean getEnableRemoteVerification() {
        return enableRemoteVerification;
    }
    protected void checkAttributes() throws BuildException {
        if (server == null) {
            throw new BuildException("server attribute must be set!");
        }
        if (userid == null) {
            throw new BuildException("userid attribute must be set!");
        }
        if (password == null) {
            throw new BuildException("password attribute must be set!");
        }
        if ((action == LIST_FILES) && (listing == null)) {
            throw new BuildException("listing attribute must be set for list "
                                     + "action!");
        }
        if (action == MK_DIR && remotedir == null) {
            throw new BuildException("remotedir attribute must be set for "
                                     + "mkdir action!");
        }
        if (action == CHMOD && chmod == null) {
            throw new BuildException("chmod attribute must be set for chmod "
                                     + "action!");
        }
        if (action == SITE_CMD && siteCommand == null) {
            throw new BuildException("sitecommand attribute must be set for site "
                                     + "action!");
        }
        if (this.isConfigurationSet) {
            try {
                Class.forName("org.apache.commons.net.ftp.FTPClientConfig");
            } catch (ClassNotFoundException e) {
                throw new BuildException(
                                         "commons-net.jar >= 1.4.0 is required for at least one"
                                         + " of the attributes specified.");
            }
        }
    }
    public void execute() throws BuildException {
        checkAttributes();
        try {
            setupFTPDelegate();
            delegate.doFTP();
        } finally {
            if (mirrorLoader instanceof SplitClassLoader) {
                ((SplitClassLoader) mirrorLoader).cleanup();
            }
            mirrorLoader = null;
        }
    }
    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(getProject());
        }
        return classpath;
    }
    protected void setupFTPDelegate() {
        ClassLoader myLoader = FTPTask.class.getClassLoader();
        if (mustSplit()) {
            mirrorLoader =
                new SplitClassLoader(myLoader, classpath, getProject(),
                                     new String[] {
                                         "FTPTaskMirrorImpl",
                                         "FTPConfigurator"
                                     });
        } else {
            mirrorLoader = myLoader;
        }
        delegate = createMirror(this, mirrorLoader);
    }
    private static boolean mustSplit() {
        return LoaderUtils.getResourceSource(FTPTask.class.getClassLoader(),
                                             "/org/apache/commons/net/"
                                             + "ftp/FTP.class")
            == null;
    }
    private static FTPTaskMirror createMirror(FTPTask task,
                                              ClassLoader loader) {
        try {
            loader.loadClass("org.apache.commons.net.ftp.FTP"); 
        } catch (ClassNotFoundException e) {
            throw new BuildException("The <classpath> for <ftp> must include"
                                     + " commons-net.jar if not in Ant's own "
                                     + " classpath", e, task.getLocation());
        }
        try {
            Class c = loader.loadClass(FTPTaskMirror.class.getName() + "Impl");
            if (c.getClassLoader() != loader) {
                throw new BuildException("Overdelegating loader",
                                         task.getLocation());
            }
            Constructor cons = c.getConstructor(new Class[] {FTPTask.class});
            return (FTPTaskMirror) cons.newInstance(new Object[] {task});
        } catch (Exception e) {
            throw new BuildException(e, task.getLocation());
        }
    }
    public static class Action extends EnumeratedAttribute {
        private static final String[] VALID_ACTIONS = {
            "send", "put", "recv", "get", "del", "delete", "list", "mkdir",
            "chmod", "rmdir", "site"
        };
        public String[] getValues() {
            return VALID_ACTIONS;
        }
        public int getAction() {
            String actionL = getValue().toLowerCase(Locale.ENGLISH);
            if (actionL.equals("send") || actionL.equals("put")) {
                return SEND_FILES;
            } else if (actionL.equals("recv") || actionL.equals("get")) {
                return GET_FILES;
            } else if (actionL.equals("del") || actionL.equals("delete")) {
                return DEL_FILES;
            } else if (actionL.equals("list")) {
                return LIST_FILES;
            } else if (actionL.equals("chmod")) {
                return CHMOD;
            } else if (actionL.equals("mkdir")) {
                return MK_DIR;
            } else if (actionL.equals("rmdir")) {
                return RM_DIR;
            } else if (actionL.equals("site")) {
                return SITE_CMD;
            }
            return SEND_FILES;
        }
    }
    public static class Granularity extends EnumeratedAttribute {
        private static final String[] VALID_GRANULARITIES = {
            "", "MINUTE", "NONE"
        };
        public String[] getValues() {
            return VALID_GRANULARITIES;
        }
        public long getMilliseconds(int action) {
            String granularityU = getValue().toUpperCase(Locale.ENGLISH);
            if ("".equals(granularityU)) {
                if (action == SEND_FILES) {
                    return GRANULARITY_MINUTE;
                }
            } else if ("MINUTE".equals(granularityU)) {
                return GRANULARITY_MINUTE;
            }
            return 0L;
        }
        static final Granularity getDefault() {
            Granularity g = new Granularity();
            g.setValue("");
            return g;
        }
    }
    public static class FTPSystemType extends EnumeratedAttribute {
        private static final String[] VALID_SYSTEM_TYPES = {
            "", "UNIX", "VMS", "WINDOWS", "OS/2", "OS/400",
            "MVS"
        };
        public String[] getValues() {
            return VALID_SYSTEM_TYPES;
        }
        static final FTPSystemType getDefault() {
            FTPSystemType ftpst = new FTPSystemType();
            ftpst.setValue("");
            return ftpst;
        }
    }
}
