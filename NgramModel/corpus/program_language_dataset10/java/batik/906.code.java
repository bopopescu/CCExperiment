package org.apache.batik.ext.awt.image.spi;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.util.ParsedURL;
public interface URLRegistryEntry extends RegistryEntry {
    boolean isCompatibleURL(ParsedURL url);
    Filter handleURL(ParsedURL url, boolean needRawData);
}
