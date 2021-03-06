package org.apache.batik.svggen;
import java.awt.Rectangle;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.LookupOp;
import java.awt.image.RescaleOp;
import java.util.LinkedList;
import java.util.List;
public class SVGBufferedImageOp extends AbstractSVGFilterConverter {
    private SVGLookupOp svgLookupOp;
    private SVGRescaleOp svgRescaleOp;
    private SVGConvolveOp svgConvolveOp;
    private SVGCustomBufferedImageOp svgCustomBufferedImageOp;
    public SVGBufferedImageOp(SVGGeneratorContext generatorContext) {
        super(generatorContext);
        this.svgLookupOp = new SVGLookupOp(generatorContext);
        this.svgRescaleOp = new SVGRescaleOp(generatorContext);
        this.svgConvolveOp = new SVGConvolveOp(generatorContext);
        this.svgCustomBufferedImageOp =
            new SVGCustomBufferedImageOp(generatorContext);
    }
    public List getDefinitionSet(){
        List filterSet = new LinkedList(svgLookupOp.getDefinitionSet());
        filterSet.addAll(svgRescaleOp.getDefinitionSet());
        filterSet.addAll(svgConvolveOp.getDefinitionSet());
        filterSet.addAll(svgCustomBufferedImageOp.getDefinitionSet());
        return filterSet;
    }
    public SVGLookupOp getLookupOpConverter(){
        return svgLookupOp;
    }
    public SVGRescaleOp getRescaleOpConverter(){
        return svgRescaleOp;
    }
    public SVGConvolveOp getConvolveOpConverter(){
        return svgConvolveOp;
    }
    public SVGCustomBufferedImageOp getCustomBufferedImageOpConverter(){
        return svgCustomBufferedImageOp;
    }
    public SVGFilterDescriptor toSVG(BufferedImageOp op,
                                     Rectangle filterRect){
        SVGFilterDescriptor filterDesc =
            svgCustomBufferedImageOp.toSVG(op, filterRect);
        if(filterDesc == null){
            if(op instanceof LookupOp)
                filterDesc = svgLookupOp.toSVG(op, filterRect);
            else if(op instanceof RescaleOp)
                filterDesc = svgRescaleOp.toSVG(op, filterRect);
            else if(op instanceof ConvolveOp)
                filterDesc = svgConvolveOp.toSVG(op, filterRect);
        }
        return filterDesc;
    }
}
