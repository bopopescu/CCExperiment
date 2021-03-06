package org.apache.batik.ext.awt.image.renderable;
import org.apache.batik.ext.awt.image.ComponentTransferFunction;
public interface ComponentTransferRable extends FilterColorInterpolation {
    Filter getSource();
    void setSource(Filter src);
    ComponentTransferFunction getAlphaFunction();
    void setAlphaFunction(ComponentTransferFunction alphaFunction);
    ComponentTransferFunction getRedFunction();
    void setRedFunction(ComponentTransferFunction redFunction);
    ComponentTransferFunction getGreenFunction();
    void setGreenFunction(ComponentTransferFunction greenFunction);
    ComponentTransferFunction getBlueFunction();
    void setBlueFunction(ComponentTransferFunction blueFunction);
}
