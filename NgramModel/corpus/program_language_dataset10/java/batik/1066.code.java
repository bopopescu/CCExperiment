package org.apache.batik.parser;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.io.Reader;
public class AWTPolylineProducer implements PointsHandler, ShapeProducer {
    protected GeneralPath path;
    protected boolean newPath;
    protected int windingRule;
    public static Shape createShape(Reader r, int wr)
        throws IOException,
               ParseException {
        PointsParser p = new PointsParser();
        AWTPolylineProducer ph = new AWTPolylineProducer();
        ph.setWindingRule(wr);
        p.setPointsHandler(ph);
        p.parse(r);
        return ph.getShape();
    }
    public void setWindingRule(int i) {
        windingRule = i;
    }
    public int getWindingRule() {
        return windingRule;
    }
    public Shape getShape() {
        return path;
    }
    public void startPoints() throws ParseException {
        path = new GeneralPath(windingRule);
        newPath = true;
    }
    public void point(float x, float y) throws ParseException {
        if (newPath) {
            newPath = false;
            path.moveTo(x, y);
        } else {
            path.lineTo(x, y);
        }
    }
    public void endPoints() throws ParseException {
    }
}
