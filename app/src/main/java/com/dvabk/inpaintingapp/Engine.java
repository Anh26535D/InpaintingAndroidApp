package com.dvabk.inpaintingapp;

public class Engine {
    static {
       System.loadLibrary("inpaintingapp");
    }

    public  static native void inverseColor(int img[], int mask[]);
}
