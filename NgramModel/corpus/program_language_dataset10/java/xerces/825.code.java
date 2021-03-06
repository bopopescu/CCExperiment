package org.apache.xerces.util;
public class XML11CharGenerator {
    private static final byte XML11CHARS [] = new byte [1 << 16];
    public static final int MASK_XML11_VALID = 0x01;
    public static final int MASK_XML11_SPACE = 0x02;
    public static final int MASK_XML11_NAME_START = 0x04;
    public static final int MASK_XML11_NAME = 0x08;
    public static final int MASK_XML11_CONTROL = 0x10;
    public static final int MASK_XML11_CONTENT = 0x20;
    public static final int MASK_XML11_NCNAME_START = 0x40;
    public static final int MASK_XML11_NCNAME = 0x80;
    public static final int MASK_XML11_CONTENT_INTERNAL = MASK_XML11_CONTROL | MASK_XML11_CONTENT; 
    static {
        int xml11NonWhitespaceRange  [] = {
                0x21, 0x7E, 0xA0, 0x2027, 0x2029, 0xD7FF, 0xE000, 0xFFFD, 
        };
        int xml11WhitespaceChars [] = {
            0x9, 0xA, 0xD, 0x20, 0x85, 0x2028,
        };
        int xml11ControlCharRange [] = {
            0x1, 0x8, 0xB, 0xC, 0xE, 0x1F, 0x7f, 0x84, 0x86, 0x9f,
        };
        int xml11NameStartCharRange [] = {
            ':', ':', 'A', 'Z', '_', '_', 'a', 'z', 
            0xC0, 0xD6, 0xD8, 0xF6, 0xF8, 0x2FF,
            0x370, 0x37D, 0x37F, 0x1FFF, 0x200C, 0x200D,
            0x2070, 0x218F, 0x2C00, 0x2FEF, 0x3001, 0xD7FF,
            0xF900, 0xFDCF, 0xFDF0, 0xFFFD,
        };
        int xml11NameCharRange [] = {
            '-', '-', '.', '.', '0', '9', 0xB7, 0xB7, 
            0x0300, 0x036F, 0x203F, 0x2040,
        };
        int xml11SpecialChars[] = {
            '<', '&', '\n', '\r', ']',
        };
        for(int i=0; i<xml11NonWhitespaceRange.length; i+=2) {
            for(int j=xml11NonWhitespaceRange[i]; j<=xml11NonWhitespaceRange[i+1]; j++) {
                XML11CHARS[j] |= MASK_XML11_VALID | MASK_XML11_CONTENT;
            }
        }
        for(int i=0; i<xml11WhitespaceChars.length; i++) {
            XML11CHARS[xml11WhitespaceChars[i]] |= MASK_XML11_VALID | MASK_XML11_SPACE | MASK_XML11_CONTENT;
        }
        for(int i=0; i<xml11ControlCharRange.length; i+=2) {
            for(int j=xml11ControlCharRange[i]; j<=xml11ControlCharRange[i+1]; j++) {
                XML11CHARS[j] |= MASK_XML11_VALID | MASK_XML11_CONTROL;
            }
        }
        for (int i = 0; i < xml11NameStartCharRange.length; i+=2) {
            for(int j=xml11NameStartCharRange[i]; j<=xml11NameStartCharRange[i+1]; j++) {
                XML11CHARS[j] |= MASK_XML11_NAME_START | MASK_XML11_NAME |
                        MASK_XML11_NCNAME_START | MASK_XML11_NCNAME;
            }
        }
        for (int i=0; i<xml11NameCharRange.length; i+=2) {
            for(int j=xml11NameCharRange[i]; j<=xml11NameCharRange[i+1]; j++) {
                XML11CHARS[j] |= MASK_XML11_NAME | MASK_XML11_NCNAME;
            }
        }
        XML11CHARS[':'] &= ~(MASK_XML11_NCNAME_START | MASK_XML11_NCNAME);
        for(int i=0;i<xml11SpecialChars.length; i++) {
            XML11CHARS[xml11SpecialChars[i]] &= (~MASK_XML11_CONTENT);
        }
    } 
    public static void main(String[] args) {
        ArrayFillingCodeGenerator.generateByteArray("XML11CHARS", XML11CHARS, System.out);
    }
}
