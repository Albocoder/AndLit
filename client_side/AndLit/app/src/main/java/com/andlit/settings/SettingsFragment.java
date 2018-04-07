package com.andlit.settings;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.andlit.R;
import com.andlit.cron.CronMaster;

import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener
{
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    // Action Listeners for when settings change
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if( key.equals("training_frequency") )
        {
            // Do something when training frequency is changed
            Context context = this.getActivity().getApplicationContext();
            CronMaster.fireTrainingAlarm(context);
            CharSequence text = "Training Frequency Changed!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
