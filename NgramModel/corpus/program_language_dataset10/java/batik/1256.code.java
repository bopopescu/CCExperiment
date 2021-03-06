package org.apache.batik.svggen.font.table;
import java.io.IOException;
import java.io.RandomAccessFile;
public class LangSysRecord {
    private int tag;
    private int offset;
    public LangSysRecord(RandomAccessFile raf) throws IOException {
        tag = raf.readInt();
        offset = raf.readUnsignedShort();
    }
    public int getTag() {
        return tag;
    }
    public int getOffset() {
        return offset;
    }
}
