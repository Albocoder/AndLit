package com.example.mehmet.andlit.voice;

import android.content.Context;
import android.content.Intent;

import com.example.mehmet.andlit.helperUI.IntermediateCameraActivity;

import java.util.StringTokenizer;

public class VoiceToCommand {
    private static final String[] verbsForStarting = {"start","initialize","play","begin","commence","begin"};
    private static final String[] operations = {"camera","recognition","recognizer",
            "detection","detector","synchronization","synchronizer","training","trainer"};

    public static boolean decide(String command, Context c) {
        StringTokenizer t = new StringTokenizer(command);
        if(t.countTokens() > 2)
            return false;
        String verb = t.nextToken();
        String obj = t.nextToken();
        if(isIn(verbsForStarting,verb)){
            switch (obj){
                case ("camera"): case("recognition"):case("recognizer"): case("detection"): case("detector"):
                    Intent i = new Intent(c, IntermediateCameraActivity.class);
                    c.startActivity(i);
                    break;
                case("synchronization"): case("synchronizer"):
                    // todo: do this
                    break;
                case("training"): case("trainer"):
                    // todo: do this
                    break;
                default:
                    return false;
            }
        }
        // put some else-if's
        else {
            return false;
        }
        return false;
    }
    private static boolean isIn(String [] theList, String toCheck) {
        toCheck = toCheck.toLowerCase();
        for (String s:theList) {
            if( s.equals(toCheck) )
                return true;
        }
        return false;
    }
}
