package com.example.mehmet.andlit.helperUI;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Mehmet on 2/12/2018.
 */

public class UILocalDBHelper {
    static boolean askForPassword;
    static int userID;
    Activity actv;

    public UILocalDBHelper(Activity actv){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(actv);
        askForPassword = sp.getBoolean("askForPassword", true);
        userID = sp.getInt("userID", -1);
        this.actv = actv;
    }

    Bundle getSettings(){
        return new Bundle();
    }
    void saveSettings(Bundle settings){

    }

    public boolean getAskForPassword(){
        return askForPassword;
    }
    public void setAskForPassword(boolean bool){
        askForPassword = bool;
        SharedPreferences.Editor edt = PreferenceManager.getDefaultSharedPreferences(actv).edit();
        edt.putBoolean("askForPassword", bool);
        edt.apply();
    }

    int getUserID(){
        return userID;
    }

    public void setUserID(int id){
        userID = id;
        SharedPreferences.Editor edt = PreferenceManager.getDefaultSharedPreferences(actv).edit();
        edt.putInt("userID", id);
        edt.apply();
    }
}
