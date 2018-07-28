package org.apache.tools.zip;
import java.util.zip.ZipException;
public interface ZipExtraField {
    ZipShort getHeaderId();
    ZipShort getLocalFileDataLength();
    ZipShort getCentralDirectoryLength();
    byte[] getLocalFileDataData();
    byte[] getCentralDirectoryData();
    void parseFromLocalFileData(byte[] data, int offset, int length)
        throws ZipException;
}