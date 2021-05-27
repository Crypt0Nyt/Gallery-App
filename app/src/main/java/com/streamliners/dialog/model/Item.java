package com.streamliners.dialog.model;


import android.graphics.Bitmap;

public class Item {
    public String url;
    public int color;
    public String label;


    public Item(String url, int color, String label) {
        this.url = url;
        this.color = color;
        this.label = label;
    }

    public Item(Bitmap bitmapFromString, int anInt, String string) {
    }
}
