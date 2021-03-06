package org.apache.batik.svggen;
import java.awt.*;
import java.awt.geom.*;
public class BasicShapes2 implements Painter {
    public void paint(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(Color.black);
        g.drawString("Arc2D", 10, 20);
        Arc2D arc = new Arc2D.Float(10, 30, 50, 40, 0, 270, Arc2D.PIE);
        g.draw(arc);
        g.translate(0, 90);
        g.drawString("Ellipse", 10, 20);
        Ellipse2D ellipse = new Ellipse2D.Double(10, 30, 100, 40);
        g.draw(ellipse);
        g.translate(150, -90);
        g.drawString("GeneralPath, lineTo", 10, 20);
        GeneralPath lineToPath = new GeneralPath();
        lineToPath.moveTo(10, 30);
        lineToPath.lineTo(60, 30);
        lineToPath.lineTo(60, 70);
        lineToPath.lineTo(10, 30);
        lineToPath.closePath();
        g.draw(lineToPath);
        g.translate(0, 90);
        g.drawString("GeneralPath, curveTo", 10, 20);
        GeneralPath curveToPath = new GeneralPath();
        curveToPath.moveTo(10, 30);
        curveToPath.curveTo(35, 10, 35, 50, 60, 30);
        curveToPath.curveTo(80, 55, 40, 55, 60, 80);
        curveToPath.curveTo(35, 60, 35, 100, 10, 80);
        curveToPath.curveTo(-10, 55, 30, 55, 10, 30);
        curveToPath.closePath();
        g.draw(curveToPath);
    }
}
