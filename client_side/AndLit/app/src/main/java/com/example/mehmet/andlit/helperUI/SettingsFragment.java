package com.example.mehmet.andlit.helperUI;

import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.example.mehmet.andlit.MainActivity;
import com.example.mehmet.andlit.R;

/**
 * Created by Mehmet on 1/21/2018.
 */

public class SettingsFragment extends Fragment {
    MainActivity mainActivity;
    boolean voiceControl = false;
    boolean soundControl = false;
    CheckBox voiceBox;
    CheckBox soundBox;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        voiceControl = getArguments().getBoolean("voiceControl");
        soundControl = getArguments().getBoolean("soundControl");
        return inflater.inflate(R.layout.settings_fragment_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        voiceBox = mainActivity.findViewById(R.id.voice_control_checkbox);
        soundBox = mainActivity.findViewById(R.id.sound_control_checkbox);
        voiceBox.setChecked(voiceControl);
        soundBox.setChecked(soundControl);
        mainActivity.findViewById(R.id.save_settings_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
            }
        });
    }

    private void saveSettings(){
        mainActivity.soundEnabled = soundBox.isChecked();
        mainActivity.voiceControlEnabled = voiceBox.isChecked();
        mainActivity.saveSettings();
        showSavedNotification();
    }

    private void showSavedNotification(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setMessage("Settings Saved");
        AlertDialog diag = builder.create();
        diag.show();
    }
}
