package com.example.mehmet.andlit.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.example.mehmet.andlit.R;

/**
 * Created by hamza on 3/10/18.
 */

public class SettingsController
{
    public static void loadSettings(Context context)
    {
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false); // sets default settings

        // example loading a setting
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(context);
        Boolean voiceControl = sharedPref.getBoolean
                (SettingsDefinedKeys.VOICE_CONTROL, false);

        /**
        The above code snippet uses PreferenceManager.getDefaultSharedPreferences(applicationcontext)
         to get the setting as a SharedPreferences object (sharedPref).getBoolean() to get the
         Boolean value of the setting that uses the key (VOICE_CONTROL defined in SettingsDefinedKeys)
         and assign it to voiceControl. If there is no value for the key, the getBoolean()
         method sets the setting value (voiceControl) to false.
         For other values such as strings, integers, or floating point numbers,
         you can use the getString(), getInt(), or getFloat() methods respectively.
        */
    }
}
