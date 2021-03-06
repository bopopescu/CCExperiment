package org.apache.batik.svggen;
import java.awt.*;
import java.awt.geom.*;
public class BStroke implements Painter {
    public void paint(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        java.awt.BasicStroke[] strokesWidth = {
            new java.awt.BasicStroke(2.f),
            new java.awt.BasicStroke(4.f),
            new java.awt.BasicStroke(8.f),
            new java.awt.BasicStroke(16.f)
                };
        java.awt.BasicStroke[] strokesCap = {
            new java.awt.BasicStroke(15.f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_BEVEL), 
            new java.awt.BasicStroke(15.f, java.awt.BasicStroke.CAP_SQUARE, java.awt.BasicStroke.JOIN_BEVEL), 
            new java.awt.BasicStroke(15.f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_BEVEL), 
        };
        java.awt.BasicStroke[] strokesJoin = {
            new java.awt.BasicStroke(10.f, java.awt.BasicStroke.CAP_SQUARE, java.awt.BasicStroke.JOIN_BEVEL), 
            new java.awt.BasicStroke(10.f, java.awt.BasicStroke.CAP_SQUARE, java.awt.BasicStroke.JOIN_MITER), 
            new java.awt.BasicStroke(10.f, java.awt.BasicStroke.CAP_SQUARE, java.awt.BasicStroke.JOIN_ROUND), 
        };
        java.awt.BasicStroke[] strokesMiter = {
            new java.awt.BasicStroke(6.f, java.awt.BasicStroke.CAP_SQUARE, java.awt.BasicStroke.JOIN_MITER, 1),   
            new java.awt.BasicStroke(6.f, java.awt.BasicStroke.CAP_SQUARE, java.awt.BasicStroke.JOIN_MITER, 2f),  
            new java.awt.BasicStroke(6.f, java.awt.BasicStroke.CAP_SQUARE, java.awt.BasicStroke.JOIN_MITER, 10f), 
        };
        java.awt.BasicStroke[] strokesDash = {
            new java.awt.BasicStroke(8.f,
                            java.awt.BasicStroke.CAP_BUTT,
                            java.awt.BasicStroke.JOIN_BEVEL,
                            8.f,
                            new float[]{ 6.f, 6.f },
                            0.f),
            new java.awt.BasicStroke(8.f,
                            java.awt.BasicStroke.CAP_BUTT,
                            java.awt.BasicStroke.JOIN_BEVEL,
                            8.f,
                            new float[]{ 10.f, 4.f },
                            0.f),
            new java.awt.BasicStroke(8.f,
                            java.awt.BasicStroke.CAP_BUTT,
                            java.awt.BasicStroke.JOIN_BEVEL,
                            8.f,
                            new float[]{ 4.f, 4.f, 10.f, 4.f },
                            0.f),
            new java.awt.BasicStroke(8.f,
                            java.awt.BasicStroke.CAP_BUTT,
                            java.awt.BasicStroke.JOIN_BEVEL,
                            8.f,
                            new float[]{ 4.f, 4.f, 10.f, 4.f },
                            4.f)
                };
        java.awt.geom.AffineTransform defaultTransform = g.getTransform();
        g.setPaint(Color.black);
        g.drawString("Varying width", 10, 10);
        for(int i=0; i<strokesWidth.length; i++){
            g.setStroke(strokesWidth[i]);
            g.drawLine(10, 30, 10, 80);
            g.translate(20, 0);
        }
        g.setTransform(defaultTransform);
        g.translate(0, 120);
        g.drawString("Varying end caps", 10, 10);
        for(int i=0; i<strokesCap.length; i++){
            g.setStroke(strokesCap[i]);
            g.drawLine(15, 30, 15, 80);
            g.translate(30, 0);
        }
        GeneralPath needle = new GeneralPath();
        needle.moveTo(0, 60);
        needle.lineTo(10, 20);
        needle.lineTo(20, 60);
        g.setTransform(defaultTransform);
        g.translate(0, 240);
        g.drawString("Varying line joins", 10, 10);
        g.translate(20, 20);
        for(int i=0; i<strokesJoin.length; i++){
            g.setStroke(strokesJoin[i]);
            g.draw(needle);
            g.translate(35, 0);
        }
        g.setTransform(defaultTransform);
        g.translate(150, 120);
        GeneralPath miterShape = new GeneralPath();
        miterShape.moveTo(0, 0);
        miterShape.lineTo(30, 0);
        miterShape.lineTo(30, 60); 
        miterShape.lineTo(0, 30); 
        g.drawString("Varying miter limit", 10, 10);
        g.translate(10, 30);
        for(int i=0; i<strokesMiter.length; i++){
            g.setStroke(strokesMiter[i]);
            g.draw(miterShape);
            g.translate(40, 0);
        }
        g.setTransform(defaultTransform);
        g.translate(150, 0);
        g.drawString("Varying dash patterns", 10, 10);
        g.translate(20, 0);
        for(int i=0; i<strokesDash.length; i++){
            g.setStroke(strokesDash[i]);
            g.drawLine(10, 20, 10, 80);
            g.translate(20, 0);
        }
    }
}
