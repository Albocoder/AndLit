package com.andlit.cloudInterface.synchronizers.photo.model;

import android.graphics.Bitmap;

public class SinglePhotoResponse {


    private Bitmap bmp;
    private String hash;
    private boolean isTraining;
    private int sizeInBytes;

    public SinglePhotoResponse(String h, Bitmap b, boolean t,int s) {
        isTraining = t;
        hash = h;
        bmp = b;
        sizeInBytes = s;
    }

    public SinglePhotoResponse(String h, boolean t,int s) {
        this(h,null,t,s);
    }

    public String getHash() {
        return hash;
    }

    public Bitmap getBmp() {
        return bmp;
    }

    public boolean isTraining() {
        return isTraining;
    }

    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }

    public int getSize() {
        return sizeInBytes;
    }
}
