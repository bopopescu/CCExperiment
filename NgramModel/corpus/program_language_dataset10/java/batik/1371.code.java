package org.apache.batik.util;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
public class Base64EncoderStream extends OutputStream {
    private static final byte[] pem_array = {
        'A','B','C','D','E','F','G','H', 
        'I','J','K','L','M','N','O','P', 
        'Q','R','S','T','U','V','W','X', 
        'Y','Z','a','b','c','d','e','f', 
        'g','h','i','j','k','l','m','n', 
        'o','p','q','r','s','t','u','v', 
        'w','x','y','z','0','1','2','3', 
        '4','5','6','7','8','9','+','/'  
    };
    byte [] atom = new byte[3];
    int     atomLen = 0;
    byte [] encodeBuf = new byte[4];
    int     lineLen = 0;
    PrintStream  out;
    boolean closeOutOnClose;
    public Base64EncoderStream(OutputStream out) {
        this.out = new PrintStream(out);
        closeOutOnClose = true;
    }
    public Base64EncoderStream(OutputStream out, boolean closeOutOnClose) {
        this.out = new PrintStream(out);
        this.closeOutOnClose = closeOutOnClose;
    }
    public void close () throws IOException {
        if (out != null) {
            encodeAtom();
            out.flush();
            if (closeOutOnClose)
                out.close();
            out=null;
        }
    }
    public void flush() throws IOException {
        out.flush();
    }
    public void write(int b) throws IOException {
        atom[atomLen++] = (byte)b;
        if (atomLen == 3)
            encodeAtom();
    }
    public void write(byte []data) throws IOException {
        encodeFromArray(data, 0, data.length);
    }
    public void write(byte [] data, int off, int len) throws IOException {
        encodeFromArray(data, off, len);
    }
    void encodeAtom() throws IOException {
        byte a, b, c;
        switch (atomLen) {
        case 0: return;
        case 1:
            a = atom[0];
            encodeBuf[0] = pem_array[((a >>> 2) & 0x3F)];
            encodeBuf[1] = pem_array[((a <<  4) & 0x30)];
            encodeBuf[2] = encodeBuf[3] = '=';
            break;
        case 2:
            a = atom[0];
            b = atom[1];
            encodeBuf[0] = pem_array[((a >>> 2) & 0x3F)];
            encodeBuf[1] = pem_array[(((a << 4) & 0x30) | ((b >>> 4) & 0x0F))];
            encodeBuf[2] = pem_array[((b  << 2) & 0x3C)];
            encodeBuf[3] = '=';
            break;
        default:
            a = atom[0];
            b = atom[1];
            c = atom[2];
            encodeBuf[0] = pem_array[((a >>> 2) & 0x3F)];
            encodeBuf[1] = pem_array[(((a << 4) & 0x30) | ((b >>> 4) & 0x0F))];
            encodeBuf[2] = pem_array[(((b << 2) & 0x3C) | ((c >>> 6) & 0x03))];
            encodeBuf[3] = pem_array[c & 0x3F];
        }
        if (lineLen == 64) {
            out.println();
            lineLen = 0;
        }
        out.write(encodeBuf);
        lineLen += 4;
        atomLen = 0;
    }
    void encodeFromArray(byte[] data, int offset, int len)
        throws IOException{
        byte a, b, c;
        if (len == 0)
            return;
        if (atomLen != 0) {
            switch(atomLen) {
            case 1:
                atom[1] = data[offset++]; len--; atomLen++;
                if (len == 0) return;
                atom[2] = data[offset++]; len--; atomLen++;
                break;
            case 2:
                atom[2] = data[offset++]; len--; atomLen++;
                break;
            default:
            }
            encodeAtom();
        }
        while (len >=3) {
            a = data[offset++];
            b = data[offset++];
            c = data[offset++];
            encodeBuf[0] = pem_array[((a >>> 2) & 0x3F)];
            encodeBuf[1] = pem_array[(((a << 4) & 0x30) | ((b >>> 4) & 0x0F))];
            encodeBuf[2] = pem_array[(((b << 2) & 0x3C) | ((c >>> 6) & 0x03))];
            encodeBuf[3] = pem_array[c & 0x3F];
            out.write(encodeBuf);
            lineLen += 4;
            if (lineLen == 64) {
                out.println();
                lineLen = 0;
            }
            len -=3;
        }
        switch (len) {
        case 1:
            atom[0] = data[offset];
            break;
        case 2:
            atom[0] = data[offset];
            atom[1] = data[offset+1];
            break;
        default:
        }
        atomLen = len;
    }
}
