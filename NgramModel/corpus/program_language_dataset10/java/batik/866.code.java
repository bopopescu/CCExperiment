package org.apache.batik.ext.awt.image.rendered;
import java.awt.RenderingHints;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.awt.image.WritableRaster;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.TransferFunction;
public class ComponentTransferRed extends AbstractRed {
    LookupOp operation;
    public ComponentTransferRed(CachableRed src,
                                TransferFunction [] funcs,
                                RenderingHints hints) {
        super(src, src.getBounds(),
              GraphicsUtil.coerceColorModel(src.getColorModel(), false),
              src.getSampleModel(),
              null);
        byte [][] tableData = {funcs[1].getLookupTable(),
                               funcs[2].getLookupTable(),
                               funcs[3].getLookupTable(),
                               funcs[0].getLookupTable()};
        operation  =  new LookupOp(new ByteLookupTable(0, tableData), hints)
            { };
    }
    public WritableRaster copyData(WritableRaster wr){
        CachableRed src = (CachableRed)getSources().get(0);
        wr = src.copyData(wr);
        GraphicsUtil.coerceData(wr, src.getColorModel(), false);
        WritableRaster srcWR = wr.createWritableTranslatedChild(0,0);
        operation.filter(srcWR, srcWR);
        return wr;
    }
}
