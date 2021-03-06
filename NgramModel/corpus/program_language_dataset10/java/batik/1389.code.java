package org.apache.batik.util;
import java.net.MalformedURLException;
import java.net.URL;
public class ParsedURLJarProtocolHandler 
    extends ParsedURLDefaultProtocolHandler {
    public static final String JAR = "jar";
    public ParsedURLJarProtocolHandler() {
        super(JAR);
    }
    public ParsedURLData parseURL(ParsedURL baseURL, String urlStr) {
        String start = urlStr.substring(0, JAR.length()+1).toLowerCase();
        if (start.equals(JAR+":"))
            return parseURL(urlStr);
        try {
            URL context = new URL(baseURL.toString());
            URL url     = new URL(context, urlStr);
            return constructParsedURLData(url);
        } catch (MalformedURLException mue) {
            return super.parseURL(baseURL, urlStr);
        }
    }
}
