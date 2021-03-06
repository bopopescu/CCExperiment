package org.apache.tools.tar;
import java.io.File;
import java.util.Date;
import java.util.Locale;
public class TarEntry implements TarConstants {
    private StringBuffer name;
    private int mode;
    private int userId;
    private int groupId;
    private long size;
    private long modTime;
    private byte linkFlag;
    private StringBuffer linkName;
    private StringBuffer magic;
    private StringBuffer userName;
    private StringBuffer groupName;
    private int devMajor;
    private int devMinor;
    private File file;
    public static final int MAX_NAMELEN = 31;
    public static final int DEFAULT_DIR_MODE = 040755;
    public static final int DEFAULT_FILE_MODE = 0100644;
    public static final int MILLIS_PER_SECOND = 1000;
    private TarEntry () {
        this.magic = new StringBuffer(TMAGIC);
        this.name = new StringBuffer();
        this.linkName = new StringBuffer();
        String user = System.getProperty("user.name", "");
        if (user.length() > MAX_NAMELEN) {
            user = user.substring(0, MAX_NAMELEN);
        }
        this.userId = 0;
        this.groupId = 0;
        this.userName = new StringBuffer(user);
        this.groupName = new StringBuffer("");
        this.file = null;
    }
    public TarEntry(String name) {
        this(name, false);
    }
    public TarEntry(String name, boolean preserveLeadingSlashes) {
        this();
        name = normalizeFileName(name, preserveLeadingSlashes);
        boolean isDir = name.endsWith("/");
        this.devMajor = 0;
        this.devMinor = 0;
        this.name = new StringBuffer(name);
        this.mode = isDir ? DEFAULT_DIR_MODE : DEFAULT_FILE_MODE;
        this.linkFlag = isDir ? LF_DIR : LF_NORMAL;
        this.userId = 0;
        this.groupId = 0;
        this.size = 0;
        this.modTime = (new Date()).getTime() / MILLIS_PER_SECOND;
        this.linkName = new StringBuffer("");
        this.userName = new StringBuffer("");
        this.groupName = new StringBuffer("");
        this.devMajor = 0;
        this.devMinor = 0;
    }
    public TarEntry(String name, byte linkFlag) {
        this(name);
        this.linkFlag = linkFlag;
        if (linkFlag == LF_GNUTYPE_LONGNAME) {
            magic = new StringBuffer(GNU_TMAGIC);
        }
    }
    public TarEntry(File file) {
        this();
        this.file = file;
        String fileName = normalizeFileName(file.getPath(), false);
        this.linkName = new StringBuffer("");
        this.name = new StringBuffer(fileName);
        if (file.isDirectory()) {
            this.mode = DEFAULT_DIR_MODE;
            this.linkFlag = LF_DIR;
            int nameLength = name.length();
            if (nameLength == 0 || name.charAt(nameLength - 1) != '/') {
                this.name.append("/");
            }
            this.size = 0;
        } else {
            this.mode = DEFAULT_FILE_MODE;
            this.linkFlag = LF_NORMAL;
            this.size = file.length();
        }
        this.modTime = file.lastModified() / MILLIS_PER_SECOND;
        this.devMajor = 0;
        this.devMinor = 0;
    }
    public TarEntry(byte[] headerBuf) {
        this();
        parseTarHeader(headerBuf);
    }
    public boolean equals(TarEntry it) {
        return getName().equals(it.getName());
    }
    public boolean equals(Object it) {
        if (it == null || getClass() != it.getClass()) {
            return false;
        }
        return equals((TarEntry) it);
    }
    public int hashCode() {
        return getName().hashCode();
    }
    public boolean isDescendent(TarEntry desc) {
        return desc.getName().startsWith(getName());
    }
    public String getName() {
        return name.toString();
    }
    public void setName(String name) {
        this.name = new StringBuffer(normalizeFileName(name, false));
    }
    public void setMode(int mode) {
        this.mode = mode;
    }
    public String getLinkName() {
        return linkName.toString();
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public int getGroupId() {
        return groupId;
    }
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
    public String getUserName() {
        return userName.toString();
    }
    public void setUserName(String userName) {
        this.userName = new StringBuffer(userName);
    }
    public String getGroupName() {
        return groupName.toString();
    }
    public void setGroupName(String groupName) {
        this.groupName = new StringBuffer(groupName);
    }
    public void setIds(int userId, int groupId) {
        setUserId(userId);
        setGroupId(groupId);
    }
    public void setNames(String userName, String groupName) {
        setUserName(userName);
        setGroupName(groupName);
    }
    public void setModTime(long time) {
        modTime = time / MILLIS_PER_SECOND;
    }
    public void setModTime(Date time) {
        modTime = time.getTime() / MILLIS_PER_SECOND;
    }
    public Date getModTime() {
        return new Date(modTime * MILLIS_PER_SECOND);
    }
    public File getFile() {
        return file;
    }
    public int getMode() {
        return mode;
    }
    public long getSize() {
        return size;
    }
    public void setSize(long size) {
        this.size = size;
    }
    public boolean isGNULongNameEntry() {
        return linkFlag == LF_GNUTYPE_LONGNAME
                           && name.toString().equals(GNU_LONGLINK);
    }
    public boolean isDirectory() {
        if (file != null) {
            return file.isDirectory();
        }
        if (linkFlag == LF_DIR) {
            return true;
        }
        if (getName().endsWith("/")) {
            return true;
        }
        return false;
    }
    public TarEntry[] getDirectoryEntries() {
        if (file == null || !file.isDirectory()) {
            return new TarEntry[0];
        }
        String[]   list = file.list();
        TarEntry[] result = new TarEntry[list.length];
        for (int i = 0; i < list.length; ++i) {
            result[i] = new TarEntry(new File(file, list[i]));
        }
        return result;
    }
    public void writeEntryHeader(byte[] outbuf) {
        int offset = 0;
        offset = TarUtils.getNameBytes(name, outbuf, offset, NAMELEN);
        offset = TarUtils.getOctalBytes(mode, outbuf, offset, MODELEN);
        offset = TarUtils.getOctalBytes(userId, outbuf, offset, UIDLEN);
        offset = TarUtils.getOctalBytes(groupId, outbuf, offset, GIDLEN);
        offset = TarUtils.getLongOctalBytes(size, outbuf, offset, SIZELEN);
        offset = TarUtils.getLongOctalBytes(modTime, outbuf, offset, MODTIMELEN);
        int csOffset = offset;
        for (int c = 0; c < CHKSUMLEN; ++c) {
            outbuf[offset++] = (byte) ' ';
        }
        outbuf[offset++] = linkFlag;
        offset = TarUtils.getNameBytes(linkName, outbuf, offset, NAMELEN);
        offset = TarUtils.getNameBytes(magic, outbuf, offset, MAGICLEN);
        offset = TarUtils.getNameBytes(userName, outbuf, offset, UNAMELEN);
        offset = TarUtils.getNameBytes(groupName, outbuf, offset, GNAMELEN);
        offset = TarUtils.getOctalBytes(devMajor, outbuf, offset, DEVLEN);
        offset = TarUtils.getOctalBytes(devMinor, outbuf, offset, DEVLEN);
        while (offset < outbuf.length) {
            outbuf[offset++] = 0;
        }
        long chk = TarUtils.computeCheckSum(outbuf);
        TarUtils.getCheckSumOctalBytes(chk, outbuf, csOffset, CHKSUMLEN);
    }
    public void parseTarHeader(byte[] header) {
        int offset = 0;
        name = TarUtils.parseName(header, offset, NAMELEN);
        offset += NAMELEN;
        mode = (int) TarUtils.parseOctal(header, offset, MODELEN);
        offset += MODELEN;
        userId = (int) TarUtils.parseOctal(header, offset, UIDLEN);
        offset += UIDLEN;
        groupId = (int) TarUtils.parseOctal(header, offset, GIDLEN);
        offset += GIDLEN;
        size = TarUtils.parseOctal(header, offset, SIZELEN);
        offset += SIZELEN;
        modTime = TarUtils.parseOctal(header, offset, MODTIMELEN);
        offset += MODTIMELEN;
        offset += CHKSUMLEN;
        linkFlag = header[offset++];
        linkName = TarUtils.parseName(header, offset, NAMELEN);
        offset += NAMELEN;
        magic = TarUtils.parseName(header, offset, MAGICLEN);
        offset += MAGICLEN;
        userName = TarUtils.parseName(header, offset, UNAMELEN);
        offset += UNAMELEN;
        groupName = TarUtils.parseName(header, offset, GNAMELEN);
        offset += GNAMELEN;
        devMajor = (int) TarUtils.parseOctal(header, offset, DEVLEN);
        offset += DEVLEN;
        devMinor = (int) TarUtils.parseOctal(header, offset, DEVLEN);
    }
    private static String normalizeFileName(String fileName,
                                            boolean preserveLeadingSlashes) {
        String osname = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (osname != null) {
            if (osname.startsWith("windows")) {
                if (fileName.length() > 2) {
                    char ch1 = fileName.charAt(0);
                    char ch2 = fileName.charAt(1);
                    if (ch2 == ':'
                        && ((ch1 >= 'a' && ch1 <= 'z')
                            || (ch1 >= 'A' && ch1 <= 'Z'))) {
                        fileName = fileName.substring(2);
                    }
                }
            } else if (osname.indexOf("netware") > -1) {
                int colon = fileName.indexOf(':');
                if (colon != -1) {
                    fileName = fileName.substring(colon + 1);
                }
            }
        }
        fileName = fileName.replace(File.separatorChar, '/');
        while (!preserveLeadingSlashes && fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }
        return fileName;
    }
}
