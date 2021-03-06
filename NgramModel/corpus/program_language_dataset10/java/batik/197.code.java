package org.apache.batik.bridge;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.TurbulenceRable;
import org.apache.batik.ext.awt.image.renderable.TurbulenceRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
public class SVGFeTurbulenceElementBridge
    extends AbstractSVGFilterPrimitiveElementBridge {
    public SVGFeTurbulenceElementBridge() {}
    public String getLocalName() {
        return SVG_FE_TURBULENCE_TAG;
    }
    public Filter createFilter(BridgeContext ctx,
                               Element filterElement,
                               Element filteredElement,
                               GraphicsNode filteredNode,
                               Filter inputFilter,
                               Rectangle2D filterRegion,
                               Map filterMap) {
        Filter in = getIn(filterElement,
                          filteredElement,
                          filteredNode,
                          inputFilter,
                          filterMap,
                          ctx);
        if (in == null) {
            return null; 
        }
        Rectangle2D defaultRegion = filterRegion;
        Rectangle2D primitiveRegion
            = SVGUtilities.convertFilterPrimitiveRegion(filterElement,
                                                        filteredElement,
                                                        filteredNode,
                                                        defaultRegion,
                                                        filterRegion,
                                                        ctx);
        float [] baseFrequency
            = convertBaseFrenquency(filterElement, ctx);
        int numOctaves
            = convertInteger(filterElement, SVG_NUM_OCTAVES_ATTRIBUTE, 1, ctx);
        int seed
            = convertInteger(filterElement, SVG_SEED_ATTRIBUTE, 0, ctx);
        boolean stitchTiles
            = convertStitchTiles(filterElement, ctx);
        boolean isFractalNoise
            = convertType(filterElement, ctx);
        TurbulenceRable turbulenceRable
            = new TurbulenceRable8Bit(primitiveRegion);
        turbulenceRable.setBaseFrequencyX(baseFrequency[0]);
        turbulenceRable.setBaseFrequencyY(baseFrequency[1]);
        turbulenceRable.setNumOctaves(numOctaves);
        turbulenceRable.setSeed(seed);
        turbulenceRable.setStitched(stitchTiles);
        turbulenceRable.setFractalNoise(isFractalNoise);
        handleColorInterpolationFilters(turbulenceRable, filterElement);
        updateFilterMap(filterElement, turbulenceRable, filterMap);
        return turbulenceRable;
    }
    protected static float[] convertBaseFrenquency(Element e,
                                                   BridgeContext ctx) {
        String s = e.getAttributeNS(null, SVG_BASE_FREQUENCY_ATTRIBUTE);
        if (s.length() == 0) {
            return new float[] {0.001f, 0.001f};
        }
        float[] v = new float[2];
        StringTokenizer tokens = new StringTokenizer(s, " ,");
        try {
            v[0] = SVGUtilities.convertSVGNumber(tokens.nextToken());
            if (tokens.hasMoreTokens()) {
                v[1] = SVGUtilities.convertSVGNumber(tokens.nextToken());
            } else {
                v[1] = v[0];
            }
            if (tokens.hasMoreTokens()) {
                throw new BridgeException
                    (ctx, e, ERR_ATTRIBUTE_VALUE_MALFORMED,
                     new Object[] {SVG_BASE_FREQUENCY_ATTRIBUTE, s});
            }
        } catch (NumberFormatException nfEx ) {
            throw new BridgeException
                (ctx, e, nfEx, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] {SVG_BASE_FREQUENCY_ATTRIBUTE, s});
        }
        if (v[0] < 0 || v[1] < 0) {
            throw new BridgeException
                (ctx, e, ERR_ATTRIBUTE_VALUE_MALFORMED,
                 new Object[] {SVG_BASE_FREQUENCY_ATTRIBUTE, s});
        }
        return v;
    }
    protected static boolean convertStitchTiles(Element e, BridgeContext ctx) {
        String s = e.getAttributeNS(null, SVG_STITCH_TILES_ATTRIBUTE);
        if (s.length() == 0) {
            return false;
        }
        if (SVG_STITCH_VALUE.equals(s)) {
            return true;
        }
        if (SVG_NO_STITCH_VALUE.equals(s)) {
            return false;
        }
        throw new BridgeException(ctx, e, ERR_ATTRIBUTE_VALUE_MALFORMED,
                                  new Object[] {SVG_STITCH_TILES_ATTRIBUTE, s});
    }
    protected static boolean convertType(Element e, BridgeContext ctx) {
        String s = e.getAttributeNS(null, SVG_TYPE_ATTRIBUTE);
        if (s.length() == 0) {
            return false;
        }
        if (SVG_FRACTAL_NOISE_VALUE.equals(s)) {
            return true;
        }
        if (SVG_TURBULENCE_VALUE.equals(s)) {
            return false;
        }
        throw new BridgeException(ctx, e, ERR_ATTRIBUTE_VALUE_MALFORMED,
                                  new Object[] {SVG_TYPE_ATTRIBUTE, s});
    }
}
