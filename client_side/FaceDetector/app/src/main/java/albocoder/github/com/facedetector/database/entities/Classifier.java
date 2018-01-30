package albocoder.github.com.facedetector.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;

import org.opencv.face.LBPHFaceRecognizer;

import java.util.Date;

@Entity(tableName = "classifier")
public class Classifier {
    // fields
    public String path;
    public String hash;
    public Date last_update;
    public int num_recogn;

    @Ignore
    private LBPHFaceRecognizer recognizerModel;

    // constructor
    public Classifier(String path,String hash,Date last_update,int num_recogn){
        this.path = path;
        this.hash = hash;
        this.last_update = last_update;
        this.num_recogn = num_recogn;
        // recognizerModel = load the recognizer model from file system
    }
    public LBPHFaceRecognizer getRecognizerModel(){ return recognizerModel; }
}
