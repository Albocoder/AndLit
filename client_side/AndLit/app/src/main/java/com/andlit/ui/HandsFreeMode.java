package com.andlit.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.andlit.R;
import com.andlit.RequestCodes;
import com.andlit.session.Session;
import com.andlit.voice.VoiceToCommand;

import java.util.ArrayList;
import java.util.Locale;

public class HandsFreeMode extends Session {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isVoiceSession = true;
        Button b = findViewById(R.id.takeVoiceCommands);
        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                startVoiceCapture();
            }
        });
    }

    @Override
    protected int getLayoutId() { return R.layout.hands_free_layout; }
}
