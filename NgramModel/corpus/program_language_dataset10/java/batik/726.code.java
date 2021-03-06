package org.apache.batik.ext.awt.color;
import org.apache.batik.util.SoftReferenceCache;
public class NamedProfileCache extends SoftReferenceCache {
    static NamedProfileCache theCache = new NamedProfileCache();
    public static NamedProfileCache getDefaultCache() { return theCache; }
    public NamedProfileCache() { }
    public synchronized boolean isPresent(String profileName) {
        return super.isPresentImpl(profileName);
    }
    public synchronized boolean isDone(String profileName) {
        return super.isDoneImpl(profileName);
    }
    public synchronized ICCColorSpaceExt request(String profileName) {
        return (ICCColorSpaceExt)super.requestImpl(profileName);
    }
    public synchronized void clear(String profileName) {
        super.clearImpl(profileName);
    }
    public synchronized void put(String profileName, ICCColorSpaceExt bi) {
        super.putImpl(profileName, bi);
    }
}
