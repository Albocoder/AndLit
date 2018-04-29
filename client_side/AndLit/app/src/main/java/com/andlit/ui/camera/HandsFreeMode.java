package com.andlit.ui.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import com.andlit.R;
import com.andlit.utils.RequestCodes;
import com.andlit.session.Session;

public class HandsFreeMode extends Session {

    // logs
    private static final String PERMISSION_REQUEST = "Please allow me to use the camera for your needs.";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isVoiceSession = true;
        Button b = findViewById(R.id.takeVoiceCommands);
        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                performTasks();
            }
        });
    }

    @Override
    protected int getLayoutId() { return R.layout.hands_free_layout; }

    private void performTasks(){
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            audioFeedback(PERMISSION_REQUEST);
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.CAMERA}, RequestCodes.CAMERA_PERMISSION_RC);
        }else {
            startVoiceCapture();
        }
    }
}
