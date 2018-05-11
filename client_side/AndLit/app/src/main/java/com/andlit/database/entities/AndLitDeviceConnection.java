package com.andlit.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "andlit_device_connection",foreignKeys = {
        @ForeignKey(entity = AndLitDevice.class, childColumns = "id",
                parentColumns = "id",onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = WifiCredentials.class, childColumns = "ssid",
                parentColumns = "ssid",onDelete = ForeignKey.CASCADE)},
        primaryKeys = {"id","ssid"},indices = {@Index("ssid")})
public class AndLitDeviceConnection {
    @NonNull
    public Long id;
    @NonNull
    public String ssid;

    public AndLitDeviceConnection (Long id,@NonNull String ssid){
        this.id = id;
        this.ssid = ssid;
    }
}
