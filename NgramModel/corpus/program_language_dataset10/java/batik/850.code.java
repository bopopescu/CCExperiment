package org.apache.batik.ext.awt.image.renderable;
import java.awt.geom.Rectangle2D;
import org.apache.batik.ext.awt.image.Light;
public interface SpecularLightingRable extends FilterColorInterpolation {
    Filter getSource();
    void setSource(Filter src);
    Light getLight();
    void setLight(Light light);
    double getSurfaceScale();
    void setSurfaceScale(double surfaceScale);
    double getKs();
    void setKs(double ks);
    double getSpecularExponent();
    void setSpecularExponent(double specularExponent);
    Rectangle2D getLitRegion();
    void setLitRegion(Rectangle2D litRegion);
    double [] getKernelUnitLength();
    void setKernelUnitLength(double [] kernelUnitLength);
}
