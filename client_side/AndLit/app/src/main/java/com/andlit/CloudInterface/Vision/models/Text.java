package com.andlit.CloudInterface.Vision.models;

import org.bytedeco.javacpp.opencv_core;

public class Text {
    private opencv_core.Rect loc;
    private String text;

    public Text(opencv_core.Rect l,String t) {
        loc = l;
        text = t;
    }

    // Getters
    public String getText() { return text; }
    public opencv_core.Rect getLoc() { return loc; }
}
