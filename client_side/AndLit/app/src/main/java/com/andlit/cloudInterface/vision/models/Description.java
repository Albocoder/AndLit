package com.andlit.cloudInterface.vision.models;

public class Description {
    String []keywords;
    public Description(String[] k){
        keywords = k;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("The photo is described by keywords like");
        for(String s:keywords) {
            sb.append(", ");
            sb.append(s);
        }
        sb.append(".");
        return sb.toString();
    }
}
