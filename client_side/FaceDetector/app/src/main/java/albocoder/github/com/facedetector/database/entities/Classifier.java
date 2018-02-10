package albocoder.github.com.facedetector.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

import albocoder.github.com.facedetector.database.converters.DateConverter;

@Entity(tableName = "classifier")
public class Classifier {
    // fields
    @PrimaryKey @NonNull
    public String path;
    public String hash;
    public Long last_update;
    public Integer num_recogn;
    public Integer num_inst_trained;
    public Double avgCorrectThresh;

    // constructor
    public Classifier(String path,String hash,Date last_update,int num_recogn,int num_inst_trained,double avgCorrectThresh){
        this(path,hash,DateConverter.toTimestamp(last_update),num_recogn,num_inst_trained,avgCorrectThresh);
    }

    public Classifier(String path,String hash,long last_update,int num_recogn, int num_inst_trained,double avgCorrectThresh){
        this.path = path;
        this.hash = hash;
        this.last_update = last_update;
        this.num_recogn = num_recogn;
        this.num_inst_trained = num_inst_trained;
        this.avgCorrectThresh = avgCorrectThresh;
    }

    public Classifier(Classifier c2) {
        this.path = c2.path;
        this.hash = c2.hash;
        this.num_recogn = c2.num_recogn;
        this.last_update = c2.last_update;
        this.num_inst_trained = c2.num_inst_trained;
        this.avgCorrectThresh = c2.avgCorrectThresh;
    }

    @Override
    public String toString(){
        return "Classifier Entry: <`"+path+"`, `"+hash+"`, `"+last_update+"`, `"+num_recogn+"`>";
    }
}
