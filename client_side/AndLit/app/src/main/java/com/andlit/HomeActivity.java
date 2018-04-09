package com.andlit;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.andlit.cloudInterface.Authentication.Authenticator;
import com.andlit.face.FaceRecognizerSingleton;
import com.andlit.session.Session;
import com.andlit.settings.SettingsActivity;
import com.andlit.settings.SettingsDefinedKeys;
import com.andlit.ui.HandsFreeMode;
import com.andlit.ui.IntermediateCameraActivity;
import com.andlit.voice.VoiceGenerator;
import com.andlit.voice.VoiceToCommand;
import com.andlit.voice.VoiceToCommandEnglish;
import java.util.ArrayList;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity
{
    private static final String TAG = "HomeActivity";

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

        Button handsFree = findViewById(R.id.handsfree_button);
        handsFree.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                loadHandsFreeMode();
            }
        });

        Button trainingButton = findViewById(R.id.training_button);
        trainingButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                new FaceRecognizerSingleton(view.getContext()).trainModel();
            }
        });

        // Voice Button init
        voiceButtonInit();

        // Text view for testing text to speech
        txtSpeechInput = findViewById(R.id.txtSpeechInput);
        txtSpeechInput.setText("Voice Input Should Display Here!");
    }

    public void voiceButtonInit()
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

    private void loadHandsFreeMode(){
        Intent i = new Intent(this, HandsFreeMode.class);
        startActivity(i);
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
            startActivityForResult(intent, RequestCodes.SPEECH_INPUT_RC);
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
        if(requestCode == RequestCodes.SPEECH_INPUT_RC)
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

        // Audio Feedback
        if(requestCode == RequestCodes.AUDIO_FEEDBACK_RC)
        {
            if(resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
            {
                Intent install = new Intent();
                install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);
            }
        }
    }

    /*
    method to check if a TTS engine is installed on the device.
    The check is performed by making use of the result of another Activity.
    */
    private void checkTTS()
    {
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, RequestCodes.AUDIO_FEEDBACK_RC);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        voiceButtonInit();
    }
}
