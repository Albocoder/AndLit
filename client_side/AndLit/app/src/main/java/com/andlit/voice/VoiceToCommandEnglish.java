package com.andlit.voice;

import android.content.Context;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class VoiceToCommandEnglish extends VoiceToCommand {
    private static final Pattern photoVerb = Pattern.compile("take|capture|snap|shoot");
    private static final Pattern photoNoun = Pattern.compile("photo|image|photography|picture|snapshot");
    private static final Pattern startOperationVerb = Pattern.compile("perform|start|run|do|commence|begin|initiate");
    private static final Pattern operation1Noun = Pattern.compile("camera|image|photo");
    private static final Pattern operation2Noun = Pattern.compile("detection|face detection|detector");
    private static final Pattern operation3Noun = Pattern.compile("text|words|content");
    private static final Pattern operation4Noun = Pattern.compile("description|summary|depiction|explain|explanation|explainer");
    private static final Pattern operation5Noun = Pattern.compile("synchronization|synch|synchronizer|back up|backup");
    private static final Pattern operation6Noun = Pattern.compile("training|trainer|train");
    private static final Pattern operation7Noun = Pattern.compile("labeling|identifying|identification|recognition|face recognition|recognizer");
    private static final Pattern opVerbs1 = Pattern.compile("describe|explain|depict|tell|interpret");
    private static final Pattern opVerbs2 = Pattern.compile("read");
    private static final Pattern opVerbs3 = Pattern.compile("detect|show|analyze|get");
    private static final Pattern opVerbs4 = Pattern.compile("recognize|find|name");
    private static final Pattern opVerbs5 = Pattern.compile("insert|add|put");
    private static final Pattern opName5 = Pattern.compile("face|person|instance|recognition");
    private static final Pattern operation2_3Nouns = Pattern.compile("faces|face|people|humans|person|human");
    private static final Pattern clearOperationVerb = Pattern.compile("clean|clear|restart|renew|reset|delete");

    private static final Pattern questionWords1 = Pattern.compile("how many|How many");
    private static final Pattern questionWords2 = Pattern.compile("who|Who");


    VoiceToCommandEnglish(Context c) {
        super(c);
    }

    @Override
    public int decide(String command) {

        // first do the opVerbs
        if(opVerbs1.matcher(command).find())
            return 3;

        else if(opVerbs2.matcher(command).find())
            return 4;

        else if(opVerbs5.matcher(command).find()){
            if(opName5.matcher(command).find()){
                int nameIndex = command.indexOf("name")+4;
                String nameExtract = command.substring(nameIndex);
                StringTokenizer nameRetriever = new StringTokenizer(nameExtract);
                name = nameRetriever.nextToken();
                last = nameRetriever.nextToken();
                return 12;
            }
            else
                return -1;
        }

        // if it is a command for photo (with verb AND noun for fail-proof)
        else if(photoVerb.matcher(command).find()){
            if(photoNoun.matcher(command).find())
                return 1;
            else
                return -1;
        }

        // if it's actually a query
        else if(questionWords1.matcher(command).find()){
            if(operation2_3Nouns.matcher(command).find())
                return 9;
            else if(operation3Noun.matcher(command).find())
                return 10;
            else
                return -1;
        }
        else if(questionWords2.matcher(command).find()){
            return 11;
        }
//        else if(questionWords3.matcher(command).find()){
//
//        }

        // if it's a special operation
        else if(opVerbs3.matcher(command).find()){
            if(operation2_3Nouns.matcher(command).find())
                return 2;
            else
                return -1;
        }
        else if(opVerbs4.matcher(command).find()){
            if(operation2_3Nouns.matcher(command).find())
                return 7;
            else
                return -1;
        }
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
