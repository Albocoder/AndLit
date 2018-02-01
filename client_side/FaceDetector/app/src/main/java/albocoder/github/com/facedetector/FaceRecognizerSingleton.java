package albocoder.github.com.facedetector;

import android.content.Context;
import android.support.annotation.NonNull;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.bytedeco.javacpp.opencv_face.LBPHFaceRecognizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

    public FaceRecognizerSingleton(Context c){ this.c = c; trainModel(); }

    private void trainModel() {
        trainedModel = LBPHFaceRecognizer.create(SEARCH_RADIUS,NEIGHBORS,GRID_X,GRID_Y,THRESHOLD);
        for (int i = 1; i < 10; i++) {
            try {
                String fileLocation = getFilePathFromAssets(c, "erinface/face" + i + ".png", "face14.png");
                Mat m = Imgcodecs.imread(fileLocation);
                new File(fileLocation).delete();    // delete the file
            } catch (IOException e) {}
        }
    }
//    public long predict(Face face){
//        trainedModel.predict(face.histEqualization(),labels,confidences);
//    }

    @NonNull
    public static String getFilePathFromAssets(Context c, String path, String newFileName) throws IOException {
        InputStream is = c.getAssets().open(path);
        File cascadeDir = c.getDir("cascade", Context.MODE_PRIVATE);
        File mCascadeFile = new File(cascadeDir, newFileName);
        FileOutputStream os = new FileOutputStream(mCascadeFile);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1)
            os.write(buffer, 0, bytesRead);
        is.close();
        os.close();
        return  mCascadeFile.getAbsolutePath();
    }
}
