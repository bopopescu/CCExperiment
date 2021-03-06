package org.apache.batik.ext.awt.image;
public class ConcreteComponentTransferFunction implements ComponentTransferFunction {
    private int type;
    private float slope;
    private float[] tableValues;
    private float intercept;
    private float amplitude;
    private float exponent;
    private float offset;
    private ConcreteComponentTransferFunction(){
    }
    public static ComponentTransferFunction getIdentityTransfer(){
        ConcreteComponentTransferFunction f = new ConcreteComponentTransferFunction();
        f.type = IDENTITY;
        return f;
    }
    public static ComponentTransferFunction
        getTableTransfer(float[] tableValues){
        ConcreteComponentTransferFunction f = new ConcreteComponentTransferFunction();
        f.type = TABLE;
        if(tableValues == null){
            throw new IllegalArgumentException();
        }
        if(tableValues.length < 2){
            throw new IllegalArgumentException();
        }
        f.tableValues = new float[tableValues.length];
        System.arraycopy(tableValues, 0,
                         f.tableValues, 0,
                         tableValues.length);
        return f;
    }
    public static ComponentTransferFunction
        getDiscreteTransfer(float[] tableValues){
        ConcreteComponentTransferFunction f = new ConcreteComponentTransferFunction();
        f.type = DISCRETE;
        if(tableValues == null){
            throw new IllegalArgumentException();
        }
        if(tableValues.length < 2){
            throw new IllegalArgumentException();
        }
        f.tableValues = new float[tableValues.length];
        System.arraycopy(tableValues, 0,
                         f.tableValues, 0,
                         tableValues.length);
        return f;
    }
    public static ComponentTransferFunction
        getLinearTransfer(float slope, float intercept){
        ConcreteComponentTransferFunction f = new ConcreteComponentTransferFunction();
        f.type = LINEAR;
        f.slope = slope;
        f.intercept = intercept;
        return f;
    }
    public static ComponentTransferFunction
        getGammaTransfer(float amplitude,
                         float exponent,
                         float offset){
        ConcreteComponentTransferFunction f = new ConcreteComponentTransferFunction();
        f.type = GAMMA;
        f.amplitude = amplitude;
        f.exponent = exponent;
        f.offset = offset;
        return f;
    }
    public int getType(){
        return type;
    }
    public float getSlope(){
        return slope;
    }
    public float[] getTableValues(){
        return tableValues;
    }
    public float getIntercept(){
        return intercept;
    }
    public float getAmplitude(){
        return amplitude;
    }
    public float getExponent(){
        return exponent;
    }
    public float getOffset(){
        return offset;
    }
}
