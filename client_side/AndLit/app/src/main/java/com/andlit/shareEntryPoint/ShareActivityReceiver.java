package com.andlit.shareEntryPoint;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.andlit.cloudInterface.authentication.Authenticator;
import com.andlit.ui.IntermediateCameraActivity;
import com.andlit.ui.helperUI.ImgGrabber;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShareActivityReceiver extends Activity {
    private static String TAG = "IntermediateCamActivity";

    protected void onCreate(Bundle savedInstanceState) {
        // Get intent, action and MIME type
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (!new Authenticator(this).isLoggedIn()) {
            Toast.makeText(this,"You are not logged in!",Toast.LENGTH_LONG).show();
            finish();
        }
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri == null)
                    finish();
                Log.d(TAG,"-1->"+imageUri.getPath());
                File root = new File(getFilesDir(), ImgGrabber.CAPTURED_DIR);
                if (!root.exists())
                    root.mkdirs();
                File imageLocation = new File(root,"capture_"+System.currentTimeMillis()+".png");
                if(imageLocation.exists())
                    imageLocation.delete();
                try {
                    imageLocation.createNewFile();
                } catch (IOException e) { finish(); }
                Log.d(TAG,"-2->"+imageLocation.getPath());
                try {
                    FileInputStream fis = new FileInputStream(imageUri.getPath());
                    FileOutputStream fos = new FileOutputStream(imageLocation);
                    byte [] buffer = new byte[1024];
                    while (true) {
                        int read = 0;
                        read = fis.read(buffer);
                        if (read == -1)
                            break;
                        fos.write(buffer, 0, read);
                    }
                    fos.flush();
                    fos.close();
                    fis.close();
                } catch (FileNotFoundException e) {finish();} catch (IOException e) {finish();}
                Log.d(TAG,"-3->"+imageLocation.length());
                Intent i = new Intent(this,IntermediateCameraActivity.class);
                i.putExtra("filepath",imageLocation.getAbsolutePath());
                startActivity(i);
            }
            else
                finish();
        } else {
            finish();
        }
    }
}
