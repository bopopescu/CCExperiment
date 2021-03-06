package org.apache.batik.svggen.font.table;
import java.io.IOException;
import java.io.RandomAccessFile;
public class CmapTable implements Table {
    private int version;
    private int numTables;
    private CmapIndexEntry[] entries;
    private CmapFormat[] formats;
    protected CmapTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
        raf.seek(de.getOffset());
        long fp = raf.getFilePointer();
        version = raf.readUnsignedShort();
        numTables = raf.readUnsignedShort();
        entries = new CmapIndexEntry[numTables];
        formats = new CmapFormat[numTables];
        for (int i = 0; i < numTables; i++) {
            entries[i] = new CmapIndexEntry(raf);
        }
        for (int i = 0; i < numTables; i++) {
            raf.seek(fp + entries[i].getOffset());
            int format = raf.readUnsignedShort();
            formats[i] = CmapFormat.create(format, raf);
        }
    }
    public CmapFormat getCmapFormat(short platformId, short encodingId) {
        for (int i = 0; i < numTables; i++) {
            if (entries[i].getPlatformId() == platformId
                    && entries[i].getEncodingId() == encodingId) {
                return formats[i];
            }
        }
        return null;
    }
    public int getType() {
        return cmap;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer( numTables * 8 ).append("cmap\n");
        for (int i = 0; i < numTables; i++) {
            sb.append( '\t' ).append(entries[i].toString()).append( '\n' );
        }
        for (int i = 0; i < numTables; i++) {
            sb.append( '\t' ).append(formats[i].toString()).append( '\n' );
        }
        return sb.toString();
    }
}
