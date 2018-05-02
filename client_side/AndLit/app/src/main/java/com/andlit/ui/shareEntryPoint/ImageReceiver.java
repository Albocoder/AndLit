package com.andlit.ui.shareEntryPoint;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.andlit.cloudInterface.authentication.Authenticator;
import com.andlit.ui.camera.IntermediateCameraActivity;
import com.andlit.ui.camera.helperUI.ImgGrabber;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageReceiver extends Activity {
    private static String TAG = "ImageReceiver";

    protected void onCreate(Bundle savedInstanceState) {
        // Get intent, action and MIME type
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        try{
            if (!new Authenticator(this).isLoggedIn()) {
                Toast.makeText(this,"You are not logged in!",Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } catch(Exception e){
            Toast.makeText(this,"You are not logged in!",Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri == null)
                    finish();
                File root = new File(getFilesDir(), ImgGrabber.CAPTURED_DIR);
                if (!root.exists())
                    root.mkdirs();
                File imageLocation = new File(root,"capture_"+System.currentTimeMillis()+".png");
                if(imageLocation.exists())
                    imageLocation.delete();
                try {
                    imageLocation.createNewFile();
                } catch (IOException e) { finish(); }
                InputStream fis = null;
                try {
                    fis = getContentResolver().openInputStream(imageUri);
                } catch (FileNotFoundException e) {
                    Log.e(TAG,"1->"+e.getLocalizedMessage());
                    Toast.makeText(this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(imageLocation);
                } catch (FileNotFoundException e) {
                    Log.e(TAG,"2->"+e.getLocalizedMessage());
                    Toast.makeText(this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                    byte [] buffer = new byte[1024];
                    while (true) {
                        int read = 0;
                        try {
                            read = fis.read(buffer);
                        } catch (IOException e) {
                            Log.e(TAG,"3->"+e.getLocalizedMessage());
                        }
                        if (read == -1)
                            break;
                        try {
                            fos.write(buffer, 0, read);
                        } catch (IOException e) {
                            Log.e(TAG,"4->"+e.getLocalizedMessage());
                        }
                    }
                try {
                    fos.flush();
                } catch (IOException e) {
                    Log.e(TAG,"5->"+e.getLocalizedMessage());
                }
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG,"6->"+e.getLocalizedMessage());
                }
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.e(TAG,"7->"+e.getLocalizedMessage());
                }
                Intent i = new Intent(this,IntermediateCameraActivity.class);
                i.putExtra("filepath",imageLocation.getAbsolutePath());
                startActivity(i);
                finish();
            }
            else
                finish();
        } else {
            finish();
        }
    }
}
