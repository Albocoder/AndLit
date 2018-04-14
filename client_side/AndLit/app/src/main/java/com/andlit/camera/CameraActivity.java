package com.andlit.camera;

import android.graphics.SurfaceTexture;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.view.SurfaceView;
import java.io.IOException;

public class CameraActivity extends AppCompatActivity
{
    private Camera mCamera;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our dummy Preview view
        SurfaceView view = new SurfaceView(this);
        try {
            mCamera.setPreviewDisplay(view.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
        SurfaceTexture surfaceTexture = new SurfaceTexture(10);
        try {
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) { e.printStackTrace(); }

        // get Camera parameters
        Camera.Parameters params = mCamera.getParameters();

        // set the picture size
        params.setPictureSize(3264,1836);

        // set Camera parameters
        mCamera.setParameters(params);

        mCamera.startPreview();

        Camera.AutoFocusCallback autoFocusCallBack = new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {
                mCamera.takePicture(null, null,mPicture);
            }
        };

        mCamera.startPreview();
        mCamera.autoFocus(autoFocusCallBack);
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance()
    {
        Camera c = null;
        try
        {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e)
        {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback()
    {
        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {
            //decode the data obtained by the camera into a Bitmap
            BitmapWrapper.bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            releaseCamera();

            setResult(Activity.RESULT_OK);

            finish();
        }
    };

    @Override
    protected void onPause()
    {
        super.onPause();

        if( mCamera != null )
            mCamera.stopPreview();

        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseCamera()
    {
        if( mCamera != null )
        {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
}


