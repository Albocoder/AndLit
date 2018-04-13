package com.andlit.cloudInterface.vision.models;

import org.bytedeco.javacpp.opencv_core;

public class Text {

    private opencv_core.Rect loc;
    private String text;
    private opencv_core.Rect ratioedLoc;

    public Text(opencv_core.Rect l,String t) {
        loc = l;
        text = t;
    }

    // Getters
    public String getText() { return text; }
    public opencv_core.Rect getLoc() { return loc; }
    public opencv_core.Rect getRatioedLoc(double widthRatio,double heightRatio) {
        int x = loc.x();
        int y = loc.y();
        int w = loc.width();
        int h = loc.height();

        ratioedLoc = new opencv_core.Rect((int)(x*widthRatio),
                (int)(y*heightRatio),(int)(w*widthRatio),(int)(h*heightRatio));
        return ratioedLoc;
    }
    public opencv_core.Rect getRatioedLoc() { return ratioedLoc; }
}
