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

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.WindowManager;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_objdetect;

import java.io.File;
import java.io.IOException;

import albocoder.github.com.facedetector.database.AppDatabase;
import albocoder.github.com.facedetector.utils.StorageHelper;

import static org.bytedeco.javacpp.opencv_core.LINE_8;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;

public class FdActivity extends Activity implements CvCameraPreview.CvCameraViewListener {
    private static final String    TAG                 = "OCVSample::MainActivity";
    private AppDatabase db;
    private FaceOperator fop;
    private CvCameraPreview cameraView;
    private opencv_objdetect.CascadeClassifier faceDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opencv);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}
                            ,2);
        }

        cameraView = (CvCameraPreview) findViewById(R.id.camera_view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        cameraView.setCvCameraViewListener(this);




        // todo: remove this cuz its only for testing the save image functionality
        Face toPredict = null;
        try {
            int[] l = new int[1];
            double[] conf = new double[1];
            String fileLocation = StorageHelper.getFilePathFromAssets(this, "trainingdata/features/face9.png", "face.png");
            toPredict = new Face(imread(fileLocation));
            new File(fileLocation).delete();
        }catch (IOException e){Log.d(TAG,"Error encountered while saving:" +e.getMessage());}
        boolean saved = false;
        try {
            saved = FaceOperator.saveFaceToDatabase(this,toPredict);
        } catch (IOException e) {
            Log.d(TAG,"Error encountered while saving:" +e.getMessage());
        }
        Log.d(TAG,"Saved: "+saved);

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
    @Override
    public void onCameraViewStarted(int width, int height) { }
    @Override
    public void onCameraViewStopped() { if (fop != null) fop.destroy(); fop = null; }

    @Override
    public Mat onCameraFrame(Mat mat) {
        FaceOperator fop = new FaceOperator(this,mat);
        Face [] faces = fop.getFaces();
        if (faces == null)
            return mat;
        for (Face aFacesArray : faces) {
            int x = aFacesArray.getBoundingBox().x();
            int y = aFacesArray.getBoundingBox().y();
            int w = aFacesArray.getBoundingBox().width();
            int h = aFacesArray.getBoundingBox().height();
            rectangle(mat,new opencv_core.Point(x, y), new opencv_core.Point(x + w, y + h)
                    , opencv_core.Scalar.GREEN,2, LINE_8,0);
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