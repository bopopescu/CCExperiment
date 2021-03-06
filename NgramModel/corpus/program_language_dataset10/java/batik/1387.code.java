package org.apache.batik.util;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
public class ParsedURLDataProtocolHandler 
    extends AbstractParsedURLProtocolHandler {
    static final String DATA_PROTOCOL = "data";
    static final String BASE64 = "base64";
    static final String CHARSET = "charset";
    public ParsedURLDataProtocolHandler() {
        super(DATA_PROTOCOL);
    }
    public ParsedURLData parseURL(ParsedURL baseURL, String urlStr) {
        return parseURL(urlStr);
    }
    public ParsedURLData parseURL(String urlStr) {
        DataParsedURLData ret = new DataParsedURLData();
        int pidx=0, idx;
        int len = urlStr.length();
        idx = urlStr.indexOf('#');
        ret.ref = null;
        if (idx != -1) {
            if (idx + 1 < len) {
                ret.ref = urlStr.substring(idx + 1);
            }
            urlStr = urlStr.substring(0, idx);
            len = urlStr.length();
        }
        idx = urlStr.indexOf(':');
        if (idx != -1) {
            ret.protocol = urlStr.substring(pidx, idx);
            if (ret.protocol.indexOf('/') == -1)
                pidx = idx+1;
            else {
                ret.protocol = null;
                pidx = 0;
            }
        }
        idx = urlStr.indexOf(',',pidx);
        if ((idx != -1) && (idx != pidx)) {
            ret.host = urlStr.substring(pidx, idx);
            pidx = idx+1;
            int aidx = ret.host.lastIndexOf(';');
            if ((aidx == -1) || (aidx==ret.host.length())) {
                ret.contentType = ret.host;
            } else {
                String enc = ret.host.substring(aidx+1);
                idx = enc.indexOf('=');
                if (idx == -1) {
                    ret.contentEncoding = enc;
                    ret.contentType = ret.host.substring(0, aidx);
                } else {
                    ret.contentType = ret.host;
                }
                aidx = 0;
                idx = ret.contentType.indexOf(';', aidx);
                if (idx != -1) {
                    aidx = idx+1;
                    while (aidx < ret.contentType.length()) {
                        idx = ret.contentType.indexOf(';', aidx);
                        if (idx == -1) idx = ret.contentType.length();
                        String param = ret.contentType.substring(aidx, idx);
                        int eqIdx = param.indexOf('=');
                        if ((eqIdx != -1) &&
                            (CHARSET.equals(param.substring(0,eqIdx)))) 
                            ret.charset = param.substring(eqIdx+1);
                        aidx = idx+1;
                    }
                }
            }
        }
        if (pidx < urlStr.length()) {
            ret.path = urlStr.substring(pidx);
        }
        return ret;
    }
    static class DataParsedURLData extends ParsedURLData {
        String charset;
        public boolean complete() {
            return path != null;
        }
        public String getPortStr() {
            String portStr = "data:";
            if (host != null) {
                portStr += host;
            }
            portStr += ",";
            return portStr;
        }
        public String toString() {
            String ret = getPortStr();
            if (path != null) {
                ret += path;
            }
            if (ref != null) {
                ret += '#' + ref;
            }
            return ret;
        }
        public String getContentType(String userAgent) {
            return contentType;
        }
        public String getContentEncoding(String userAgent) {
            return contentEncoding;
        }
        protected InputStream openStreamInternal
            (String userAgent, Iterator mimeTypes, Iterator encodingTypes)
            throws IOException {
            stream = decode(path);
            if (BASE64.equals(contentEncoding)) {
                stream = new Base64DecodeStream(stream);
            }
            return stream;
        }
        public static InputStream decode(String s) {
            int len = s.length();
            byte [] data = new byte[len];
            int j=0;
            for(int i=0; i<len; i++) {
                char c = s.charAt(i);
                switch (c) {
                default : data[j++]= (byte)c;   break;
                case '%': {
                    if (i+2 < len) {
                        i += 2;
                        byte b; 
                        char c1 = s.charAt(i-1);
                        if      (c1 >= '0' && c1 <= '9') b=(byte)(c1-'0');
                        else if (c1 >= 'a' && c1 <= 'z') b=(byte)(c1-'a'+10);
                        else if (c1 >= 'A' && c1 <= 'Z') b=(byte)(c1-'A'+10);
                        else break;
                        b*=16;
                        char c2 = s.charAt(i);
                        if      (c2 >= '0' && c2 <= '9') b+=(byte)(c2-'0');
                        else if (c2 >= 'a' && c2 <= 'z') b+=(byte)(c2-'a'+10);
                        else if (c2 >= 'A' && c2 <= 'Z') b+=(byte)(c2-'A'+10);
                        else break;
                        data[j++] = b;
                    }
                }
                break;
                }
            }
            return new ByteArrayInputStream(data, 0, j);
        }
    }
}
