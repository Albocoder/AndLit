package com.andlit.cloudInterface.pools.models;

public class PoolMember {
    private String username;
    private long id;

    public PoolMember(long id,String un){
        username = un;
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public long getId() {
        return id;
    }
}
