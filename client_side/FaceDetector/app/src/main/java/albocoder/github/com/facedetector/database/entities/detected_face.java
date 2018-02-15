package albocoder.github.com.facedetector.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

import albocoder.github.com.facedetector.database.converters.DateConverter;

@Entity(tableName = "detected_faces")
public class detected_face {
    public Integer id;      // this is the manually given label
    @NonNull
    public String path;
    @PrimaryKey @NonNull
    public String hash;
    public Long date_taken;
    public Integer predictedlabel;

    public detected_face(int id,String path,String hash,long date_taken,int predictedlabel){
        this.id = id;
        this.path = path;
        this.hash = hash;
        this.date_taken = date_taken;
        this.predictedlabel = predictedlabel;
    }

    public detected_face(int id,String path,String hash,Date date_taken,int predictedlabel){
        this(id,path,hash, DateConverter.toTimestamp(date_taken),predictedlabel);
    }

    public String toString(){
        return "Detected face: < `"+id+"`, `"+path+"`, `"+hash+"`, `"+date_taken+"`, `"
                +predictedlabel+"` >";
    }
}
