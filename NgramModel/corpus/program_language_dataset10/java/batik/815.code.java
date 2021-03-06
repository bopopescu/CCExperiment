package org.apache.batik.ext.awt.image.renderable;
import java.awt.Shape;
public interface ClipRable extends Filter {
    void setUseAntialiasedClip(boolean useAA);
    boolean getUseAntialiasedClip();
      void setSource(Filter src);
      Filter getSource();
    void setClipPath(Shape clipPath);
      Shape getClipPath();
}
