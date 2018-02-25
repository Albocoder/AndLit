package com.example.mehmet.andlit.voice;

/**
 * Created by Mehmet on 2/25/2018.
 */

import java.util.HashMap;
import java.util.Locale;
import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;

/**
 * Created by hamza
 * an encapuslation of google's text to speech engine
 * the constructor takes in a String input, initializes engine, speaks the input, and shuts down
 */

public class VoiceGenerator implements TextToSpeech.OnInitListener
{
    // Properties
    private TextToSpeech tts;
    private boolean ready;

    // Constructors
    public VoiceGenerator(Context context)
    {
        ready = false;
        tts = new TextToSpeech(context, this);
    }

    // Methods
    @Override
    public void onInit(int status)
    {
        if(status == TextToSpeech.SUCCESS)
        {
            // Change this to match your
            // locale
            tts.setLanguage(Locale.US);
            ready = true;
        }
        else
        {
            ready = false;
        }
    }

    // method which uses the engine to read out any text that is passed to it.
    // Before doing so, it checks if both the allowed and the ready values are true.
    // The speech it generates is placed in the notification stream.
    public void speak(String text)
    {
        // Speak only if the TTS is ready
        // and the user has allowed speech

        if(ready)
        {
            HashMap<String, String> hash = new HashMap<String,String>();
            hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                    String.valueOf(AudioManager.STREAM_NOTIFICATION));
            tts.speak(text, TextToSpeech.QUEUE_ADD, hash);
        }
    }

    // method that plays silence for a specified duration.
    // Using this method, we can add pauses to the speech to make it sound a little clearer
    public void pause(int duration)
    {
        tts.playSilence(duration, TextToSpeech.QUEUE_ADD, null);
    }

    // method to free up resources when the TTS engine is no longer needed.
    public void destroy()
    {
        tts.shutdown();
    }
}