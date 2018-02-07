package albocoder.github.com.facedetector.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

import albocoder.github.com.facedetector.database.converters.DateConverter;

@Entity(tableName = "detected_faces")
public class detected_face {
    public Integer id;
    @PrimaryKey @NonNull
    public String path;
    public String hash;
    public Long date_taken;

    public detected_face(int id,String path,String hash,long date_taken){
        this.id = id;
        this.path = path;
        this.hash = hash;
        this.date_taken = date_taken;
    }

    public detected_face(int id,String path,String hash,Date date_taken){
        this(id,path,hash, DateConverter.toTimestamp(date_taken));
    }

    public String toString(){
        return "Detected face: < `"+id+"`, `"+path+"`, `"+hash+"`, `"+date_taken+"` >";
    }
}
