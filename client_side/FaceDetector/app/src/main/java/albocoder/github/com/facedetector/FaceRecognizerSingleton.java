package albocoder.github.com.facedetector;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.bytedeco.javacpp.opencv_face.LBPHFaceRecognizer;

public class FaceRecognizerSingleton {
    // constants
    public static final int SEARCH_RADIUS = 1;
    public static final int NEIGHBORS = 5;
    public static final int GRID_X = 8;
    public static final int GRID_Y = 8;
    public static final double THRESHOLD = 130.0;
    private static final String TAG = "Controller::FaceRecognizerSingleton";

    // statics
    private static LBPHFaceRecognizer trainedModel;
    // fields
    private Context c;

    public FaceRecognizerSingleton(Context c){
        this.c = c;
    }

    @VisibleForTesting
    public void trainModel() {
        trainedModel = LBPHFaceRecognizer.create(SEARCH_RADIUS,NEIGHBORS,GRID_X,GRID_Y,THRESHOLD);
        Mat m = Imgcodecs.imread("android.resource://albocoder.github.com.facedetector/"+R.raw.face14);
        Log.i(TAG,m.dump());
        // get mat and label from database
        //trainedModel.train(mats,labels);
    }
//    public long predict(Face face){
//        trainedModel.predict(face.histEqualization(),labels,confidences);
//    }
}
