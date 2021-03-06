package org.apache.batik.svggen;
import java.awt.Rectangle;
import java.awt.image.BufferedImageOp;
import java.awt.image.RescaleOp;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class SVGRescaleOp extends AbstractSVGFilterConverter {
    public SVGRescaleOp(SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }
    public SVGFilterDescriptor toSVG(BufferedImageOp filter,
                                     Rectangle filterRect) {
        if(filter instanceof RescaleOp)
            return toSVG((RescaleOp)filter);
        else
            return null;
    }
    public SVGFilterDescriptor toSVG(RescaleOp rescaleOp) {
        SVGFilterDescriptor filterDesc =
            (SVGFilterDescriptor)descMap.get(rescaleOp);
        Document domFactory = generatorContext.domFactory;
        if (filterDesc == null) {
            Element filterDef = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                           SVG_FILTER_TAG);
            Element feComponentTransferDef =
                domFactory.createElementNS(SVG_NAMESPACE_URI,
                                           SVG_FE_COMPONENT_TRANSFER_TAG);
            float[] offsets = rescaleOp.getOffsets(null);
            float[] scaleFactors = rescaleOp.getScaleFactors(null);
            if(offsets.length != scaleFactors.length)
                throw new SVGGraphics2DRuntimeException(ERR_SCALE_FACTORS_AND_OFFSETS_MISMATCH);
            if(offsets.length != 1 &&
               offsets.length != 3 &&
               offsets.length != 4)
                throw new SVGGraphics2DRuntimeException(ERR_ILLEGAL_BUFFERED_IMAGE_RESCALE_OP);
            Element feFuncR = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                         SVG_FE_FUNC_R_TAG);
            Element feFuncG = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                         SVG_FE_FUNC_G_TAG);
            Element feFuncB = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                         SVG_FE_FUNC_B_TAG);
            Element feFuncA = null;
            String type = SVG_LINEAR_VALUE;
            if(offsets.length == 1){
                String slope = doubleString(scaleFactors[0]);
                String intercept = doubleString(offsets[0]);
                feFuncR.setAttributeNS(null, SVG_TYPE_ATTRIBUTE, type);
                feFuncG.setAttributeNS(null, SVG_TYPE_ATTRIBUTE, type);
                feFuncB.setAttributeNS(null, SVG_TYPE_ATTRIBUTE, type);
                feFuncR.setAttributeNS(null, SVG_SLOPE_ATTRIBUTE, slope);
                feFuncG.setAttributeNS(null, SVG_SLOPE_ATTRIBUTE, slope);
                feFuncB.setAttributeNS(null, SVG_SLOPE_ATTRIBUTE, slope);
                feFuncR.setAttributeNS(null, SVG_INTERCEPT_ATTRIBUTE, intercept);
                feFuncG.setAttributeNS(null, SVG_INTERCEPT_ATTRIBUTE, intercept);
                feFuncB.setAttributeNS(null, SVG_INTERCEPT_ATTRIBUTE, intercept);
            }
            else if(offsets.length >= 3){
                feFuncR.setAttributeNS(null, SVG_TYPE_ATTRIBUTE, type);
                feFuncG.setAttributeNS(null, SVG_TYPE_ATTRIBUTE, type);
                feFuncB.setAttributeNS(null, SVG_TYPE_ATTRIBUTE, type);
                feFuncR.setAttributeNS(null, SVG_SLOPE_ATTRIBUTE,
                                       doubleString(scaleFactors[0]));
                feFuncG.setAttributeNS(null, SVG_SLOPE_ATTRIBUTE,
                                       doubleString(scaleFactors[1]));
                feFuncB.setAttributeNS(null, SVG_SLOPE_ATTRIBUTE,
                                       doubleString(scaleFactors[2]));
                feFuncR.setAttributeNS(null, SVG_INTERCEPT_ATTRIBUTE,
                                       doubleString(offsets[0]));
                feFuncG.setAttributeNS(null, SVG_INTERCEPT_ATTRIBUTE,
                                       doubleString(offsets[1]));
                feFuncB.setAttributeNS(null, SVG_INTERCEPT_ATTRIBUTE,
                                       doubleString(offsets[2]));
                if(offsets.length == 4){
                    feFuncA = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                         SVG_FE_FUNC_A_TAG);
                    feFuncA.setAttributeNS(null, SVG_TYPE_ATTRIBUTE, type);
                    feFuncA.setAttributeNS(null, SVG_SLOPE_ATTRIBUTE,
                                         doubleString(scaleFactors[3]));
                    feFuncA.setAttributeNS(null, SVG_INTERCEPT_ATTRIBUTE,
                                         doubleString(offsets[3]));
                }
            }
            feComponentTransferDef.appendChild(feFuncR);
            feComponentTransferDef.appendChild(feFuncG);
            feComponentTransferDef.appendChild(feFuncB);
            if(feFuncA != null)
                feComponentTransferDef.appendChild(feFuncA);
            filterDef.appendChild(feComponentTransferDef);
            filterDef.
                setAttributeNS(null, SVG_ID_ATTRIBUTE,
                               generatorContext.idGenerator.
                               generateID(ID_PREFIX_FE_COMPONENT_TRANSFER));
            String filterAttrBuf = URL_PREFIX + SIGN_POUND + filterDef.getAttributeNS(null, SVG_ID_ATTRIBUTE) + URL_SUFFIX;
            filterDesc = new SVGFilterDescriptor(filterAttrBuf, filterDef);
            defSet.add(filterDef);
            descMap.put(rescaleOp, filterDesc);
        }
        return filterDesc;
    }
}
