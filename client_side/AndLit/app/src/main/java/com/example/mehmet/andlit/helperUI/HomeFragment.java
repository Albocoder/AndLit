package com.example.mehmet.andlit.helperUI;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

import com.example.mehmet.andlit.MainActivity;
import com.example.mehmet.andlit.R;

/**
 * Created by Mehmet on 1/21/2018.
 */

public class HomeFragment extends Fragment {
    View myView;
    MainActivity homeActivity;
    FragmentManager fragmentManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.home_fragment_layout, container, false);
        return myView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(!homeActivity.voiceControlEnabled)
            homeActivity.findViewById(R.id.home_start_recording_button).setVisibility(View.INVISIBLE);
        homeActivity.findViewById(R.id.open_camera_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TakePhotoFragment tpf = new TakePhotoFragment();
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
            Toast.makeText(getApplicationContext(), "Speech Not Supported!", Toast.LENGTH_SHORT).show();
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
                tw.setText(result.get(0));
            }
        }
    }
}
