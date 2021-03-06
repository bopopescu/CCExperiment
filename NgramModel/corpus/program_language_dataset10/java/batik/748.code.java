package org.apache.batik.ext.awt.image;
import java.io.Serializable;
public final class ARGBChannel implements Serializable{
    public static final int CHANNEL_A = 3;
    public static final int CHANNEL_R = 2;
    public static final int CHANNEL_G = 1;
    public static final int CHANNEL_B = 0;
    public static final String RED = "Red";
    public static final String GREEN = "Green";
    public static final String BLUE = "Blue";
    public static final String ALPHA = "Alpha";
    public static final ARGBChannel R 
        = new ARGBChannel(CHANNEL_R, RED);
    public static final ARGBChannel G 
        = new ARGBChannel(CHANNEL_G, GREEN);
    public static final ARGBChannel B 
        = new ARGBChannel(CHANNEL_B, BLUE);
    public static final ARGBChannel A 
        = new ARGBChannel(CHANNEL_A, ALPHA);
    private String desc;
    private int val;
    private ARGBChannel(int val, String desc){
        this.desc = desc;
        this.val = val;
    }
    public String toString(){
        return desc;
    }
    public int toInt(){
        return val;
    }
    public Object readResolve() {
        switch(val){
        case CHANNEL_R:
            return R;
        case CHANNEL_G:
            return G;
        case CHANNEL_B:
            return B;
        case CHANNEL_A:
            return A;
        default:
            throw new Error("Unknown ARGBChannel value");
        }
    }
}
