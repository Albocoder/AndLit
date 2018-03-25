package com.andlit.CloudInterface.Vision.models;

import java.util.List;

public class Description {
    List<String> keywords;
    public Description(List<String> k){
        keywords = k;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("The photo is described by keywords like");
        for(String s:keywords) {
            sb.append(", ");
            sb.append(s);
        }
        sb.append(".");
        return sb.toString();
    }
}
