package albocoder.github.com.facedetector;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import albocoder.github.com.facedetector.database.AppDatabase;
import albocoder.github.com.facedetector.database.entities.detected_faces;

import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RGB2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_objdetect.CASCADE_SCALE_IMAGE;

public class FaceOperator {
    // class constants
    private static final int FACE_MIN_SQUARE_EDGE = 40; // start detection from 40x40 square
    private static final float FACE_MIN_SQUARE_RELATIVE_EDGE = 0.2f;

    // shared fields
    private static CascadeClassifier frontDetector;
    private static CascadeClassifier profileDetector;
    private static final String TAG = "Controller:FaceOperator";
    private static final String DETECTIONS_PATH = "detections/";

    // unique fields
    private float mRelativeFaceSize;
    private int mAbsoluteFaceSize;
    private Mat scene;
    private final Context context;

    // calculated fields
    private Face[] foundFaces;

    // constructors
    FaceOperator(Context c, Mat f, int abs, float rel){
        super();
        context = c;
        scene = f.clone();
        mAbsoluteFaceSize = abs;
        mRelativeFaceSize = rel;
        foundFaces = null;
    }
    FaceOperator(Context c, Mat f){this (c,f,FACE_MIN_SQUARE_EDGE,FACE_MIN_SQUARE_RELATIVE_EDGE);}
    FaceOperator(Context c, Mat f,float rel){this (c,f,0,rel);}
    FaceOperator(Context c, Mat f,int abs){this (c,f,abs,FACE_MIN_SQUARE_RELATIVE_EDGE);}

    public Face[] getFaces() {
        if (foundFaces != null)
            return foundFaces;
        initializeDetector();
        Mat mGray = scene.clone();

        cvtColor(scene,mGray, CV_RGB2GRAY);

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0)
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
        }

        RectVector facesVector = new RectVector();
        if (frontDetector != null)
            frontDetector.detectMultiScale(mGray, facesVector, 1.1, 5, 0| CASCADE_SCALE_IMAGE,
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        Face[] facesArray = new Face[(int) facesVector.size()];
        for (int i = 0; i < facesVector.size();i++) {
            Rect faceRect = facesVector.get(i);
            Mat content = scene.apply(faceRect);
            facesArray[i] = new Face(faceRect,content);
        }
//        Log.i(TAG,"Found "+facesRectArray.size()+"!");
        foundFaces = facesArray;
        mGray.release();
        System.gc();
        Runtime.getRuntime().gc(); // if you delete this you are fucked! DON'T DO IT FFS!!!
        return facesArray;
    }
    private synchronized void initializeDetector() {
        if (frontDetector == null) {
            try {
                // load cascade file from application resources
                InputStream is = context.getResources().openRawResource(R.raw.lbpcascade_frontalface_improved);
                File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
                File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface_improved.xml");
                FileOutputStream os = new FileOutputStream(mCascadeFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1)
                    os.write(buffer, 0, bytesRead);
                is.close();
                os.close();

                frontDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                frontDetector.load(mCascadeFile.getAbsolutePath());
                if (frontDetector.empty()) {
                    Log.e(TAG, "Failed to load cascade classifier");
                    frontDetector = null;
                } else
                    Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
                cascadeDir.delete();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
            }
        }
        if (profileDetector == null) {
            try {
                // load cascade file from application resources
                InputStream is = context.getResources().openRawResource(R.raw.lbpcascade_profileface);
                File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
                File mCascadeFile = new File(cascadeDir, "lbpcascade_profileface.xml");
                FileOutputStream os = new FileOutputStream(mCascadeFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1)
                    os.write(buffer, 0, bytesRead);
                is.close();
                os.close();

                profileDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                profileDetector.load(mCascadeFile.getAbsolutePath());
                if (profileDetector.empty()) {
                    Log.e(TAG, "Failed to load cascade classifier2");
                    profileDetector = null;
                } else
                    Log.i(TAG, "Loaded cascade classifier2 from " + mCascadeFile.getAbsolutePath());
                cascadeDir.delete();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to load cascade2. Exception thrown: " + e);
            }
        }
    }
    public Face[] recognizeFaces() {
        FaceRecognizerSingleton frs = new FaceRecognizerSingleton(context);
        Face[] facesToRecognize = getFaces();

        for(Face f: facesToRecognize)
            frs.predict(f); // Todo: develop this to get the id;
        return facesToRecognize;
    }

    // Utility functions
    public void destroy(){
        for(Face f:foundFaces)
            f.destroy();
        scene.release();
        foundFaces = null;
        System.gc();
    }
    public static boolean saveFaceToDatabase(Context c, Face f) throws IOException {
        // todo: TEST
        long timeInMillis = System.currentTimeMillis();
        Random r = new Random();
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            // write to here
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root+DETECTIONS_PATH);
            if(!myDir.exists())
                myDir.mkdir();
            String filename = "face_"+timeInMillis+"_"+r.nextInt()+".png";
            File toSave = new File (myDir, filename);
            if (toSave.exists())
                toSave.delete();
            Mat m = f.getRGBContent().clone();
//            cvtColor(m,m,CV_RGB2BGR);
            boolean saved = imwrite(toSave.getAbsolutePath(),m);
            if(!saved)
                return false;
            FileInputStream fis = new FileInputStream(toSave.getAbsolutePath());
            String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
            fis.close();
            detected_faces detection = new detected_faces(f.getID(),toSave.getAbsolutePath()
                    ,md5,timeInMillis);
            AppDatabase db = AppDatabase.getDatabase(c);
            db.facesDao().insertFace(detection);
        }
        else{
            // write to here
            File detectionsDir = new File(c.getFilesDir(),DETECTIONS_PATH);
            if(!detectionsDir.exists())
                detectionsDir.mkdir();
            String filename = "face_"+timeInMillis+"_"+r.nextInt()+".png";
            File toSave = new File (detectionsDir, filename);
            if (toSave.exists())
                toSave.delete();
            Mat m = f.getRGBContent().clone();
//            cvtColor(m,m,CV_RGB2BGR);
            boolean saved = imwrite(toSave.getAbsolutePath(),m);
            if(!saved)
                return false;
            FileInputStream fis = new FileInputStream(toSave.getAbsolutePath());
            String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
            fis.close();
            detected_faces detection = new detected_faces(f.getID(),toSave.getAbsolutePath()
                    ,md5,timeInMillis);
            AppDatabase db = AppDatabase.getDatabase(c);
            db.facesDao().insertFace(detection);
            return true;
        }
        return true;
    }

    public void storeFaces(){
        for(Face f: foundFaces)
            try {
                saveFaceToDatabase(context,f);
            } catch (IOException e) {
                Log.e(TAG,"Couldn't save a face! Error encountered: "+e.getMessage());
            }
    }
    public void storeFacesThenDestroy(){ storeFaces(); destroy(); }
}
