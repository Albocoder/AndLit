package albocoder.github.com.facedetector;

import android.content.Context;

import org.opencv.core.Mat;
import org.opencv.face.LBPHFaceRecognizer;

public class FaceRecognizerSingleton {
    // constants
    public static final int SEARCH_RADIUS = 1;
    public static final int NEIGHBORS = 5;
    public static final int GRID_X = 8;
    public static final int GRID_Y = 8;
    public static final double THRESHOLD = 130.0;

    // statics
    private static LBPHFaceRecognizer trainedModel;
    // fields
    private Context c;

    public FaceRecognizerSingleton(Context c){ trainModel(); this.c = c;}

    private void trainModel() {
        trainedModel = LBPHFaceRecognizer.create(SEARCH_RADIUS,NEIGHBORS,GRID_X,GRID_Y,THRESHOLD);
        // get mat and label from database
        //trainedModel.train(mats,labels);
    }
//    public long predict(Face face){
//        trainedModel.predict(face.histEqualization(),labels,confidences);
//    }
}
