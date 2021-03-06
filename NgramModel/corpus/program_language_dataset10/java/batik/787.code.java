package org.apache.batik.ext.awt.image.codec.tiff;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.apache.batik.ext.awt.image.codec.util.SeekableStream;
public class TIFFDirectory implements Serializable {
    boolean isBigEndian;
    int numEntries;
    TIFFField[] fields;
    Map fieldIndex = new HashMap();
    long IFDOffset = 8;
    long nextIFDOffset = 0;
    TIFFDirectory() {}
    private static boolean isValidEndianTag(int endian) {
        return ((endian == 0x4949) || (endian == 0x4d4d));
    }
    public TIFFDirectory(SeekableStream stream, int directory)
        throws IOException {
        long global_save_offset = stream.getFilePointer();
        long ifd_offset;
        stream.seek(0L);
        int endian = stream.readUnsignedShort();
        if (!isValidEndianTag(endian)) {
            throw new
                IllegalArgumentException("TIFFDirectory1");
        }
        isBigEndian = (endian == 0x4d4d);
        int magic = readUnsignedShort(stream);
        if (magic != 42) {
            throw new
                IllegalArgumentException("TIFFDirectory2");
        }
        ifd_offset = readUnsignedInt(stream);
        for (int i = 0; i < directory; i++) {
            if (ifd_offset == 0L) {
                throw new
                   IllegalArgumentException("TIFFDirectory3");
            }
            stream.seek(ifd_offset);
            long entries = readUnsignedShort(stream);
            stream.skip(12*entries);
            ifd_offset = readUnsignedInt(stream);
        }
        stream.seek(ifd_offset);
        initialize(stream);
        stream.seek(global_save_offset);
    }
    public TIFFDirectory(SeekableStream stream, long ifd_offset, int directory)
        throws IOException {
        long global_save_offset = stream.getFilePointer();
        stream.seek(0L);
        int endian = stream.readUnsignedShort();
        if (!isValidEndianTag(endian)) {
            throw new
                IllegalArgumentException("TIFFDirectory1");
        }
        isBigEndian = (endian == 0x4d4d);
        stream.seek(ifd_offset);
        int dirNum = 0;
        while(dirNum < directory) {
            long numEntries = readUnsignedShort(stream);
            stream.seek(ifd_offset + 12*numEntries);
            ifd_offset = readUnsignedInt(stream);
            stream.seek(ifd_offset);
            dirNum++;
        }
        initialize(stream);
        stream.seek(global_save_offset);
    }
    private static final int[] sizeOfType = {
        0, 
        1, 
        1, 
        2, 
        4, 
        8, 
        1, 
        1, 
        2, 
        4, 
        8, 
        4, 
        8  
    };
    private void initialize(SeekableStream stream) throws IOException {
        long nextTagOffset;
        int i, j;
        IFDOffset = stream.getFilePointer();
        numEntries = readUnsignedShort(stream);
        fields = new TIFFField[numEntries];
        for (i = 0; i < numEntries; i++) {
            int tag = readUnsignedShort(stream);
            int type = readUnsignedShort(stream);
            int count = (int)(readUnsignedInt(stream));
            int value = 0;
            nextTagOffset = stream.getFilePointer() + 4;
            try {
                if (count*sizeOfType[type] > 4) {
                    value = (int)(readUnsignedInt(stream));
                    stream.seek(value);
                }
            } catch (ArrayIndexOutOfBoundsException ae) {
                System.err.println(tag + " " + "TIFFDirectory4");
                stream.seek(nextTagOffset);
                continue;
            }
            fieldIndex.put(new Integer(tag), new Integer(i));
            Object obj = null;
            switch (type) {
            case TIFFField.TIFF_BYTE:
            case TIFFField.TIFF_SBYTE:
            case TIFFField.TIFF_UNDEFINED:
            case TIFFField.TIFF_ASCII:
                byte[] bvalues = new byte[count];
                stream.readFully(bvalues, 0, count);
                if (type == TIFFField.TIFF_ASCII) {
                    int index = 0, prevIndex = 0;
                    List v = new ArrayList();
                    while (index < count) {
                        while ((index < count) && (bvalues[index++] != 0));
                        v.add(new String(bvalues, prevIndex,
                                         (index - prevIndex)) );
                        prevIndex = index;
                    }
                    count = v.size();
                    String[] strings = new String[count];
                    v.toArray( strings );
                    obj = strings;
                } else {
                    obj = bvalues;
                }
                break;
            case TIFFField.TIFF_SHORT:
                char[] cvalues = new char[count];
                for (j = 0; j < count; j++) {
                    cvalues[j] = (char)(readUnsignedShort(stream));
                }
                obj = cvalues;
                break;
            case TIFFField.TIFF_LONG:
                long[] lvalues = new long[count];
                for (j = 0; j < count; j++) {
                    lvalues[j] = readUnsignedInt(stream);
                }
                obj = lvalues;
                break;
            case TIFFField.TIFF_RATIONAL:
                long[][] llvalues = new long[count][2];
                for (j = 0; j < count; j++) {
                    llvalues[j][0] = readUnsignedInt(stream);
                    llvalues[j][1] = readUnsignedInt(stream);
                }
                obj = llvalues;
                break;
            case TIFFField.TIFF_SSHORT:
                short[] svalues = new short[count];
                for (j = 0; j < count; j++) {
                    svalues[j] = readShort(stream);
                }
                obj = svalues;
                break;
            case TIFFField.TIFF_SLONG:
                int[] ivalues = new int[count];
                for (j = 0; j < count; j++) {
                    ivalues[j] = readInt(stream);
                }
                obj = ivalues;
                break;
            case TIFFField.TIFF_SRATIONAL:
                int[][] iivalues = new int[count][2];
                for (j = 0; j < count; j++) {
                    iivalues[j][0] = readInt(stream);
                    iivalues[j][1] = readInt(stream);
                }
                obj = iivalues;
                break;
            case TIFFField.TIFF_FLOAT:
                float[] fvalues = new float[count];
                for (j = 0; j < count; j++) {
                    fvalues[j] = readFloat(stream);
                }
                obj = fvalues;
                break;
            case TIFFField.TIFF_DOUBLE:
                double[] dvalues = new double[count];
                for (j = 0; j < count; j++) {
                    dvalues[j] = readDouble(stream);
                }
                obj = dvalues;
                break;
            default:
                System.err.println("TIFFDirectory0");
                break;
            }
            fields[i] = new TIFFField(tag, type, count, obj);
            stream.seek(nextTagOffset);
        }
        nextIFDOffset = readUnsignedInt(stream);
    }
    public int getNumEntries() {
        return numEntries;
    }
    public TIFFField getField(int tag) {
        Integer i = (Integer)fieldIndex.get(new Integer(tag));
        if (i == null) {
            return null;
        } else {
            return fields[i.intValue()];
        }
    }
    public boolean isTagPresent(int tag) {
        return fieldIndex.containsKey(new Integer(tag));
    }
    public int[] getTags() {
        int[] tags = new int[fieldIndex.size()];
        Iterator iter = fieldIndex.keySet().iterator();
        int i = 0;
        while (iter.hasNext()) {
            tags[i++] = ((Integer)iter.next()).intValue();
        }
        return tags;
    }
    public TIFFField[] getFields() {
        return fields;
    }
    public byte getFieldAsByte(int tag, int index) {
        Integer i = (Integer)fieldIndex.get(new Integer(tag));
        byte [] b = (fields[i.intValue()]).getAsBytes();
        return b[index];
    }
    public byte getFieldAsByte(int tag) {
        return getFieldAsByte(tag, 0);
    }
    public long getFieldAsLong(int tag, int index) {
        Integer i = (Integer)fieldIndex.get(new Integer(tag));
        return (fields[i.intValue()]).getAsLong(index);
    }
    public long getFieldAsLong(int tag) {
        return getFieldAsLong(tag, 0);
    }
    public float getFieldAsFloat(int tag, int index) {
        Integer i = (Integer)fieldIndex.get(new Integer(tag));
        return fields[i.intValue()].getAsFloat(index);
    }
    public float getFieldAsFloat(int tag) {
        return getFieldAsFloat(tag, 0);
    }
    public double getFieldAsDouble(int tag, int index) {
        Integer i = (Integer)fieldIndex.get(new Integer(tag));
        return fields[i.intValue()].getAsDouble(index);
    }
    public double getFieldAsDouble(int tag) {
        return getFieldAsDouble(tag, 0);
    }
    private short readShort(SeekableStream stream)
        throws IOException {
        if (isBigEndian) {
            return stream.readShort();
        } else {
            return stream.readShortLE();
        }
    }
    private int readUnsignedShort(SeekableStream stream)
        throws IOException {
        if (isBigEndian) {
            return stream.readUnsignedShort();
        } else {
            return stream.readUnsignedShortLE();
        }
    }
    private int readInt(SeekableStream stream)
        throws IOException {
        if (isBigEndian) {
            return stream.readInt();
        } else {
            return stream.readIntLE();
        }
    }
    private long readUnsignedInt(SeekableStream stream)
        throws IOException {
        if (isBigEndian) {
            return stream.readUnsignedInt();
        } else {
            return stream.readUnsignedIntLE();
        }
    }
    private long readLong(SeekableStream stream)
        throws IOException {
        if (isBigEndian) {
            return stream.readLong();
        } else {
            return stream.readLongLE();
        }
    }
    private float readFloat(SeekableStream stream)
        throws IOException {
        if (isBigEndian) {
            return stream.readFloat();
        } else {
            return stream.readFloatLE();
        }
    }
    private double readDouble(SeekableStream stream)
        throws IOException {
        if (isBigEndian) {
            return stream.readDouble();
        } else {
            return stream.readDoubleLE();
        }
    }
    private static int readUnsignedShort(SeekableStream stream,
                                         boolean isBigEndian)
        throws IOException {
        if (isBigEndian) {
            return stream.readUnsignedShort();
        } else {
            return stream.readUnsignedShortLE();
        }
    }
    private static long readUnsignedInt(SeekableStream stream,
                                        boolean isBigEndian)
        throws IOException {
        if (isBigEndian) {
            return stream.readUnsignedInt();
        } else {
            return stream.readUnsignedIntLE();
        }
    }
    public static int getNumDirectories(SeekableStream stream)
        throws IOException{
        long pointer = stream.getFilePointer(); 
        stream.seek(0L);
        int endian = stream.readUnsignedShort();
        if (!isValidEndianTag(endian)) {
            throw new
                IllegalArgumentException("TIFFDirectory1");
        }
        boolean isBigEndian = (endian == 0x4d4d);
        int magic = readUnsignedShort(stream, isBigEndian);
        if (magic != 42) {
            throw new
                IllegalArgumentException("TIFFDirectory2");
        }
        stream.seek(4L);
        long offset = readUnsignedInt(stream, isBigEndian);
        int numDirectories = 0;
        while (offset != 0L) {
            ++numDirectories;
            stream.seek(offset);
            long entries = readUnsignedShort(stream, isBigEndian);
            stream.skip(12*entries);
            offset = readUnsignedInt(stream, isBigEndian);
        }
        stream.seek(pointer); 
        return numDirectories;
    }
    public boolean isBigEndian() {
        return isBigEndian;
    }
    public long getIFDOffset() {
        return IFDOffset;
    }
    public long getNextIFDOffset() {
        return nextIFDOffset;
    }
}
