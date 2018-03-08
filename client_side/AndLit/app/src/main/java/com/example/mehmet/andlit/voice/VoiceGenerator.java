package com.example.mehmet.andlit.voice;

import java.util.HashMap;
import java.util.Locale;
import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;


/**
 * Created by hamza
 * an encapuslation of google's text to speech engine
 * the constructor takes in a String input, initializes engine, speaks the input, and shuts down
 * In order to use this class. Simply create its instance and pass it the app context 
 * with code: VoiceGenerator speaker = new VoiceGenerator(this.getApplicationContext());
 * and then you can convert any string to voice with code: speaker.speak("Hi");
 */

public class VoiceGenerator implements TextToSpeech.OnInitListener
{
    // Properties
    private static TextToSpeech tts = null;
    private static boolean ready = false;
    private static Context context;

    // Constructor
    public VoiceGenerator(Context context)
    {
        this.context = context;

        if(tts == null)
            tts = new TextToSpeech(context, this);
    }

    // Methods

    /*
    This method is called when the TTS engine has been initialized.
    The status parameter lets us know if the initialization was successful.
    */
    @Override
    public void onInit(int status)
    {
        if(status == TextToSpeech.SUCCESS)
        {
            // Change this to match your locale
            tts.setLanguage(Locale.US);
            ready = true;
        }
        else
        {
            ready = false;
        }
    }

    // method which uses the engine to read out any text that is passed to it.
    public void speak(String text)
    {
        // Speak only if the TTS is ready
        if(ready && tts != null)
        {
            HashMap<String, String> hash = new HashMap<String,String>();
            hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                    String.valueOf(AudioManager.STREAM_NOTIFICATION));
            tts.speak(text, TextToSpeech.QUEUE_ADD, hash);
        }
    }

    /*
    method that plays silence for a specified duration.
    Using this method, we can add pauses to the speech to make it sound a little clearer
     */
    public void pause(int duration)
    {
        if(ready && tts != null)
        {
            tts.playSilence(duration, TextToSpeech.QUEUE_ADD, null);
        }
    }

    // Free up resources
    public void destroy()
    {
        if(tts != null)
        {
            tts.shutdown();
        }
    }


    /*
    method to check if a TTS engine is installed on the device.
    The check is performed by making use of the result of another Activity.
    */
//    private void checkTTS()
//    {
//        Intent check = new Intent();
//        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
//        startActivityForResult(check, 0x1);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data)
//    {
//        if(requestCode == 0x1)
//        {
//            if(resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
//            {
//                Intent install = new Intent();
//                install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
//                startActivity(install);
//            }
//        }
//    }
}
