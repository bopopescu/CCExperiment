package org.apache.batik.ext.awt;
import java.awt.Color;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
public final class LinearGradientPaint extends MultipleGradientPaint {
    private Point2D start, end;   
    public LinearGradientPaint(float startX, float startY, 
                               float endX, float endY, 
                               float[] fractions, Color[] colors) {
        this(new Point2D.Float(startX, startY),
             new Point2D.Float(endX, endY), 
             fractions, 
             colors,
             NO_CYCLE,
             SRGB);
    }
    public LinearGradientPaint(float startX, float startY, 
                               float endX, float endY, 
                               float[] fractions, Color[] colors, 
                               CycleMethodEnum cycleMethod) {
        this(new Point2D.Float(startX, startY), 
             new Point2D.Float(endX, endY), 
             fractions, 
             colors,
             cycleMethod,
             SRGB);
    }
    public LinearGradientPaint(Point2D start, Point2D end, float[] fractions,
                               Color[] colors) {
        this(start, end, fractions, colors, NO_CYCLE, SRGB);
    }
    public LinearGradientPaint(Point2D start, Point2D end, float[] fractions,
                               Color[] colors, 
                               CycleMethodEnum cycleMethod, 
                               ColorSpaceEnum colorSpace) {
        this(start, end, fractions, colors, cycleMethod, colorSpace, 
             new AffineTransform());
    }
    public LinearGradientPaint(Point2D start, Point2D end, float[] fractions,
                               Color[] colors,
                               CycleMethodEnum cycleMethod, 
                               ColorSpaceEnum colorSpace, 
                               AffineTransform gradientTransform) {
        super(fractions, colors, cycleMethod, colorSpace, gradientTransform);
        if (start == null || end == null) {
            throw new NullPointerException("Start and end points must be" +
                                           "non-null");
        }
        if (start.equals(end)) {
            throw new IllegalArgumentException("Start point cannot equal" +
                                               "endpoint");
        }
        this.start = (Point2D)start.clone();
        this.end = (Point2D)end.clone();
    }
    public PaintContext createContext(ColorModel cm,
                                      Rectangle deviceBounds,
                                      Rectangle2D userBounds,
                                      AffineTransform transform,
                                      RenderingHints hints) {
        transform = new AffineTransform(transform);
        transform.concatenate(gradientTransform); 
        try {
            return new LinearGradientPaintContext(cm, 
                                                  deviceBounds,
                                                  userBounds, 
                                                  transform,
                                                  hints,
                                                  start, 
                                                  end,
                                                  fractions,
                                                  this.getColors(),
                                                  cycleMethod,
                                                  colorSpace);
        }
        catch(NoninvertibleTransformException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("transform should be" + 
                                               "invertible");
        }
    }
    public Point2D getStartPoint() {
        return new Point2D.Double(start.getX(), start.getY());
    }
    public Point2D getEndPoint() {
        return new Point2D.Double(end.getX(), end.getY());
    }
}
