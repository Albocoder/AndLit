package albocoder.github.com.facedetector;

import android.content.Context;
import android.util.Log;

import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_objdetect.*;

import java.io.*;

import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;

public class FaceOperator {
    // class constants
    private static final int FACE_MIN_SQUARE_EDGE = 40; // start detection from 40x40 square
    private static final float FACE_MIN_SQUARE_RELATIVE_EDGE = 0.2f;

    // shared fields
    private static CascadeClassifier frontDetector;
    private static CascadeClassifier profileDetector;
    private static final String TAG = "Controller:FaceOperator";

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
        scene = new Mat();
        f.copyTo(scene);
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
        Mat mRgba = new Mat(), mGray = new Mat();

        scene.copyTo(mRgba);
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
            Mat content = mRgba.apply(faceRect);
            facesArray[i] = new Face(faceRect,content);
        }
//        Log.i(TAG,"Found "+facesRectArray.size()+"!");
        foundFaces = facesArray;
        mRgba.release();
        mGray.release();
        return facesArray;
    }
    private void initializeDetector() {
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
    public void destroy(){ for(Face f:foundFaces) f.destroy(); scene.release();}
}
