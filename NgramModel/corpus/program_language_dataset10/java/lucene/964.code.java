package org.apache.lucene.store.je;
import java.io.IOException;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
public class Block extends Object {
    protected DatabaseEntry key, data;
    protected Block(File file) throws IOException {
        byte[] fileKey = file.getKey();
        key = new DatabaseEntry(new byte[fileKey.length + 8]);
        data = new DatabaseEntry(new byte[JEIndexOutput.BLOCK_LEN]);
        System.arraycopy(fileKey, 0, key.getData(), 0, fileKey.length);
        seek(0L);
    }
    protected byte[] getKey() {
        return key.getData();
    }
    protected byte[] getData() {
        return data.getData();
    }
    protected void seek(long position) throws IOException {
        byte[] data = key.getData();
        int index = data.length - 8;
        position >>>= JEIndexOutput.BLOCK_SHIFT;
        data[index + 0] = (byte) (0xff & (position >>> 56));
        data[index + 1] = (byte) (0xff & (position >>> 48));
        data[index + 2] = (byte) (0xff & (position >>> 40));
        data[index + 3] = (byte) (0xff & (position >>> 32));
        data[index + 4] = (byte) (0xff & (position >>> 24));
        data[index + 5] = (byte) (0xff & (position >>> 16));
        data[index + 6] = (byte) (0xff & (position >>> 8));
        data[index + 7] = (byte) (0xff & (position >>> 0));
    }
    protected void get(JEDirectory directory) throws IOException {
        try {
            directory.blocks.get(directory.txn, key, data, null);
        } catch (DatabaseException e) {
            throw new IOException(e.getMessage());
        }
    }
    protected void put(JEDirectory directory) throws IOException {
        try {
            directory.blocks.put(directory.txn, key, data);
        } catch (DatabaseException e) {
            throw new IOException(e.getMessage());
        }
    }
}
