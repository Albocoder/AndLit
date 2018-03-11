package com.example.mehmet.andlit.helperUI;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

import com.example.mehmet.andlit.MainActivity;
import com.example.mehmet.andlit.R;
import com.example.mehmet.andlit.Settings.SettingsDefinedKeys;
import com.example.mehmet.andlit.voice.VoiceToCommand;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Mehmet on 1/21/2018.
 */

public class HomeFragment extends Fragment {
    View myView;
    MainActivity homeActivity;
    FragmentManager fragmentManager;
    TextView ttsView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.home_fragment_layout, container, false);
        return myView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Checking voiceControl Settings
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(homeActivity.getApplicationContext());
        boolean voiceControl = sharedPref.getBoolean
                (SettingsDefinedKeys.VOICE_CONTROL, false);
        if(!voiceControl)
            homeActivity.findViewById(R.id.home_start_recording_button).setVisibility(View.INVISIBLE);

        homeActivity.findViewById(R.id.open_camera_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //tpf.setMainActivity(homeActivity);
                //homeActivity.switchFragments(R.id.content_frame, tpf);                
                takePhoto();
            }
        });
        homeActivity.findViewById(R.id.home_start_recording_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptSpeechInput();
            }
        });

        ttsView = homeActivity.findViewById(R.id.home_tts);
    }

    void takePhoto(){
        Intent i = new Intent(homeActivity.getApplicationContext(), IntermediateCameraActivity.class);
        startActivity(i);
    }

    public void lul(FragmentManager fm){
        fm.beginTransaction().replace(this.getId(), new ShowImageFragment());
    }

    public void setHomeActivity(MainActivity mainActivity){
        homeActivity = mainActivity;
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
            Toast.makeText(homeActivity.getApplicationContext(), "Speech Not Supported!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // speechToText
        if(requestCode == 100)
        {
            if(resultCode == RESULT_OK && null != data)
            {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //    VoiceToCommand.decide(result.get(0),homeActivity.getApplicationContext());
                ttsView.setText(result.get(0));
            }
        }
    }
}
