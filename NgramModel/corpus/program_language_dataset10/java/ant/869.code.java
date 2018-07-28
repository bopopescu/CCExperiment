package org.apache.tools.zip;
import java.util.zip.ZipException;
public final class JarMarker implements ZipExtraField {
    private static final ZipShort ID = new ZipShort(0xCAFE);
    private static final ZipShort NULL = new ZipShort(0);
    private static final byte[] NO_BYTES = new byte[0];
    private static final JarMarker DEFAULT = new JarMarker();
    public JarMarker() {
    }
    public static JarMarker getInstance() {
        return DEFAULT;
    }
    public ZipShort getHeaderId() {
        return ID;
    }
    public ZipShort getLocalFileDataLength() {
        return NULL;
    }
    public ZipShort getCentralDirectoryLength() {
        return NULL;
    }
    public byte[] getLocalFileDataData() {
        return NO_BYTES;
    }
    public byte[] getCentralDirectoryData() {
        return NO_BYTES;
    }
    public void parseFromLocalFileData(byte[] data, int offset, int length)
        throws ZipException {
        if (length != 0) {
            throw new ZipException("JarMarker doesn't expect any data");
        }
    }
}