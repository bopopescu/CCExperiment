package org.apache.batik.gvt.font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphJustificationInfo;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.util.Iterator;
import java.util.List;
import org.apache.batik.gvt.text.AttributedCharacterSpanIterator;
public class MultiGlyphVector implements GVTGlyphVector {
    GVTGlyphVector [] gvs;
    int [] nGlyphs;
    int [] off;
    int nGlyph;
    public MultiGlyphVector(List gvs) {
        int nSlots = gvs.size();
        this.gvs     = new GVTGlyphVector[ nSlots ];
        this.nGlyphs = new int[ nSlots ];
        this.off     = new int[ nSlots ];
        Iterator iter = gvs.iterator();
        int i=0;
        while (iter.hasNext()) {
            off[i]      = nGlyph;
            GVTGlyphVector gv = (GVTGlyphVector)iter.next();
            this.gvs[i] = gv;
            nGlyphs[i]  = gv.getNumGlyphs();
            nGlyph     += nGlyphs[i];
            i++;
        }
        nGlyphs[i-1]++;
    }
    public int getNumGlyphs() {
        return nGlyph;
    }
    int getGVIdx(int glyphIdx) {
        if (glyphIdx > nGlyph) return -1;
        if (glyphIdx == nGlyph) return gvs.length-1;
        for (int i=0; i<nGlyphs.length; i++)
            if (glyphIdx-off[i] < nGlyphs[i]) return i;
        return -1;
    }
    public GVTFont getFont() {
        throw new IllegalArgumentException("Can't be correctly Implemented");
    }
    public FontRenderContext getFontRenderContext() {
        return gvs[0].getFontRenderContext();
    }
    public int getGlyphCode(int glyphIndex) {
        int idx = getGVIdx(glyphIndex);
        return gvs[idx].getGlyphCode(glyphIndex-off[idx]);
    }
    public GlyphJustificationInfo getGlyphJustificationInfo(int glyphIndex) {
        int idx = getGVIdx(glyphIndex);
        return gvs[idx].getGlyphJustificationInfo(glyphIndex-off[idx]);
    }
    public Shape getGlyphLogicalBounds(int glyphIndex) {
        int idx = getGVIdx(glyphIndex);
        return gvs[idx].getGlyphLogicalBounds(glyphIndex-off[idx]);
    }
    public GVTGlyphMetrics getGlyphMetrics(int glyphIndex) {
        int idx = getGVIdx(glyphIndex);
        return gvs[idx].getGlyphMetrics(glyphIndex-off[idx]);
    }
    public Shape getGlyphOutline(int glyphIndex) {
        int idx = getGVIdx(glyphIndex);
        return gvs[idx].getGlyphOutline(glyphIndex-off[idx]);
    }
    public Rectangle2D getGlyphCellBounds(int glyphIndex) {
        return getGlyphLogicalBounds(glyphIndex).getBounds2D();
    }
    public Point2D getGlyphPosition(int glyphIndex) {
        int idx = getGVIdx(glyphIndex);
        return gvs[idx].getGlyphPosition(glyphIndex-off[idx]);
    }
    public AffineTransform getGlyphTransform(int glyphIndex) {
        int idx = getGVIdx(glyphIndex);
        return gvs[idx].getGlyphTransform(glyphIndex-off[idx]);
    }
    public Shape getGlyphVisualBounds(int glyphIndex) {
        int idx = getGVIdx(glyphIndex);
        return gvs[idx].getGlyphVisualBounds(glyphIndex-off[idx]);
    }
    public void setGlyphPosition(int glyphIndex, Point2D newPos) {
        int idx = getGVIdx(glyphIndex);
        gvs[idx].setGlyphPosition(glyphIndex-off[idx], newPos);
    }
    public void setGlyphTransform(int glyphIndex, AffineTransform newTX) {
        int idx = getGVIdx(glyphIndex);
        gvs[idx].setGlyphTransform(glyphIndex-off[idx], newTX);
    }
    public void setGlyphVisible(int glyphIndex, boolean visible) {
        int idx = getGVIdx(glyphIndex);
        gvs[idx].setGlyphVisible(glyphIndex-off[idx], visible);
    }
    public boolean isGlyphVisible(int glyphIndex) {
        int idx = getGVIdx(glyphIndex);
        return gvs[idx].isGlyphVisible(glyphIndex-off[idx]);
    }
    public int[] getGlyphCodes(int beginGlyphIndex, int numEntries,
                        int[] codeReturn) {
        int [] ret = codeReturn;
        if (ret == null)
            ret = new int[numEntries];
        int [] tmp = null;
        int gvIdx = getGVIdx(beginGlyphIndex);
        int gi    = beginGlyphIndex-off[gvIdx];
        int i=0;
        GVTGlyphVector gv;
        while (numEntries != 0) {
            int len = numEntries;
            if (gi+len > nGlyphs[gvIdx])
                len = nGlyphs[gvIdx]-gi;
            gv = gvs[gvIdx];
            if (i == 0) {
                gv.getGlyphCodes(gi, len, ret);
            } else {
                if ((tmp == null) || (tmp.length < len))
                    tmp = new int[len];
                gv.getGlyphCodes(gi, len, tmp);
                System.arraycopy( tmp, 0, ret, i, len );
            }
            gi=0;
            gvIdx++;
            numEntries -= len;
            i+=len;
        }
        return ret;
    }
    public float[] getGlyphPositions(int beginGlyphIndex,
                              int numEntries,
                              float[] positionReturn) {
        float [] ret = positionReturn;
        if (ret == null)
            ret = new float[numEntries*2];
        float [] tmp = null;
        int gvIdx = getGVIdx(beginGlyphIndex);
        int gi    = beginGlyphIndex-off[gvIdx];
        int i=0;
        GVTGlyphVector gv;
        while (numEntries != 0) {
            int len = numEntries;
            if (gi+len > nGlyphs[gvIdx])
                len = nGlyphs[gvIdx]-gi;
            gv = gvs[gvIdx];
            if (i == 0) {
                gv.getGlyphPositions(gi, len, ret);
            } else {
                if ((tmp == null) || (tmp.length < len*2))
                    tmp = new float[len*2];
                gv.getGlyphPositions(gi, len, tmp);
                System.arraycopy( tmp, 0, ret, i, len * 2 );
            }
            gi=0;
            gvIdx++;
            numEntries -= len;
            i+=len*2;
        }
        return ret;
    }
    public Rectangle2D getLogicalBounds() {
        Rectangle2D ret = null;
        for (int idx=0; idx<gvs.length; idx++) {
            Rectangle2D b = gvs[idx].getLogicalBounds();
            if (ret == null) ret = b;
            else ret.add( b );   
        }
        return ret;
    }
    public Shape getOutline() {
        GeneralPath ret = null;
        for (int idx=0; idx<gvs.length; idx++) {
            Shape s = gvs[idx].getOutline();
            if (ret == null) ret = new GeneralPath(s);
            else ret.append(s, false);
        }
        return ret;
    }
    public Shape getOutline(float x, float y) {
        Shape outline = getOutline();
        AffineTransform tr = AffineTransform.getTranslateInstance(x,y);
        outline = tr.createTransformedShape(outline);
        return outline;
    }
    public Rectangle2D getBounds2D(AttributedCharacterIterator aci) {
        Rectangle2D ret = null;
        int begin = aci.getBeginIndex();
        for (int idx=0; idx<gvs.length; idx++) {
            GVTGlyphVector gv = gvs[idx];
            int end = gv.getCharacterCount(0, gv.getNumGlyphs())+1;
            Rectangle2D b = gvs[idx].getBounds2D
                (new AttributedCharacterSpanIterator(aci, begin, end));
            if (ret == null) ret = b;
            else ret.add(b);
            begin = end;
        }
        return ret;
    }
    public Rectangle2D getGeometricBounds() {
        Rectangle2D ret = null;
        for (int idx=0; idx<gvs.length; idx++) {
            Rectangle2D b = gvs[idx].getGeometricBounds();
            if (ret == null) ret = b;
            else ret.add(b);
        }
        return ret;
    }
    public void performDefaultLayout() {
        for (int idx=0; idx<gvs.length; idx++) {
            gvs[idx].performDefaultLayout();
        }
    }
    public int getCharacterCount(int startGlyphIndex, int endGlyphIndex) {
        int idx1 = getGVIdx(startGlyphIndex);
        int idx2 = getGVIdx(endGlyphIndex);
        int ret=0;
        for (int idx=idx1; idx<=idx2; idx++) {
            int gi1 = startGlyphIndex-off[idx];
            int gi2 = endGlyphIndex-off[idx];
            if (gi2 >= nGlyphs[idx]) {
                gi2 = nGlyphs[idx]-1;
            }
            ret += gvs[idx].getCharacterCount(gi1, gi2);
            startGlyphIndex += (gi2-gi1+1);
        }
        return ret;
    }
    public void draw(Graphics2D g2d,
              AttributedCharacterIterator aci) {
        int begin = aci.getBeginIndex();
        for (int idx=0; idx<gvs.length; idx++) {
            GVTGlyphVector gv = gvs[idx];
            int end = gv.getCharacterCount(0, gv.getNumGlyphs())+1;
            gv.draw(g2d, new AttributedCharacterSpanIterator(aci, begin, end));
            begin = end;
        }
    }
}
