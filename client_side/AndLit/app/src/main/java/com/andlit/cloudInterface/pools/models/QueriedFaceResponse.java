package com.andlit.cloudInterface.pools.models;

public class QueriedFaceResponse {
    private String name;
    private String last;
    private double distance;

    public QueriedFaceResponse(String n,String l,double dist){
        name = n;
        last = l;
        distance = dist;
    }

    public String getName() {
        return name;
    }

    public String getLast() {
        return last;
    }

    public double getDistance() {
        return distance;
    }
}
