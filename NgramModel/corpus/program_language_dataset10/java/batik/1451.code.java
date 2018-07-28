package org.apache.batik.xml;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import org.apache.batik.util.EncodingUtilities;
public class XMLUtilities extends XMLCharacters {
    public static final int IS_XML_10_NAME  = 1;
    public static final int IS_XML_10_QNAME = 2;
    protected XMLUtilities() {
    }
    public static boolean isXMLSpace(char c) {
      return (c <= 0x0020) &&
             (((((1L << 0x0009) |
                 (1L << 0x000A) |
                 (1L << 0x000D) |
                 (1L << 0x0020)) >> c) & 1L) != 0);
    }
    public static boolean isXMLNameFirstCharacter(char c) {
        return (NAME_FIRST_CHARACTER[c / 32] & (1 << (c % 32))) != 0;
    }
    public static boolean isXML11NameFirstCharacter(char c) {
        return (NAME11_FIRST_CHARACTER[c / 32] & (1 << (c % 32))) != 0;
    }
    public static boolean isXMLNameCharacter(char c) {
        return (NAME_CHARACTER[c / 32] & (1 << (c % 32))) != 0;
    }
    public static boolean isXML11NameCharacter(char c) {
        return (NAME11_CHARACTER[c / 32] & (1 << (c % 32))) != 0;
    }
    public static boolean isXMLCharacter(int c) {
        return ( ( ( XML_CHARACTER[c >>> 5 ] & (1 << (c & 0x1F ))) != 0 )
                || (c >= 0x10000 && c <= 0x10ffff) );
    }
    public static boolean isXML11Character(int c) {
        return c >= 1 && c <= 0xd7ff
            || c >= 0xe000 && c <= 0xfffd
            || c >= 0x10000 && c <= 0x10ffff;
    }
    public static boolean isXMLPublicIdCharacter(char c) {
        return (c < 128) &&
            (PUBLIC_ID_CHARACTER[c / 32] & (1 << (c % 32))) != 0;
    }
    public static boolean isXMLVersionCharacter(char c) {
        return (c < 128) &&
            (VERSION_CHARACTER[c / 32] & (1 << (c % 32))) != 0;
    }
    public static boolean isXMLAlphabeticCharacter(char c) {
        return (c < 128) &&
            (ALPHABETIC_CHARACTER[c / 32] & (1 << (c % 32))) != 0;
    }
    public static int testXMLQName(String s) {
        int isQName = IS_XML_10_QNAME;
        boolean foundColon = false;
        int len = s.length();
        if (len == 0) {
            return 0;
        }
        char c = s.charAt(0);
        if (!isXMLNameFirstCharacter(c)) {
            return 0;
        }
        if (c == ':') {
            isQName = 0;
        }
        for (int i = 1; i < len; i++) {
            c = s.charAt(i);
            if (!isXMLNameCharacter(c)) {
                return 0;
            }
            if (isQName != 0 && c == ':') {
                if (foundColon || i == len - 1) {
                    isQName = 0;
                } else {
                    foundColon = true;
                }
            }
        }
        return IS_XML_10_NAME | isQName;
    }
    public static Reader createXMLDocumentReader(InputStream is)
        throws IOException {
        PushbackInputStream pbis = new PushbackInputStream(is, 128);
        byte[] buf = new byte[4];
        int len = pbis.read(buf);
        if (len > 0) {
            pbis.unread(buf, 0, len);
        }
        if (len == 4) {
            switch (buf[0] & 0x00FF) {
            case 0:
                if (buf[1] == 0x003c && buf[2] == 0x0000 && buf[3] == 0x003f) {
                    return new InputStreamReader(pbis, "UnicodeBig");
                }
                break;
            case '<':
                switch (buf[1] & 0x00FF) {
                case 0:
                    if (buf[2] == 0x003f && buf[3] == 0x0000) {
                        return new InputStreamReader(pbis, "UnicodeLittle");
                    }
                    break;
                case '?':
                    if (buf[2] == 'x' && buf[3] == 'm') {
                        Reader r = createXMLDeclarationReader(pbis, "UTF8");
                        String enc = getXMLDeclarationEncoding(r, "UTF8");
                        return new InputStreamReader(pbis, enc);
                    }
                }
                break;
            case 0x004C:
                if (buf[1] == 0x006f &&
                    (buf[2] & 0x00FF) == 0x00a7 &&
                    (buf[3] & 0x00FF) == 0x0094) {
                    Reader r = createXMLDeclarationReader(pbis, "CP037");
                    String enc = getXMLDeclarationEncoding(r, "CP037");
                    return new InputStreamReader(pbis, enc);
                }
                break;
            case 0x00FE:
                if ((buf[1] & 0x00FF) == 0x00FF) {
                    return new InputStreamReader(pbis, "Unicode");
                }
                break;
            case 0x00FF:
                if ((buf[1] & 0x00FF) == 0x00FE) {
                    return new InputStreamReader(pbis, "Unicode");
                }
            }
        }
        return new InputStreamReader(pbis, "UTF8");
    }
    protected static Reader createXMLDeclarationReader(PushbackInputStream pbis,
                                                       String enc)
        throws IOException {
        byte[] buf = new byte[128];
        int len = pbis.read(buf);
        if (len > 0) {
            pbis.unread(buf, 0, len);
        }
        return new InputStreamReader(new ByteArrayInputStream(buf, 4, len), enc);
    }
    protected static String getXMLDeclarationEncoding(Reader r, String e)
        throws IOException {
        int c;
        if ((c = r.read()) != 'l') {
            return e;
        }
        if (!isXMLSpace((char)(c = r.read()))) {
            return e;
        }
        while (isXMLSpace((char)(c = r.read())));
        if (c != 'v') {
            return e;
        }
        if ((c = r.read()) != 'e') {
            return e;
        }
        if ((c = r.read()) != 'r') {
            return e;
        }
        if ((c = r.read()) != 's') {
            return e;
        }
        if ((c = r.read()) != 'i') {
            return e;
        }
        if ((c = r.read()) != 'o') {
            return e;
        }
        if ((c = r.read()) != 'n') {
            return e;
        }
        c = r.read();
        while (isXMLSpace((char)c)) {
            c = r.read();
        }
        if (c != '=') {
            return e;
        }
        while (isXMLSpace((char)(c = r.read())));
        if (c != '"' && c != '\'') {
            return e;
        }
        char sc = (char)c;
        for (;;) {
            c = r.read();
            if (c == sc) {
                break;
            }
            if (!isXMLVersionCharacter((char)c)) {
                return e;
            }
        }
        if (!isXMLSpace((char)(c = r.read()))) {
            return e;
        }
        while (isXMLSpace((char)(c = r.read())));
        if (c != 'e') {
            return e;
        }
        if ((c = r.read()) != 'n') {
            return e;
        }
        if ((c = r.read()) != 'c') {
            return e;
        }
        if ((c = r.read()) != 'o') {
            return e;
        }
        if ((c = r.read()) != 'd') {
            return e;
        }
        if ((c = r.read()) != 'i') {
            return e;
        }
        if ((c = r.read()) != 'n') {
            return e;
        }
        if ((c = r.read()) != 'g') {
            return e;
        }
        c = r.read();
        while (isXMLSpace((char)c)) {
            c = r.read();
        }
        if (c != '=') {
            return e;
        }
        while (isXMLSpace((char)(c = r.read())));
        if (c != '"' && c != '\'') {
            return e;
        }
        sc = (char)c;
        StringBuffer enc = new StringBuffer();
        for (;;) {
            c = r.read();
            if (c == -1) {
                return e;
            }
            if (c == sc) {
                return encodingToJavaEncoding(enc.toString(), e);
            }
            enc.append((char)c);
        }
    }
    public static String encodingToJavaEncoding(String e, String de) {
        String result = EncodingUtilities.javaEncoding(e);
        return (result == null) ? de : result;
    }
}