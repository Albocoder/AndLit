package com.andlit.voice;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.andlit.ui.settings.SettingsDefinedKeys;

public class VoiceToCommandWrapper {

    public VoiceToCommand v;

    public VoiceToCommandWrapper(Context c) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        String locale = sharedPref.getString(SettingsDefinedKeys.LANGUAGE,"en");
        switch (locale) {
            //case("tr")
            default:
                v = new VoiceToCommandEnglish(c);
            break;
        }
    }

    public int decide(String command) {
        return v.decide(command);
    }
}
