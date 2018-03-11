package com.example.mehmet.andlit.Settings;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.example.mehmet.andlit.R;
import android.support.v7.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat
{
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
