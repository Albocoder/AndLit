package albocoder.github.com.facedetector;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Algorithm;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FaceOperator extends Activity{

    // fields
    private static final String TAG = "Controller:FaceOperator";
    private float mRelativeFaceSize;
    private int mAbsoluteFaceSize;
    CameraBridgeViewBase.CvCameraViewFrame scene;
    private static CascadeClassifier mJavaDetector;

    // constructors
    FaceOperator(CameraBridgeViewBase.CvCameraViewFrame f,float rel, int abs){
        scene = f;
        mAbsoluteFaceSize = abs;
        mRelativeFaceSize = rel;
        if (mJavaDetector == null) {
            try {
                // load cascade file from application resources
                InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml"); // The better one
                FileOutputStream os = new FileOutputStream(mCascadeFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                is.close();
                os.close();

                mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                if (mJavaDetector.empty()) {
                    Log.e(TAG, "Failed to load cascade classifier");
                    mJavaDetector = null;
                } else
                    Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
                //cascadeDir.delete();

            } catch (IOException e) {
                Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                mJavaDetector = null;
            }
        }
    }
    FaceOperator(CameraBridgeViewBase.CvCameraViewFrame f){ this(f,0.2f,30); }

    // OPERATIONS ON SCENE
    //  1. get the faces in the scene
    public Face[] getFaces(){
        Mat mRgba = scene.rgba();
        Mat mGray = scene.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect faces = new MatOfRect();

        if (mJavaDetector != null)
            mJavaDetector.detectMultiScale(mGray, faces, 1.1, 5, 0| Objdetect.CASCADE_SCALE_IMAGE,
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

        Rect[] facesRectArray = faces.toArray();
        Face[] facesArray = new Face[facesRectArray.length];

        int index = 0;
        for (Rect faceRect : facesRectArray) {
            Mat content = mRgba.submat(faceRect);
            facesArray[index] = new Face(faceRect,content);
            index++;
        }
        return facesArray;
    }

    //  2. populate faces with the IDs of faces
    public void findNames(Face[] faces){
        // 1. Histogram Equalization
        // 2.
    }
}
