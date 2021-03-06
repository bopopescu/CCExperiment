package org.apache.xerces.impl.io;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Locale;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.util.MessageFormatter;
public final class UTF8Reader
    extends Reader {
    public static final int DEFAULT_BUFFER_SIZE = 2048;
    private static final boolean DEBUG_READ = false;
    protected final InputStream fInputStream;
    protected final byte[] fBuffer;
    protected int fOffset;
    private int fSurrogate = -1;
    private final MessageFormatter fFormatter;
    private final Locale fLocale;
    public UTF8Reader(InputStream inputStream) {
        this(inputStream, DEFAULT_BUFFER_SIZE, new XMLMessageFormatter(), Locale.getDefault());
    } 
    public UTF8Reader(InputStream inputStream, MessageFormatter messageFormatter,
            Locale locale) {
        this(inputStream, DEFAULT_BUFFER_SIZE, messageFormatter, locale);
    } 
    public UTF8Reader(InputStream inputStream, int size,
            MessageFormatter messageFormatter, Locale locale) {
        this(inputStream, new byte[size], messageFormatter, locale);
    } 
    public UTF8Reader(InputStream inputStream, byte [] buffer,
            MessageFormatter messageFormatter, Locale locale) {
        fInputStream = inputStream;
        fBuffer = buffer;
        fFormatter = messageFormatter;
        fLocale = locale;
    } 
    public int read() throws IOException {
        int c = fSurrogate;
        if (fSurrogate == -1) {
            int index = 0;
            int b0 = index == fOffset
                   ? fInputStream.read() : fBuffer[index++] & 0x00FF;
            if (b0 == -1) {
                return -1;
            }
            if (b0 < 0x80) {
                c = (char)b0;
            }
            else if ((b0 & 0xE0) == 0xC0 && (b0 & 0x1E) != 0) {
                int b1 = index == fOffset
                       ? fInputStream.read() : fBuffer[index++] & 0x00FF;
                if (b1 == -1) {
                    expectedByte(2, 2);
                }
                if ((b1 & 0xC0) != 0x80) {
                    invalidByte(2, 2, b1);
                }
                c = ((b0 << 6) & 0x07C0) | (b1 & 0x003F);
            }
            else if ((b0 & 0xF0) == 0xE0) {
                int b1 = index == fOffset
                       ? fInputStream.read() : fBuffer[index++] & 0x00FF;
                if (b1 == -1) {
                    expectedByte(2, 3);
                }
                if ((b1 & 0xC0) != 0x80 
                    || (b0 == 0xED && b1 >= 0xA0)
                    || ((b0 & 0x0F) == 0 && (b1 & 0x20) == 0)) {
                    invalidByte(2, 3, b1);
                }
                int b2 = index == fOffset
                       ? fInputStream.read() : fBuffer[index++] & 0x00FF;
                if (b2 == -1) {
                    expectedByte(3, 3);
                }
                if ((b2 & 0xC0) != 0x80) {
                    invalidByte(3, 3, b2);
                }
                c = ((b0 << 12) & 0xF000) | ((b1 << 6) & 0x0FC0) |
                    (b2 & 0x003F);
            }
            else if ((b0 & 0xF8) == 0xF0) {
                int b1 = index == fOffset
                       ? fInputStream.read() : fBuffer[index++] & 0x00FF;
                if (b1 == -1) {
                    expectedByte(2, 4);
                }
                if ((b1 & 0xC0) != 0x80
                    || ((b1 & 0x30) == 0 && (b0 & 0x07) == 0)) {
                    invalidByte(2, 3, b1);
                }
                int b2 = index == fOffset
                       ? fInputStream.read() : fBuffer[index++] & 0x00FF;
                if (b2 == -1) {
                    expectedByte(3, 4);
                }
                if ((b2 & 0xC0) != 0x80) {
                    invalidByte(3, 3, b2);
                }
                int b3 = index == fOffset
                       ? fInputStream.read() : fBuffer[index++] & 0x00FF;
                if (b3 == -1) {
                    expectedByte(4, 4);
                }
                if ((b3 & 0xC0) != 0x80) {
                    invalidByte(4, 4, b3);
                }
                int uuuuu = ((b0 << 2) & 0x001C) | ((b1 >> 4) & 0x0003);
                if (uuuuu > 0x10) {
                    invalidSurrogate(uuuuu);
                }
                int wwww = uuuuu - 1;
                int hs = 0xD800 |
                         ((wwww << 6) & 0x03C0) | ((b1 << 2) & 0x003C) |
                         ((b2 >> 4) & 0x0003);
                int ls = 0xDC00 | ((b2 << 6) & 0x03C0) | (b3 & 0x003F);
                c = hs;
                fSurrogate = ls;
            }
            else {
                invalidByte(1, 1, b0);
            }
        }
        else {
            fSurrogate = -1;
        }
        if (DEBUG_READ) {
            System.out.println("read(): 0x"+Integer.toHexString(c));
        }
        return c;
    } 
    public int read(char ch[], int offset, int length) throws IOException {
        int out = offset;
        int count = 0;
        if (fOffset == 0) {
            if (length > fBuffer.length) {
                length = fBuffer.length;
            }
            if (fSurrogate != -1) {
                ch[out++] = (char)fSurrogate;
                fSurrogate = -1;
                length--;
            }
            count = fInputStream.read(fBuffer, 0, length);
            if (count == -1) {
                return -1;
            }
            count += out - offset;
        }
        else {
            count = fOffset;
            fOffset = 0;
        }
        final int total = count;
        int in;
        byte byte1;
        final byte byte0 = 0;
        for (in = 0; in < total; in++) {
            byte1 = fBuffer[in];
            if (byte1 >= byte0) {
                ch[out++] = (char)byte1;
            }
            else   {
                break;
            }
        }
        for ( ; in < total; in++) {
            byte1 = fBuffer[in];
            if (byte1 >= byte0) {
                ch[out++] = (char)byte1;
                continue;
            }
            int b0 = byte1 & 0x0FF;
            if ((b0 & 0xE0) == 0xC0 && (b0 & 0x1E) != 0) {
                int b1 = -1;
                if (++in < total) {
                    b1 = fBuffer[in] & 0x00FF;
                }
                else {
                    b1 = fInputStream.read();
                    if (b1 == -1) {
                        if (out > offset) {
                            fBuffer[0] = (byte)b0;
                            fOffset = 1;
                            return out - offset;
                        }
                        expectedByte(2, 2);
                    }
                    count++;
                }
                if ((b1 & 0xC0) != 0x80) {
                    if (out > offset) {
                        fBuffer[0] = (byte)b0;
                        fBuffer[1] = (byte)b1;
                        fOffset = 2;
                        return out - offset;
                    }
                    invalidByte(2, 2, b1);
                }
                int c = ((b0 << 6) & 0x07C0) | (b1 & 0x003F);
                ch[out++] = (char)c;
                count -= 1;
                continue;
            }
            if ((b0 & 0xF0) == 0xE0) {
                int b1 = -1;
                if (++in < total) {
                    b1 = fBuffer[in] & 0x00FF;
                }
                else {
                    b1 = fInputStream.read();
                    if (b1 == -1) {
                        if (out > offset) {
                            fBuffer[0] = (byte)b0;
                            fOffset = 1;
                            return out - offset;
                        }
                        expectedByte(2, 3);
                    }
                    count++;
                }
                if ((b1 & 0xC0) != 0x80 
                    || (b0 == 0xED && b1 >= 0xA0)
                    || ((b0 & 0x0F) == 0 && (b1 & 0x20) == 0)) {
                    if (out > offset) {
                        fBuffer[0] = (byte)b0;
                        fBuffer[1] = (byte)b1;
                        fOffset = 2;
                        return out - offset;
                    }
                    invalidByte(2, 3, b1);
                }
                int b2 = -1;
                if (++in < total) {
                    b2 = fBuffer[in] & 0x00FF;
                }
                else {
                    b2 = fInputStream.read();
                    if (b2 == -1) {
                        if (out > offset) {
                            fBuffer[0] = (byte)b0;
                            fBuffer[1] = (byte)b1;
                            fOffset = 2;
                            return out - offset;
                        }
                        expectedByte(3, 3);
                    }
                    count++;
                }
                if ((b2 & 0xC0) != 0x80) {
                    if (out > offset) {
                        fBuffer[0] = (byte)b0;
                        fBuffer[1] = (byte)b1;
                        fBuffer[2] = (byte)b2;
                        fOffset = 3;
                        return out - offset;
                    }
                    invalidByte(3, 3, b2);
                }
                int c = ((b0 << 12) & 0xF000) | ((b1 << 6) & 0x0FC0) |
                        (b2 & 0x003F);
                ch[out++] = (char)c;
                count -= 2;
                continue;
            }
            if ((b0 & 0xF8) == 0xF0) {
                int b1 = -1;
                if (++in < total) {
                    b1 = fBuffer[in] & 0x00FF;
                }
                else {
                    b1 = fInputStream.read();
                    if (b1 == -1) {
                        if (out > offset) {
                            fBuffer[0] = (byte)b0;
                            fOffset = 1;
                            return out - offset;
                        }
                        expectedByte(2, 4);
                    }
                    count++;
                }
                if ((b1 & 0xC0) != 0x80
                    || ((b1 & 0x30) == 0 && (b0 & 0x07) == 0)) {
                    if (out > offset) {
                        fBuffer[0] = (byte)b0;
                        fBuffer[1] = (byte)b1;
                        fOffset = 2;
                        return out - offset;
                    }
                    invalidByte(2, 4, b1);
                }
                int b2 = -1;
                if (++in < total) {
                    b2 = fBuffer[in] & 0x00FF;
                }
                else {
                    b2 = fInputStream.read();
                    if (b2 == -1) {
                        if (out > offset) {
                            fBuffer[0] = (byte)b0;
                            fBuffer[1] = (byte)b1;
                            fOffset = 2;
                            return out - offset;
                        }
                        expectedByte(3, 4);
                    }
                    count++;
                }
                if ((b2 & 0xC0) != 0x80) {
                    if (out > offset) {
                        fBuffer[0] = (byte)b0;
                        fBuffer[1] = (byte)b1;
                        fBuffer[2] = (byte)b2;
                        fOffset = 3;
                        return out - offset;
                    }
                    invalidByte(3, 4, b2);
                }
                int b3 = -1;
                if (++in < total) {
                    b3 = fBuffer[in] & 0x00FF;
                }
                else {
                    b3 = fInputStream.read();
                    if (b3 == -1) {
                        if (out > offset) {
                            fBuffer[0] = (byte)b0;
                            fBuffer[1] = (byte)b1;
                            fBuffer[2] = (byte)b2;
                            fOffset = 3;
                            return out - offset;
                        }
                        expectedByte(4, 4);
                    }
                    count++;
                }
                if ((b3 & 0xC0) != 0x80) {
                    if (out > offset) {
                        fBuffer[0] = (byte)b0;
                        fBuffer[1] = (byte)b1;
                        fBuffer[2] = (byte)b2;
                        fBuffer[3] = (byte)b3;
                        fOffset = 4;
                        return out - offset;
                    }
                    invalidByte(4, 4, b2);
                }
                int uuuuu = ((b0 << 2) & 0x001C) | ((b1 >> 4) & 0x0003);
                if (uuuuu > 0x10) {
                    invalidSurrogate(uuuuu);
                }
                int wwww = uuuuu - 1;
                int zzzz = b1 & 0x000F;
                int yyyyyy = b2 & 0x003F;
                int xxxxxx = b3 & 0x003F;
                int hs = 0xD800 | ((wwww << 6) & 0x03C0) | (zzzz << 2) | (yyyyyy >> 4);
                int ls = 0xDC00 | ((yyyyyy << 6) & 0x03C0) | xxxxxx;
                ch[out++] = (char)hs;
                if ((count -= 2) <= length) {
                    ch[out++] = (char)ls;
                }
                else {
                    fSurrogate = ls;
                    --count;
                }
                continue;
            }
            if (out > offset) {
                fBuffer[0] = (byte)b0;
                fOffset = 1;
                return out - offset;
            }
            invalidByte(1, 1, b0);
        }
        if (DEBUG_READ) {
            System.out.println("read(char[],"+offset+','+length+"): count="+count);
        }
        return count;
    } 
    public long skip(long n) throws IOException {
        long remaining = n;
        final char[] ch = new char[fBuffer.length];
        do {
            int length = ch.length < remaining ? ch.length : (int)remaining;
            int count = read(ch, 0, length);
            if (count > 0) {
                remaining -= count;
            }
            else {
                break;
            }
        } while (remaining > 0);
        long skipped = n - remaining;
        return skipped;
    } 
    public boolean ready() throws IOException {
        return false;
    } 
    public boolean markSupported() {
        return false;
    } 
    public void mark(int readAheadLimit) throws IOException {
        throw new IOException(fFormatter.formatMessage(fLocale, "OperationNotSupported", new Object[]{"mark()", "UTF-8"}));
    } 
    public void reset() throws IOException {
        fOffset = 0;
        fSurrogate = -1;
    } 
    public void close() throws IOException {
        fInputStream.close();
    } 
    private void expectedByte(int position, int count)
        throws MalformedByteSequenceException {
        throw new MalformedByteSequenceException(fFormatter,
            fLocale,
            XMLMessageFormatter.XML_DOMAIN,
            "ExpectedByte",
            new Object[] {Integer.toString(position), Integer.toString(count)});
    } 
    private void invalidByte(int position, int count, int c)
        throws MalformedByteSequenceException {
        throw new MalformedByteSequenceException(fFormatter,
            fLocale,
            XMLMessageFormatter.XML_DOMAIN,
            "InvalidByte", 
            new Object [] {Integer.toString(position), Integer.toString(count)});
    } 
    private void invalidSurrogate(int uuuuu) throws MalformedByteSequenceException {
        throw new MalformedByteSequenceException(fFormatter,
            fLocale,
            XMLMessageFormatter.XML_DOMAIN,
            "InvalidHighSurrogate", 
            new Object[] {Integer.toHexString(uuuuu)});
    } 
} 
