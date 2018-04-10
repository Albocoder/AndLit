package com.andlit.voice;

import android.content.Context;
import android.content.Intent;

import com.andlit.ui.IntermediateCameraActivity;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class VoiceToCommandEnglish extends VoiceToCommand {
    private static final Pattern photoVerb = Pattern.compile("get|take|do|capture|snap|shoot");
    private static final Pattern photoNoun = Pattern.compile("photo|image|photography|picture|snapshot");
    private static final Pattern startOperationVerb = Pattern.compile("perform|start|run|do|commence|begin|initiate");
    private static final Pattern operation1Noun = Pattern.compile("camera|image|photo");
    private static final Pattern operation2Noun = Pattern.compile("detection|face detection|detector");
    private static final Pattern operation3Noun = Pattern.compile("text|words|content");
    private static final Pattern operation4Noun = Pattern.compile("description|summary|depiction|explain|explanation|explainer");
    private static final Pattern operation5Noun = Pattern.compile("synchronization|synch|synchronizer|back up|backup");
    private static final Pattern operation6Noun = Pattern.compile("training|trainer|train");
    private static final Pattern operation7Noun = Pattern.compile("recognition|face recognition|recognizer");
    private static final Pattern opVerbs1 = Pattern.compile("describe|explain|depict|tell|interpret");
    private static final Pattern opVerbs2 = Pattern.compile("read");
    private static final Pattern clearOperationVerb = Pattern.compile("clean|clear|restart|renew|reset|delete");

//    private static final String[] questionWords1 = {"How many"};

    public VoiceToCommandEnglish(Context c) {
        super(c);
    }

    @Override
    public int decide(String command) {
//        StringTokenizer tokenizer = new StringTokenizer(command);
//        ArrayList<String> tokens = new ArrayList<>();
//        while (tokenizer.hasMoreTokens())
//            tokens.add(tokenizer.nextToken());

        // first do the opVerbs
        if(opVerbs1.matcher(command).find())
            return 3;

        else if(opVerbs2.matcher(command).find())
            return 4;

        // if it is a command for photo (with verb AND noun for fail-proof)
        else if(photoVerb.matcher(command).find()){
            if(photoNoun.matcher(command).find())
                return 1;
            else
                return -1;
        }

        // if it's actually a query
//        else if()

        // if it's an operation verb
        else if(startOperationVerb.matcher(command).find()) {
            if(operation1Noun.matcher(command).find())
                return 1;
            else if(operation2Noun.matcher(command).find())
                return 2;
            else if(operation3Noun.matcher(command).find())
                return 4;
            else if(operation4Noun.matcher(command).find())
                return 3;
            else if(operation5Noun.matcher(command).find())
                return 6;
            else if(operation6Noun.matcher(command).find())
                return 5;
            else if(operation7Noun.matcher(command).find())
                return 7;
            else
                return -1;
        }

        else if(clearOperationVerb.matcher(command).find()) {
            if(command.contains("session"))
                return 8;
            else
                return -1;
        }

        else
            return -1;
    }
}
