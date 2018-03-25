package com.andlit.voice;

import android.content.Context;

public abstract class VoiceToCommand {
    protected Context c;

    protected VoiceToCommand(Context c){
        this.c = c;
    }

    protected static boolean isIn(String [] theList, String toCheck) {
        toCheck = toCheck.toLowerCase();
        for (String s:theList) {
            if( s.equals(toCheck) )
                return true;
        }
        return false;
    }

    // what must be overwritten
    public abstract boolean decide(String command);
}
