package org.apache.batik.svggen;
import java.util.HashMap;
import java.util.Map;
public class SVGIDGenerator {
    private Map prefixMap = new HashMap();
    public SVGIDGenerator() {
    }
    public String generateID(String prefix) {
        Integer maxId = (Integer)prefixMap.get(prefix);
        if (maxId == null) {
            maxId = new Integer(0);
            prefixMap.put(prefix, maxId);
        }
        maxId = new Integer(maxId.intValue()+1);
        prefixMap.put(prefix, maxId);
        return prefix + maxId;
    }
}
