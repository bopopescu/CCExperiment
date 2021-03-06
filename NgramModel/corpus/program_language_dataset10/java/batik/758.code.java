package org.apache.batik.ext.awt.image;
public class LinearTransfer implements TransferFunction {
    public byte [] lutData;
    public float slope;
    public float intercept;
    public LinearTransfer(float slope, float intercept){
        this.slope = slope;
        this.intercept = intercept;
    }
    private void buildLutData(){
        lutData = new byte [256];
        int j, value;
        float scaledInt = (intercept*255f)+0.5f;
        for (j=0; j<=255; j++){
            value = (int)(slope*j+scaledInt);
            if(value < 0){
                value = 0;
            }
            else if(value > 255){
                value = 255;
            }
            lutData[j] = (byte)(0xff & value);
        }
    }
    public byte [] getLookupTable(){
        buildLutData();
        return lutData;
    }
}
