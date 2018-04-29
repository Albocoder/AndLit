package com.andlit.ui.camera.helperUI;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.andlit.BuildConfig;
import com.andlit.ui.camera.IntermediateCameraActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


// WARNING!!!!!!! This is a single use Activity! It needs a parent to use it otherwise it crashes!
public class ImgGrabber extends Activity {
    // constants
    public static final String TAG = "ImgAnalysisBundle";
    public static final String CAPTURED_DIR = "captured";
    public static final int CAPTURE_IMAGE_REQUEST = 1337;

    // fields
    private File imageLocation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // connect to the database
        imageLocation = new File(this.getIntent().
                getStringExtra(IntermediateCameraActivity.ARGUMENT_KEY));
        try {
            if(!imageLocation.exists())
                imageLocation.createNewFile();
        } catch (IOException e) {
            Log.d(TAG,"Couldn't open file to save the image!");
            exitActivity(-1);
            return;
        }
        startCameraActivity();
    }
    protected void startCameraActivity() {
        Uri outputFileUri = FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID+".provider",imageLocation);
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // setting up generic flags
        i.setFlags(0);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        // setting up device specific flags
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        else if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
            ClipData clip=
                    ClipData.newUri(getContentResolver(), "scene", outputFileUri);
            i.setClipData(clip);
            i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        else {
            List<ResolveInfo> resInfoList=
                    getPackageManager()
                            .queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);

            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, outputFileUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
        try{
            startActivityForResult(i, CAPTURE_IMAGE_REQUEST);
        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(this,"No camera was found in device!", Toast.LENGTH_LONG).show();
            exitActivity(-1);
        }
    }
    public static void StoreImage(Context mContext, Uri imageLoc, File imageDir) {
        Bitmap bm = null;
        try {
            bm = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), imageLoc);
            FileOutputStream out = new FileOutputStream(imageDir);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            bm.recycle();
        } catch (IOException e) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setResult(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                if(imageLocation.length() == 0 && data != null)
                    StoreImage(this,data.getData(),imageLocation);
                if(imageLocation.length() == 0)
                    exitActivity(-1);
                if(data == null)
                    exitActivity(-1);
                exitActivity(0);
            }
            else {
                exitActivity(-1);
            }
        }
    }
    private void exitActivity(int code){
        setResult(code);
        finish();
    }
}