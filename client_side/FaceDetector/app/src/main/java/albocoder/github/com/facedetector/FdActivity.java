package albocoder.github.com.facedetector;

<<<<<<< HEAD
=======
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

>>>>>>> 2bdcb84510d217369405090c62d7012b44d54212
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
<<<<<<< HEAD
import org.opencv.core.Scalar;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
=======
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.Objdetect;
import org.opencv.osgi.OpenCVInterface;
>>>>>>> 2bdcb84510d217369405090c62d7012b44d54212

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
<<<<<<< HEAD
import android.view.WindowManager;

public class FdActivity extends Activity implements CvCameraViewListener2 {
    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    private Mat                    mRgba;
    private CameraBridgeViewBase   mOpenCvCameraView;
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            mOpenCvCameraView.enableView();
            super.onManagerConnected(status);
        }
    };

=======
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

public class FdActivity extends Activity {

    /** Called when the activity is first created. */
>>>>>>> 2bdcb84510d217369405090c62d7012b44d54212
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
<<<<<<< HEAD

        setContentView(R.layout.face_detect_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }
=======
        setContentView(R.layout.face_detect_surface_view);
    }

    /*
>>>>>>> 2bdcb84510d217369405090c62d7012b44d54212
    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
<<<<<<< HEAD
=======

>>>>>>> 2bdcb84510d217369405090c62d7012b44d54212
    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
<<<<<<< HEAD
=======

>>>>>>> 2bdcb84510d217369405090c62d7012b44d54212
    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }
<<<<<<< HEAD
    public void onCameraViewStarted(int width, int height) { mRgba = new Mat(); }
    public void onCameraViewStopped() { mRgba.release(); }
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        FaceRunner facernb = new FaceRunner(this,mRgba);
        this.runOnUiThread(facernb);
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        for (Face aFacesArray : facernb.getFaces())
            Imgproc.rectangle(mRgba, aFacesArray.getBoundingBox().tl(),
                    aFacesArray.getBoundingBox().br(), FACE_RECT_COLOR, 3);
        return mRgba;
    }
    class FaceRunner implements Runnable{
        Mat m;
        Face[] facesArray;
        Context c;
        public FaceRunner(Context c,Mat s){
            m = new Mat();
            this.c = c;
            s.copyTo(m);
            facesArray = null;
        }
        @Override
        public void run() {
            FaceOperator fop = new FaceOperator(c,m);
            facesArray = fop.getFaces();
        }
        public Face[] getFaces(){return facesArray;}
    }
=======

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Face[] faces = getFaces(inputFrame);
        Rect[] facesArray = new Rect[faces.length];
        int index = 0;
        for (Face f: faces)
            facesArray[index++] = f.getBoundingBox();
        for (Rect fr : facesArray)
            Imgproc.rectangle(mRgba, fr.tl(), fr.br(), FACE_RECT_COLOR, 3);
        return mRgba;
    }

    public Face[] getFaces(CvCameraViewFrame frame){
        mRgba = frame.rgba();
        mGray = frame.gray();

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
    }*/
>>>>>>> 2bdcb84510d217369405090c62d7012b44d54212
}