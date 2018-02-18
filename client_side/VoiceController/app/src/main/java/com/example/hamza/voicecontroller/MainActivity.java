package com.example.hamza.voicecontroller;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity
{
    // Properties
    public static String EXTRA_MESSAGE;
    private final int CHECK_CODE = 0x1;
    private final int LONG_DURATION = 5000;
    private final int SHORT_DURATION = 1200;
    private VoiceGenerator speaker;

    // Methods
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // textToSpeech Initialization
        checkTTS();
    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view)
    {
        // Do something in response to button
        //   Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        //    intent.putExtra(EXTRA_MESSAGE, message);
//        startActivity(intent);

        // Text To Speech testing
        speaker.speak(message);
        System.out.println(message);
    }

    // method to check if a TTS engine is installed on the device.
    // The check is performed by making use of the result of another Activity.
    private void checkTTS()
    {
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, CHECK_CODE);
    }

    // If tts is not installed then prompt user to install it
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == CHECK_CODE)
        {
            if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
            {
                speaker = new VoiceGenerator(this);
            }
            else
            {
                Intent install = new Intent();
                install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);
            }
        }
    }

    // shutdown tts
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        speaker.destroy();
    }
}
