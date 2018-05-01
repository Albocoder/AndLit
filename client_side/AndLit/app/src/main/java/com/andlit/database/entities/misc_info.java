package com.andlit.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.support.annotation.NonNull;

@Entity(primaryKeys={"id","key"},foreignKeys = {@ForeignKey(entity = KnownPPL.class,
        childColumns = "id",parentColumns = "id",onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = {"id","key"},unique = true)})
public class misc_info {
    @NonNull
    public Integer id;
    @NonNull
    public String key;
    public String desc;

    public misc_info(int id,String key,String desc){
        this.id = id;
        this.key = key;
        this.desc = desc;
    }
}
