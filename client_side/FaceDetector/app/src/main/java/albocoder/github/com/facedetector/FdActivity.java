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

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.WindowManager;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_objdetect;

import albocoder.github.com.facedetector.database.AppDatabase;
import albocoder.github.com.facedetector.utils.StorageHelper;

import static org.bytedeco.javacpp.opencv_core.LINE_8;
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

        cameraView = (CvCameraPreview) findViewById(R.id.camera_view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        cameraView.setCvCameraViewListener(this);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                faceDetector = StorageHelper.loadClassifierCascade(FdActivity.this, R.raw.lbpcascade_frontalface_improved);
                return null;
            }
        }.execute();

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

//    @Override
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
            rectangle(mat,new Point(x, y), new Point(x + w, y + h)
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