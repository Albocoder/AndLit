package albocoder.github.com.facedetector;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
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
    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }
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
}