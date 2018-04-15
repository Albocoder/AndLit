package com.andlit.cloudInterface.synchronizers.database.model;

// I know you are cringing at this class but this is developed keeping in mind
// that it may hold more information in the future given that server does
public class DatabaseStats {
    private long size;

    public DatabaseStats(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }
}
