package org.apache.batik.bridge;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.TileRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
public class SVGFeTileElementBridge
    extends AbstractSVGFilterPrimitiveElementBridge {
    public SVGFeTileElementBridge() {}
    public String getLocalName() {
        return SVG_FE_TILE_TAG;
    }
    public Filter createFilter(BridgeContext ctx,
                               Element filterElement,
                               Element filteredElement,
                               GraphicsNode filteredNode,
                               Filter inputFilter,
                               Rectangle2D filterRegion,
                               Map filterMap) {
        Rectangle2D defaultRegion = filterRegion;
        Rectangle2D primitiveRegion
            = SVGUtilities.convertFilterPrimitiveRegion(filterElement,
                                                        filteredElement,
                                                        filteredNode,
                                                        defaultRegion,
                                                        filterRegion,
                                                        ctx);
        Filter in = getIn(filterElement,
                          filteredElement,
                          filteredNode,
                          inputFilter,
                          filterMap,
                          ctx);
        if (in == null) {
            return null; 
        }
        Filter filter
            = new TileRable8Bit(in, primitiveRegion, in.getBounds2D(), false);
        handleColorInterpolationFilters(filter, filterElement);
        updateFilterMap(filterElement, filter, filterMap);
        return filter;
    }
}
