package com.andlit.voice;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.andlit.cloudInterface.Vision.VisionEndpoint;
import com.andlit.cloudInterface.Vision.models.Description;
import com.andlit.cloudInterface.Vision.models.Text;
import com.andlit.database.AppDatabase;
import com.andlit.database.entities.KnownPPL;
import com.andlit.face.FaceOperator;
import com.andlit.face.FaceRecognizerSingleton;
import com.andlit.settings.SettingsDefinedKeys;

import java.io.File;
import java.util.List;

public class VoiceSession {
    private static final String TAG = "VoiceSession";

    private VoiceGenerator speaker;
    private Boolean saveOnExit;
    private FaceOperator fop;
    private AppDatabase db;
    private FaceRecognizerSingleton frs;
    private List<KnownPPL> allKnownPpl;
    private VisionEndpoint vis;
    private VoiceToCommand vc;

    private Description d;
    private List<Text> t;

    public VoiceSession(Context c) {
        speaker = new VoiceGenerator(c);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        saveOnExit = sharedPref.getBoolean(SettingsDefinedKeys.SAVE_UNLABELED_ON_EXIT,false);
        db = AppDatabase.getDatabase(c);
        frs = new FaceRecognizerSingleton(c);

    }
}
