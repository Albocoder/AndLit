package com.andlit.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "settings")
public class Setting {
    @PrimaryKey @NonNull
    public String key;
    public String value;

    public Setting(@NonNull String key, String value) {
        this.key = key;
        this.value = value;
    }
}
