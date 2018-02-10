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

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Random;

import albocoder.github.com.facedetector.database.AppDatabase;
import albocoder.github.com.facedetector.database.entities.KnownPPL;
import albocoder.github.com.facedetector.database.entities.training_face;
import albocoder.github.com.facedetector.face.Face;
import albocoder.github.com.facedetector.face.FaceOperator;
import albocoder.github.com.facedetector.face.FaceRecognizerSingleton;
import albocoder.github.com.facedetector.face.RecognizedFace;
import albocoder.github.com.facedetector.utils.StorageHelper;

import static org.bytedeco.javacpp.opencv_core.LINE_8;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.CV_FONT_HERSHEY_PLAIN;
import static org.bytedeco.javacpp.opencv_imgproc.putText;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;

public class FdActivity extends Activity implements CvCameraPreview.CvCameraViewListener {
    private static final String    TAG                 = "OCVSample::MainActivity";
    private AppDatabase db;
    private FaceOperator fop;
    private FaceRecognizerSingleton frs;
    private CvCameraPreview cameraView;

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
            return;
        }
        cameraView = (CvCameraPreview) findViewById(R.id.camera_view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        cameraView.setCvCameraViewListener(this);


        // testing classifier
        db = AppDatabase.getDatabase(getApplicationContext());
        db.knownPplDao().insertEntry(
                new KnownPPL(1,"Erin","Avllazagaj",new Date(1996,3,13), 21,"Dorm #76 Bilkent") );
        db.knownPplDao().insertEntry(
                new KnownPPL(2,"Barack","Obama",new Date(1961,8,4), 56,"USA") );

        // random generator for md5 (NEVER DO THIS IRL)
        Random r = new Random();

        // delete all instances
        List<training_face> recs =  db.trainingFaceDao().getAllRecords();
        for (training_face tf : recs)
            FaceOperator.deleteTrainingInstance(this,tf);

        for(int i = 1; i < 17; i++) {
            String filepath;
            try {
                filepath = StorageHelper.getFilePathFromAssets(this,"training/face"+i+".png","training","face"+i+".png");
            } catch (IOException e) { continue; }
            String md5 = ""+r.nextLong()+r.nextInt();
            try {
                md5 = StorageHelper.MD5toHexString(StorageHelper.getMD5OfFile(filepath));
            } catch (IOException|NoSuchAlgorithmException e) {}

            Mat m = imread(filepath);
            if (m==null)
                continue;
            Face detected = new Face(m);
            if (i == 15 || i == 16) {
                frs = new FaceRecognizerSingleton(this);
                RecognizedFace rf = frs.recognize(detected);
                Log.d(TAG,rf.toString());
                continue;
            }
            if(i < 9)
                try {
                    detected.setID(1);
                    FaceOperator.moveDetectionToTraining(this,FaceOperator.saveDetectedFaceToDatabase(this,detected));
                } catch (IOException|NoSuchAlgorithmException e) {}
            else
                try {
                    detected.setID(2);
                    FaceOperator.moveDetectionToTraining(this,FaceOperator.saveDetectedFaceToDatabase(this,detected));
                } catch (IOException|NoSuchAlgorithmException e) {}

            new File(filepath).delete();
        }

//        Face toPredict = null;
//        try {
//            int[] l = new int[1];
//            double[] conf = new double[1];
//            String fileLocation = StorageHelper.getFilePathFromAssets(getApplicationContext()
//                    , "trainingdata/features/face9.png", "face.png");
//            toPredict = new Face(imread(fileLocation));
//            new File(fileLocation).delete();
//        }catch (IOException e){}
//        toPredict.setID(1);
//
//        try {
//            detected_face f = FaceOperator.saveDetectedFaceToDatabase(this,toPredict);
//            if(f != null)
//                Log.d(TAG,f.toString());// if one works the other will too
//        } catch (IOException |NoSuchAlgorithmException e) {
//            Log.d(TAG,"Error encountered while saving:" +e.getMessage());
//        }
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

            RecognizedFace rf = frs.recognize(aFacesArray);
            int [] labels = rf.getLabels();
            double [] conf = rf.getConfidences();

            Log.d(TAG,"[");
            for(int i = 0; i < labels.length;i++){
                Log.d(TAG," ("+labels[i]+", "+conf[i]+")");
            }
            Log.d(TAG," ]");

            KnownPPL p = db.knownPplDao().getEntryWithID(labels[0]);
            if (p!= null)
                putText(mat,p.name+" "+p.sname,new opencv_core.Point(x, y),CV_FONT_HERSHEY_PLAIN,1, opencv_core.Scalar.MAGENTA);
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