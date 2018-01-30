package albocoder.github.com.facedetector;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import java.io.*;

public class FaceOperator {
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
    FaceOperator(Context c,Mat f,float rel, int abs){
        super();
        context = c;
        scene = new Mat();
        f.copyTo(scene);
        mAbsoluteFaceSize = abs;
        mRelativeFaceSize = rel;
        foundFaces = null;
    }
    FaceOperator(Context c, Mat f){this (c,f,0.2f,30);}
    FaceOperator(Context c, CameraBridgeViewBase.CvCameraViewFrame f,float rel, int abs){this(c,f.rgba(),rel,abs);}
    FaceOperator(Context c, CameraBridgeViewBase.CvCameraViewFrame f){this(c,f.rgba(),0.2f,30);}

    public Face[] getFaces() {
        if (foundFaces != null)
            return foundFaces;
        initializeDetector();
        Mat mRgba = new Mat(), mGray = new Mat();

        scene.copyTo(mRgba);
        Imgproc.cvtColor(scene,mGray, Imgproc.COLOR_RGB2GRAY);

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0)
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
        }

        MatOfRect facesFront = new MatOfRect();
        MatOfRect facesProf = new MatOfRect();
        if (frontDetector != null)
            frontDetector.detectMultiScale(mGray, facesFront, 1.1, 5, 0|Objdetect.CASCADE_SCALE_IMAGE,
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        if (profileDetector != null)
            profileDetector.detectMultiScale(mGray, facesProf, 1.1, 5, 0|Objdetect.CASCADE_SCALE_IMAGE,
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        Rect[] facesRectArray = facesFront.toArray();//ToDo: Merge the two arrays(+facesProf.toArray())
        Face[] facesArray = new Face[facesRectArray.length];

        int index = 0;
        for (Rect faceRect : facesRectArray) {
            Mat content = mRgba.submat(faceRect);
            facesArray[index] = new Face(faceRect,content);
            index++;
        }
        foundFaces = facesArray;
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
            f.recognize();
        return facesToRecognize;
    }
}
