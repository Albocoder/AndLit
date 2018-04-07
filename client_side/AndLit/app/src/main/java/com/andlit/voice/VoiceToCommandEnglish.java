package com.andlit.voice;

import android.content.Context;
import android.content.Intent;

import com.andlit.UI.IntermediateCameraActivity;

import java.util.StringTokenizer;

public class VoiceToCommandEnglish extends VoiceToCommand {
    private static final String[] verbsForStarting = {"start","initialize","play","begin","commence","open"};
    private static final String[] operations = {"camera","recognition","recognizer",
            "detection","detector","synchronization","synchronizer","training","trainer"};

    public VoiceToCommandEnglish(Context c) {
        super(c);
    }

    @Override
    public boolean decide(String command) {
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
}
