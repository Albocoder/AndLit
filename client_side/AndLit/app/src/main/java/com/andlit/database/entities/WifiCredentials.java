package com.andlit.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "wifi_credentials")
public class WifiCredentials {
    @PrimaryKey @NonNull
    public String ssid;
    @NonNull
    public String password;

    public WifiCredentials(@NonNull String ssid, @NonNull String password){
        this.ssid = ssid;
        this.password = password;
    }
}
