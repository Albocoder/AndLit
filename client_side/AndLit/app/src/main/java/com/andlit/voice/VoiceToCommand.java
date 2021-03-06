package com.andlit.voice;

import android.content.Context;

public abstract class VoiceToCommand {
    protected Context c;
    public String name = null, last = null;

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
    public abstract int decide(String command);
}
