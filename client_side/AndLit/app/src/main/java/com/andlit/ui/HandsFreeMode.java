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
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.hands_free_layout);
        Button b = findViewById(R.id.takeVoiceCommands);
        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
//                promptSpeechInput();
                getPicture();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // speechToText
        if(requestCode == RequestCodes.SPEECH_INPUT_RC)
        {
            if(resultCode == RESULT_OK && null != data)
            {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                VoiceToCommand vc;
                //todo: make this work in the Session
            }
        }
    }



    private void promptSpeechInput()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something!");
        try
        {
            startActivityForResult(intent, RequestCodes.SPEECH_INPUT_RC);
        }
        catch (ActivityNotFoundException a)
        {
            Toast.makeText(getApplicationContext(), "Speech Not Supported!", Toast.LENGTH_SHORT).show();
        }
    }
}
