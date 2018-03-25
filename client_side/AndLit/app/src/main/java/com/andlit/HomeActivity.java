package com.andlit;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.andlit.CloudInterface.Authenticator;
import com.andlit.Settings.SettingsActivity;
import com.andlit.Settings.SettingsDefinedKeys;
import com.andlit.helperUI.IntermediateCameraActivity;
import com.andlit.voice.VoiceToCommand;
import com.andlit.voice.VoiceToCommandEnglish;

import java.util.ArrayList;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity
{
    // View related Properties
    private TextView txtSpeechInput;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Camera button init
        Button cameraButton = findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                loadCameraScreen();
            }
        });

        // Settings Button init
        Button settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                loadSettingsScreen();
            }
        });

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                new Authenticator(view.getContext()).logout();
                Intent i = new Intent(view.getContext(),LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

        // Voice Button init
        voiceButtonInit();

        // Text view for testing text to speech
        txtSpeechInput = findViewById(R.id.txtSpeechInput);
        txtSpeechInput.setText("Voice Input Should Display Here!");
    }

    private void voiceButtonInit()
    {
        Button voiceButton = findViewById(R.id.voice_button);
        voiceButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                promptSpeechInput();
            }
        });
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        Boolean voiceControl = sharedPref.getBoolean(SettingsDefinedKeys.VOICE_CONTROL, false);
        if(voiceControl)
            voiceButton.setVisibility(View.VISIBLE);
        else
            voiceButton.setVisibility(View.INVISIBLE);
    }

    private void loadSettingsScreen()
    {
        Intent intent = new Intent(this.getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    private void loadCameraScreen(){
        Intent i = new Intent(this, IntermediateCameraActivity.class);
        startActivity(i);
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something!");
        try
        {
            startActivityForResult(intent, 100);
        }
        catch (ActivityNotFoundException a)
        {
            Toast.makeText(getApplicationContext(), "Speech Not Supported!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // speechToText
        if(requestCode == 100)
        {
            if(resultCode == RESULT_OK && null != data)
            {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                // result.get(0) holds the voice input in string form
                txtSpeechInput.setText(result.get(0));  // testing the input by displaying on a text view
                VoiceToCommand vc;
                //todo if langauge settings are english
                vc = new VoiceToCommandEnglish(this);
                vc.decide(result.get(0));
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        voiceButtonInit();
    }
}
