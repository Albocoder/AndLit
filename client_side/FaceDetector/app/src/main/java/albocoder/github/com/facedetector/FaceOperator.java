package albocoder.github.com.facedetector;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;

public class FaceOperator extends Activity{

    // fields
    private static final String TAG = "Controller:FaceOperator";
    private float mRelativeFaceSize;
    private int mAbsoluteFaceSize;
    Mat scene;
    private static CascadeClassifier mJavaFrontDetector;
    private static CascadeClassifier mJavaProfileDetector;

    // calculated
    private Face[] faces;

    // constructors
    FaceOperator(Mat f,float rel, int abs){
        scene = new Mat();
        f.copyTo(scene);
        mAbsoluteFaceSize = abs;
        mRelativeFaceSize = rel;
        faces = null;
        if (mJavaFrontDetector == null) {
            try {
                // load cascade file from application resources
                InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface_improved.xml");
                FileOutputStream os = new FileOutputStream(mCascadeFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1)
                    os.write(buffer, 0, bytesRead);
                is.close();
                os.close();

                mJavaFrontDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                if (mJavaFrontDetector.empty()) {
                    Log.e(TAG, "Failed to load cascade classifier");
                    mJavaFrontDetector = null;
                } else
                    Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
            } catch (IOException e) {
                Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                mJavaFrontDetector = null;
            }
        }
        if (mJavaProfileDetector == null) {
            try {
                // load cascade file from application resources
                InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                File mCascadeFile = new File(cascadeDir, "lbpcascade_profileface.xml");
                FileOutputStream os = new FileOutputStream(mCascadeFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1)
                    os.write(buffer, 0, bytesRead);
                is.close();
                os.close();

                mJavaProfileDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                if (mJavaProfileDetector.empty()) {
                    Log.e(TAG, "Failed to load cascade classifier");
                    mJavaProfileDetector = null;
                } else
                    Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
            } catch (IOException e) {
                Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                mJavaProfileDetector = null;
            }
        }
    }
    FaceOperator(Mat f){ this(f,0.2f,30); }
    FaceOperator(CameraBridgeViewBase.CvCameraViewFrame f,float rel, int abs){this(f.rgba(),rel,abs);}
    FaceOperator(CameraBridgeViewBase.CvCameraViewFrame f){this(f,0.2f,30);}

    // OPERATIONS ON SCENE
    //  1. get the faces in the scene
    public Face[] getFaces(){
        if(faces != null)
            return faces;
        Mat mGray = new Mat();
        Imgproc.cvtColor(scene,mGray,Imgproc.COLOR_RGB2GRAY);

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect facesFron = new MatOfRect();
        MatOfRect facesProf = new MatOfRect();

        if (mJavaFrontDetector != null)
            mJavaFrontDetector.detectMultiScale(mGray, facesFron, 1.1, 5, 0| Objdetect.CASCADE_SCALE_IMAGE,
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        if (mJavaProfileDetector != null)
            mJavaProfileDetector.detectMultiScale(mGray, facesProf, 1.1, 5, 0| Objdetect.CASCADE_SCALE_IMAGE,
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

        //TODO: Rect[] facesRectArray = concatenate(facesFron.toArray(),facesProf.toArray());
        Rect[] facesRectArray = facesFron.toArray();

        Face[] facesArray = new Face[facesRectArray.length];

        int index = 0;
        for (Rect faceRect : facesRectArray) {
            Mat content = scene.submat(faceRect);
            facesArray[index] = new Face(faceRect,content);
            index++;
        }
        faces = facesArray;
        return facesArray;
    }

    //  2. populate faces with the IDs of faces
    public void findNames(){
        if(faces == null)
            getFaces();
        for(Face f:faces)
            f.recognize();
    }

    // TODO: Set up a commons static class to do all the common operations
    public <T> T[] concatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;
        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }
}