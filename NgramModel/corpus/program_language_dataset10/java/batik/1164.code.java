package org.apache.batik.svggen;
import java.util.HashSet;
import java.util.Set;
public class SVGAttribute {
    private String name;
    private Set applicabilitySet;
    private boolean isSetInclusive;
    public SVGAttribute(Set applicabilitySet, boolean isSetInclusive){
        if(applicabilitySet == null)
            applicabilitySet = new HashSet();
        this.applicabilitySet = applicabilitySet;
        this.isSetInclusive = isSetInclusive;
    }
    public boolean appliesTo(String tag){
        boolean tagInMap = applicabilitySet.contains(tag);
        if(isSetInclusive)
            return tagInMap;
        else
            return !tagInMap;
    }
}
