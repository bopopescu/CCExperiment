package org.apache.tools.ant.types.optional.image;
import javax.media.jai.PlanarImage;
public abstract class TransformOperation extends ImageOperation {
    public abstract PlanarImage executeTransformOperation(PlanarImage img);
    public void addRectangle(Rectangle instr) {
        instructions.add(instr);
    }
}
