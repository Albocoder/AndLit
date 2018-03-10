package com.example.hamza.voicecontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

// Speech to text imports
import java.util.ArrayList;
import java.util.Locale;
import android.content.ActivityNotFoundException;
import android.speech.RecognizerIntent;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/*
An android activity class to test the VoiceGenerator and VoiceRecognizers
 */
public class MainActivity extends AppCompatActivity
{
    // Properties
    private TextView txtSpeechInput;
    private ImageButton btnSpeak;

    // Methods
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SpeechToText
        txtSpeechInput = findViewById(R.id.txtSpeechInput);
        txtSpeechInput.setText("Text should be here");
        btnSpeak = findViewById(R.id.btnSpeak);

        btnSpeak.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                promptSpeechInput();
            }
        });
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

    /** Called when the user taps the Send button */
    public void sendMessage(View view)
    {
        // Text To Speech testing
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        VoiceGenerator speaker = new VoiceGenerator(this.getApplicationContext());
        speaker.speak(message);
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
                txtSpeechInput.setText(result.get(0));
            }
        }
    }
}
