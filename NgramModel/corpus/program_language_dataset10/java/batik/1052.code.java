package org.apache.batik.gvt.text;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
public interface TextLayoutFactory {
    TextSpanLayout createTextLayout(AttributedCharacterIterator aci,
                                    int [] charMap,
                                    Point2D offset,
                                    FontRenderContext frc);
}
