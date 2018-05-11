package com.andlit.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "andlit_devices")
public class AndLitDevice {
    @PrimaryKey @NonNull
    public String mac;
    @NonNull
    public String name;
    @NonNull
    public String username;
    @NonNull
    public String password;

    public AndLitDevice(@NonNull String mac, @NonNull String name, @NonNull String username,
                        @NonNull String password) {
        this.mac = mac;
        this.name = name;
        this.username = username;
        this.password = password;
    }
}
