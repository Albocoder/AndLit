package com.andlit.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "andlit_device_connection",foreignKeys = {
        @ForeignKey(entity = AndLitDevice.class, childColumns = "mac",
                parentColumns = "mac",onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = WifiCredentials.class, childColumns = "ssid",
                parentColumns = "ssid",onDelete = ForeignKey.CASCADE)},
        primaryKeys = {"mac","ssid"},indices = {@Index("ssid")})
public class AndLitDeviceConnection {
    @NonNull
    public String mac;
    @NonNull
    public String ssid;
    public String ip;

    public AndLitDeviceConnection (@NonNull String mac, @NonNull String ssid, String ip){
        this.mac = mac;
        this.ssid = ssid;
        this.ip = ip;
    }
}
