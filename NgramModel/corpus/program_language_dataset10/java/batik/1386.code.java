package org.apache.batik.util;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;
public class ParsedURLData {
    protected static final String HTTP_USER_AGENT_HEADER      = "User-Agent";
    protected static final String HTTP_ACCEPT_HEADER          = "Accept";
    protected static final String HTTP_ACCEPT_LANGUAGE_HEADER = "Accept-Language";
    protected static final String HTTP_ACCEPT_ENCODING_HEADER = "Accept-Encoding";
    protected static List acceptedEncodings = new LinkedList();
    static {
        acceptedEncodings.add("gzip");
    }
    public static final byte[] GZIP_MAGIC = {(byte)0x1f, (byte)0x8b};
    public static InputStream checkGZIP(InputStream is)
        throws IOException {
            if (!is.markSupported())
                is = new BufferedInputStream(is);
            byte[] data = new byte[2];
            try {
                is.mark(2);
                is.read(data);
                is.reset();
            } catch (Exception ex) {
                is.reset();
                return is;
            }
        if ((data[0] == GZIP_MAGIC[0]) &&
            (data[1] == GZIP_MAGIC[1]))
            return new GZIPInputStream(is);
        if (((data[0]&0x0F)  == 8) &&
            ((data[0]>>>4)   <= 7)) {
            int chk = ((((int)data[0])&0xFF)*256+
                       (((int)data[1])&0xFF));
            if ((chk %31)  == 0) {
                try {
                    is.mark(100);
                    InputStream ret = new InflaterInputStream(is);
                    if (!ret.markSupported())
                        ret = new BufferedInputStream(ret);
                    ret.mark(2);
                    ret.read(data);
                    is.reset();
                    ret = new InflaterInputStream(is);
                    return ret;
                } catch (ZipException ze) {
                    is.reset();
                    return is;
                }
            }
        }
        return is;
    }
    public String protocol        = null;
    public String host            = null;
    public int    port            = -1;
    public String path            = null;
    public String ref             = null;
    public String contentType     = null;
    public String contentEncoding = null;
    public InputStream stream     = null;
    public boolean hasBeenOpened  = false;
    protected String contentTypeMediaType;
    protected String contentTypeCharset;
    protected URL postConnectionURL;
    public ParsedURLData() {
    }
    public ParsedURLData(URL url) {
        protocol = url.getProtocol();
        if ((protocol != null) && (protocol.length() == 0))
            protocol = null;
        host = url.getHost();
        if ((host != null) && (host.length() == 0))
            host = null;
        port     = url.getPort();
        path     = url.getFile();
        if ((path != null) && (path.length() == 0))
            path = null;
        ref      = url.getRef();
        if ((ref != null) && (ref.length() == 0))
            ref = null;
    }
    protected URL buildURL() throws MalformedURLException {
        if ((protocol != null) && (host != null)) {
            String file = "";
            if (path != null)
                file = path;
            if (port == -1)
                return new URL(protocol, host, file);
            return new URL(protocol, host, port, file);
        }
        return new URL(toString());
    }
    public int hashCode() {
        int hc = port;
        if (protocol != null)
            hc ^= protocol.hashCode();
        if (host != null)
            hc ^= host.hashCode();
        if (path != null) {
            int len = path.length();
            if (len > 20)
                hc ^= path.substring(len-20).hashCode();
            else
                hc ^= path.hashCode();
        }
        if (ref != null) {
            int len = ref.length();
            if (len > 20)
                hc ^= ref.substring(len-20).hashCode();
            else
                hc ^= ref.hashCode();
        }
        return hc;
    }
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (! (obj instanceof ParsedURLData))
            return false;
        ParsedURLData ud = (ParsedURLData)obj;
        if (ud.port != port)
            return false;
        if (ud.protocol==null) {
            if (protocol != null)
                return false;
        } else if (protocol == null)
            return false;
        else if (!ud.protocol.equals(protocol))
            return false;
        if (ud.host==null) {
            if (host   !=null)
                return false;
        } else if (host == null)
            return false;
        else if (!ud.host.equals(host))
            return false;
        if (ud.ref==null) {
            if (ref   !=null)
                return false;
        } else if (ref == null)
            return false;
        else if (!ud.ref.equals(ref))
            return false;
        if (ud.path==null) {
            if (path   !=null)
                return false;
        } else if (path == null)
            return false;
        else if (!ud.path.equals(path))
            return false;
        return true;
    }
    public String getContentType(String userAgent) {
        if (contentType != null)
            return contentType;
        if (!hasBeenOpened) {
            try {
                openStreamInternal(userAgent, null,  null);
            } catch (IOException ioe) {  }
        }
        return contentType;
    }
    public String getContentTypeMediaType(String userAgent) {
        if (contentTypeMediaType != null) {
            return contentTypeMediaType;
        }
        extractContentTypeParts(userAgent);
        return contentTypeMediaType;
    }
    public String getContentTypeCharset(String userAgent) {
        if (contentTypeMediaType != null) {
            return contentTypeCharset;
        }
        extractContentTypeParts(userAgent);
        return contentTypeCharset;
    }
    public boolean hasContentTypeParameter(String userAgent, String param) {
        getContentType(userAgent);
        if (contentType == null) {
            return false;
        }
        int i = 0;
        int len = contentType.length();
        int plen = param.length();
loop1:  while (i < len) {
            switch (contentType.charAt(i)) {
                case ' ':
                case ';':
                    break loop1;
            }
            i++;
        }
        if (i == len) {
            contentTypeMediaType = contentType;
        } else {
            contentTypeMediaType = contentType.substring(0, i);
        }
loop2:  for (;;) {
            while (i < len && contentType.charAt(i) != ';') {
                i++;
            }
            if (i == len) {
                return false;
            }
            i++;
            while (i < len && contentType.charAt(i) == ' ') {
                i++;
            }
            if (i >= len - plen - 1) {
                return false;
            }
            for (int j = 0; j < plen; j++) {
                if (!(contentType.charAt(i++) == param.charAt(j))) {
                    continue loop2;
                }
            }
            if (contentType.charAt(i) == '=') {
                return true;
            }
        }
    }
    protected void extractContentTypeParts(String userAgent) {
        getContentType(userAgent);
        if (contentType == null) {
            return;
        }
        int i = 0;
        int len = contentType.length();
loop1:  while (i < len) {
            switch (contentType.charAt(i)) {
                case ' ':
                case ';':
                    break loop1;
            }
            i++;
        }
        if (i == len) {
            contentTypeMediaType = contentType;
        } else {
            contentTypeMediaType = contentType.substring(0, i);
        }
        for (;;) {
            while (i < len && contentType.charAt(i) != ';') {
                i++;
            }
            if (i == len) {
                return;
            }
            i++;
            while (i < len && contentType.charAt(i) == ' ') {
                i++;
            }
            if (i >= len - 8) {
                return;
            }
            if (contentType.charAt(i++) == 'c') {
                if (contentType.charAt(i++) != 'h') continue;
                if (contentType.charAt(i++) != 'a') continue;
                if (contentType.charAt(i++) != 'r') continue;
                if (contentType.charAt(i++) != 's') continue;
                if (contentType.charAt(i++) != 'e') continue;
                if (contentType.charAt(i++) != 't') continue;
                if (contentType.charAt(i++) != '=') continue;
                int j = i;
loop2:          while (i < len) {
                    switch (contentType.charAt(i)) {
                        case ' ':
                        case ';':
                            break loop2;
                    }
                    i++;
                }
                contentTypeCharset = contentType.substring(j, i);
                return;
            }
        }
    }
    public String getContentEncoding(String userAgent) {
        if (contentEncoding != null)
            return contentEncoding;
        if (!hasBeenOpened) {
            try {
                openStreamInternal(userAgent, null,  null);
            } catch (IOException ioe) {  }
        }
        return contentEncoding;
    }
    public boolean complete() {
        try {
            buildURL();
        } catch (MalformedURLException mue) {
            return false;
        }
        return true;
    }
    public InputStream openStream(String userAgent, Iterator mimeTypes)
        throws IOException {
        InputStream raw = openStreamInternal(userAgent, mimeTypes,
                                             acceptedEncodings.iterator());
        if (raw == null)
            return null;
        stream = null;
        return checkGZIP(raw);
    }
    public InputStream openStreamRaw(String userAgent, Iterator mimeTypes)
        throws IOException {
        InputStream ret = openStreamInternal(userAgent, mimeTypes, null);
        stream = null;
        return ret;
    }
    protected InputStream openStreamInternal(String userAgent,
                                             Iterator mimeTypes,
                                             Iterator encodingTypes)
        throws IOException {
        if (stream != null)
            return stream;
        hasBeenOpened = true;
        URL url = null;
        try {
            url = buildURL();
        } catch (MalformedURLException mue) {
            throw new IOException
                ("Unable to make sense of URL for connection");
        }
        if (url == null)
            return null;
        URLConnection urlC = url.openConnection();
        if (urlC instanceof HttpURLConnection) {
            if (userAgent != null)
                urlC.setRequestProperty(HTTP_USER_AGENT_HEADER, userAgent);
            if (mimeTypes != null) {
                String acceptHeader = "";
                while (mimeTypes.hasNext()) {
                    acceptHeader += mimeTypes.next();
                    if (mimeTypes.hasNext())
                        acceptHeader += ",";
                }
                urlC.setRequestProperty(HTTP_ACCEPT_HEADER, acceptHeader);
            }
            if (encodingTypes != null) {
                String encodingHeader = "";
                while (encodingTypes.hasNext()) {
                    encodingHeader += encodingTypes.next();
                    if (encodingTypes.hasNext())
                        encodingHeader += ",";
                }
                urlC.setRequestProperty(HTTP_ACCEPT_ENCODING_HEADER,
                                        encodingHeader);
            }
            contentType       = urlC.getContentType();
            contentEncoding   = urlC.getContentEncoding();
            postConnectionURL = urlC.getURL();
        }
        try {
            return (stream = urlC.getInputStream());
        } catch (IOException e) {
            if (urlC instanceof HttpURLConnection) {
                return (stream = ((HttpURLConnection) urlC).getErrorStream());
            } else {
                throw e;
            }
        }
    }
    public String getPortStr() {
        String portStr ="";
        if (protocol != null)
            portStr += protocol + ":";
        if ((host != null) || (port != -1)) {
            portStr += "//";
            if (host != null) portStr += host;
            if (port != -1)   portStr += ":" + port;
        }
        return portStr;
    }
    protected boolean sameFile(ParsedURLData other) {
        if (this == other) return true;
        if ((port      == other.port) &&
            ((path     == other.path)
             || ((path!=null) && path.equals(other.path))) &&
            ((host     == other.host)
             || ((host!=null) && host.equals(other.host))) &&
            ((protocol == other.protocol)
             || ((protocol!=null) && protocol.equals(other.protocol))))
            return true;
        return false;
    }
    public String toString() {
        String ret = getPortStr();
        if (path != null)
            ret += path;
        if (ref != null)
            ret += "#" + ref;
        return ret;
    }
    public String getPostConnectionURL() {
        if (postConnectionURL != null) {
            if (ref != null) {
                return postConnectionURL.toString() + '#' + ref;
            }
            return postConnectionURL.toString();
        }
        return toString();
    }
}
