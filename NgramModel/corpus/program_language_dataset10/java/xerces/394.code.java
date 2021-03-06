package org.apache.xerces.impl.io;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
public final class Latin1Reader 
    extends Reader {
    public static final int DEFAULT_BUFFER_SIZE = 2048;
    protected final InputStream fInputStream;
    protected final byte[] fBuffer;
    public Latin1Reader(InputStream inputStream) {
        this(inputStream, DEFAULT_BUFFER_SIZE);
    } 
    public Latin1Reader(InputStream inputStream, int size) {
        this(inputStream, new byte[size]);
    } 
    public Latin1Reader(InputStream inputStream, byte [] buffer) {
        fInputStream = inputStream;
        fBuffer = buffer;
    } 
    public int read() throws IOException {
        return fInputStream.read();
    } 
    public int read(char ch[], int offset, int length) throws IOException {
        if (length > fBuffer.length) {
            length = fBuffer.length;
        }
        int count = fInputStream.read(fBuffer, 0, length);
        for (int i = 0; i < count; ++i) {
            ch[offset + i] = (char) (fBuffer[i] & 0xff);
        }
        return count;
    } 
    public long skip(long n) throws IOException {
        return fInputStream.skip(n);
    } 
    public boolean ready() throws IOException {
        return false;
    } 
    public boolean markSupported() {
        return fInputStream.markSupported();
    } 
    public void mark(int readAheadLimit) throws IOException {
        fInputStream.mark(readAheadLimit);
    } 
    public void reset() throws IOException {
        fInputStream.reset();
    } 
     public void close() throws IOException {
         fInputStream.close();
     } 
} 
