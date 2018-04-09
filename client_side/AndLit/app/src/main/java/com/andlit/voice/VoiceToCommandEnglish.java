package com.andlit.voice;

import android.content.Context;
import android.content.Intent;

import com.andlit.ui.IntermediateCameraActivity;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class VoiceToCommandEnglish extends VoiceToCommand {
    private static final String[] verbsForStarting = {"start","initialize","play","begin","commence","open","do"};
    private static final String[] questionWords = {"How","Who","What","Where"};
    private static final String[] operations = {"camera","recognition","recognizer",
            "detection","detector","synchronization","synchronizer","training","trainer"};

    public VoiceToCommandEnglish(Context c) {
        super(c);
    }

    @Override
    public int decide(String command) {
        StringTokenizer tokenizer = new StringTokenizer(command);
        ArrayList<String> tokens = new ArrayList<>();
        while (tokenizer.hasMoreTokens())
            tokens.add(tokenizer.nextToken());
        boolean startSomething = false;
        for (String t:tokens)
            if(isIn(verbsForStarting,t))
                startSomething = true;

// todo: develop this
//        if(isIn(verbsForStarting,verb)){
//            switch (obj){
//                case ("camera"): case("recognition"):case("recognizer"): case("detection"): case("detector"):
//                    Intent i = new Intent(c, IntermediateCameraActivity.class);
//                    c.startActivity(i);
//                    break;
//                case("synchronization"): case("synchronizer"):
//                    // todo: do this
//                    break;
//                case("training"): case("trainer"):
//                    // todo: do this
//                    break;
//                default:
//                    return 0;
//            }
//        }
        // put some else-if's
        else {
            return 0;
        }
        return 0;
    }
}
