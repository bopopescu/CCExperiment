package org.apache.tools.zip;
public class UnicodeCommentExtraField extends AbstractUnicodeExtraField {
    public static final ZipShort UCOM_ID = new ZipShort(0x6375);
    public UnicodeCommentExtraField () { 
    }
    public UnicodeCommentExtraField(String text, byte[] bytes, int off,
                                    int len) {
        super(text, bytes, off, len);
    }
    public UnicodeCommentExtraField(String comment, byte[] bytes) {
        super(comment, bytes);
    }
    public ZipShort getHeaderId() {
        return UCOM_ID;
    }
}
