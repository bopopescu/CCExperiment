package org.apache.batik.svggen;
import java.util.List;
import java.util.Map;
public interface SVGDescriptor{
    Map getAttributeMap(Map attrMap);
    List getDefinitionSet(List defSet);
}
