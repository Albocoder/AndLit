package albocoder.github.com.facedetector;

// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_imgproc;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import java.io.*;
import albocoder.github.com.facedetector.database.*;
import albocoder.github.com.facedetector.database.entities.*;

import static org.bytedeco.javacpp.opencv_core.LINE_8;

public class FdActivity extends Activity implements CvCameraPreview.CvCameraViewListener {
    private static final String    TAG                 = "OCVSample::albocoder.github.com.facedetector.MainActivity";
    private opencv_core.Scalar     FACE_RECT_COLOR     = new opencv_core.Scalar(255,0.0,0.0,1);
    private AppDatabase db;
    private CvCameraPreview cameraView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_detect_surface_view);

        cameraView = (CvCameraPreview) findViewById(R.id.camera_view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        cameraView.setCvCameraViewListener(this);

//        // Database initialization test
//        db = AppDatabase.getDatabase(getApplicationContext());
////        db.userDao().deleteEntries();
//        List<UserLogin> users = db.userDao().getLoginEntry();
//        if (users.size()==0) {
//            db.userDao().insertEntry(new UserLogin(3242,"The aspect of", "twilight... ZOE!"));
//            users.add(db.userDao().getLoginEntry().get(0));
//            Log.d(TAG,users.get(0).toString());
//        }
//        else
//            Log.d(TAG,"It existed before:"+users.get(0).toString());

    }
    public void onCameraViewStarted(int width, int height) {}
    public void onCameraViewStopped() {}

    @Override
    public Mat onCameraFrame(Mat mat) {
//        FaceRunner facernb = new FaceRunner(this,mat);
//        this.runOnUiThread(facernb);
        FaceOperator fop = new FaceOperator(this,mat);
        Face [] faces = fop.getFaces();
//        Face [] faces = facernb.getFaces();
        if (faces == null)
            return mat;
        for (Face aFacesArray : faces) {
            int x = aFacesArray.getBoundingBox().x();
            int y = aFacesArray.getBoundingBox().y();
            int w = aFacesArray.getBoundingBox().width();
            int h = aFacesArray.getBoundingBox().height();
            opencv_imgproc.rectangle(mat,new Point(x, y), new Point(x + w, y + h)
                    , Scalar.GREEN,2, LINE_8,0);
        }
        fop.destroy();
        fop = null;
        return mat;
    }
}
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!
// Warning: TESTiNG ONLY CLASS DON'T USE!!!!