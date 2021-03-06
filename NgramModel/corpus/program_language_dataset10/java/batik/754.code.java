package org.apache.batik.ext.awt.image;
public class GammaTransfer implements TransferFunction {
    public byte [] lutData;
    public float amplitude;
    public float exponent;
    public float offset;
    public GammaTransfer(float amplitude, float exponent, float offset){
        this.amplitude = amplitude;
        this.exponent = exponent;
        this.offset = offset;
    }
    private void buildLutData(){
        lutData = new byte [256];
        int j, v;
        for (j=0; j<=255; j++){
            v = (int)Math.round(255*(amplitude*Math.pow(j/255f, exponent)+offset));
            if(v > 255){
                v = (byte)0xff;
            }
            else if(v < 0){
                v = (byte)0x00;
            }
            lutData[j] = (byte)(v & 0xff);
        }
    }
    public byte [] getLookupTable(){
        buildLutData();
        return lutData;
    }
}
