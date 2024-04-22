package com.dvabk.inpaintingapp;

import android.graphics.Path;

public class Stroke {

    public int color;

    public int strokeWidth;

    public Path path;

    public Stroke(int color, int strokeWidth, Path path) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }
}