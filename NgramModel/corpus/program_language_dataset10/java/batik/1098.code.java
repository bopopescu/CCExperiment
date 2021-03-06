package org.apache.batik.parser;
public interface PathHandler {
    void startPath() throws ParseException;
    void endPath() throws ParseException;
    void movetoRel(float x, float y) throws ParseException;
    void movetoAbs(float x, float y) throws ParseException;
    void closePath() throws ParseException;
    void linetoRel(float x, float y) throws ParseException;
    void linetoAbs(float x, float y) throws ParseException;
    void linetoHorizontalRel(float x) throws ParseException;
    void linetoHorizontalAbs(float x) throws ParseException;
    void linetoVerticalRel(float y) throws ParseException;
    void linetoVerticalAbs(float y) throws ParseException;
    void curvetoCubicRel(float x1, float y1, 
                         float x2, float y2, 
                         float x, float y) throws ParseException;
    void curvetoCubicAbs(float x1, float y1, 
                         float x2, float y2, 
                         float x, float y) throws ParseException;
    void curvetoCubicSmoothRel(float x2, float y2, 
                               float x, float y) throws ParseException;
    void curvetoCubicSmoothAbs(float x2, float y2, 
                               float x, float y) throws ParseException;
    void curvetoQuadraticRel(float x1, float y1, 
                             float x, float y) throws ParseException;
    void curvetoQuadraticAbs(float x1, float y1, 
                             float x, float y) throws ParseException;
    void curvetoQuadraticSmoothRel(float x, float y) throws ParseException;
    void curvetoQuadraticSmoothAbs(float x, float y) throws ParseException;
    void arcRel(float rx, float ry, 
                float xAxisRotation, 
                boolean largeArcFlag, boolean sweepFlag, 
                float x, float y) throws ParseException;
    void arcAbs(float rx, float ry, 
                float xAxisRotation, 
                boolean largeArcFlag, boolean sweepFlag, 
                float x, float y) throws ParseException;
}
