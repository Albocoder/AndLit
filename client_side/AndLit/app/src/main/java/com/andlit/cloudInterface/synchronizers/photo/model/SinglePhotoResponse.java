package com.andlit.cloudInterface.synchronizers.photo.model;

import android.graphics.Bitmap;

public class SinglePhotoResponse {


    private Bitmap bmp;
    private String hash;
    private boolean isTraining;

    public SinglePhotoResponse(String h, Bitmap b, boolean t) {
        isTraining = t;
        hash = h;
        bmp = b;
    }

    public SinglePhotoResponse(String h, boolean t) {
        this(h,null,t);
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
}
