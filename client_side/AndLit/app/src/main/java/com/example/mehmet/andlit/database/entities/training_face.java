package com.example.mehmet.andlit.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "training_faces", indices = {@Index(value = "label")},
        foreignKeys = {@ForeignKey(entity = KnownPPL.class,
                childColumns = "label", parentColumns = "id", onDelete = ForeignKey.CASCADE)})
public class training_face {
    public Integer label;
    @NonNull
    public String path;
    @PrimaryKey @NonNull
    public String hash;

    public training_face(int label, String path, String hash) {
        this.label = label;
        this.path = path;
        this.hash = hash;
    }

    @Override
    public String toString(){
        return "Training Face: < `"+label+"`, `"+ path+"`, `"+hash+"` >";
    }
}
