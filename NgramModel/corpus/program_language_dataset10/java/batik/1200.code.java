package org.apache.batik.svggen;
import java.awt.Polygon;
import java.awt.geom.PathIterator;
import org.w3c.dom.Element;
public class SVGPolygon extends SVGGraphicObjectConverter {
    public SVGPolygon(SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }
    public Element toSVG(Polygon polygon) {
        Element svgPolygon =
            generatorContext.domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                        SVG_POLYGON_TAG);
        StringBuffer points = new StringBuffer(" ");
        PathIterator pi = polygon.getPathIterator(null);
        float[] seg = new float[6];
        while(!pi.isDone()){
            int segType = pi.currentSegment(seg);
            switch(segType){
            case PathIterator.SEG_MOVETO:
                appendPoint(points, seg[0], seg[1]);
                break;
            case PathIterator.SEG_LINETO:
                appendPoint(points, seg[0], seg[1]);
                break;
            case PathIterator.SEG_CLOSE:
                break;
            case PathIterator.SEG_QUADTO:
            case PathIterator.SEG_CUBICTO:
            default:
                throw new Error("invalid segmentType:" + segType );
            }
            pi.next();
        } 
        svgPolygon.setAttributeNS(null,
                                  SVG_POINTS_ATTRIBUTE,
                                  points.substring(0, points.length() - 1));
        return svgPolygon;
    }
    private void appendPoint(StringBuffer points, float x, float y){
        points.append(doubleString(x));
        points.append(SPACE);
        points.append(doubleString(y));
        points.append(SPACE);
    }
}
