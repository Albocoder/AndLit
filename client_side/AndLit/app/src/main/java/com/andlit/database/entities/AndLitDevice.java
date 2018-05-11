package com.andlit.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "andlit_devices")
public class AndLitDevice {
    @PrimaryKey(autoGenerate=true)
    public Long id;
    @NonNull
    public String username;
    @NonNull
    public String password;
    public String ip;

    public AndLitDevice(@NonNull String username, @NonNull String password, String ip) {
        this.username = username;
        this.password = password;
        this.ip = ip;
    }
}
