package albocoder.github.com.facedetector.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import org.opencv.face.LBPHFaceRecognizer;

import java.util.Date;

import albocoder.github.com.facedetector.database.converters.DateConverter;

@Entity(tableName = "classifier")
public class Classifier {
    // fields
    @PrimaryKey @NonNull
    public String path;
    public String hash;
    public long last_update;
    public int num_recogn;

    @Ignore
    private LBPHFaceRecognizer recognizerModel;

    // constructor
    public Classifier(String path,String hash,Date last_update,int num_recogn){
        this(path,hash,DateConverter.toTimestamp(last_update),num_recogn);
    }

    public Classifier(String path,String hash,long last_update,int num_recogn){
        this.path = path;
        this.hash = hash;
        this.last_update = last_update;
        this.num_recogn = num_recogn;
        // createModel() -> recognizerModel = load the recognizer model from file system
    }

    public LBPHFaceRecognizer getRecognizerModel(){ return recognizerModel; }
}
