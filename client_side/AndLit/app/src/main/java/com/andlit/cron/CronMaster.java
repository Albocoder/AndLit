package com.andlit.cron;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.andlit.cron.training.TrainingAlarmReceiver;

public class CronMaster {
    public static final int TRAINING_CODE = 0;
    public static final int SYNC_DETECTIONS_CODE = 1;


    public static boolean fireAllCrons(Context c) {
        boolean fired = fireTrainingAlarm(c);
        // todo: add all other alarms here!
        // fired &= ...

        return fired;
    }

    /* ************************ Alarm firing routines ****************************/
    public static boolean fireTrainingAlarm(Context c){
        boolean alarmUp = (PendingIntent.getBroadcast(c, TRAINING_CODE,
                new Intent(c, TrainingAlarmReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);
        boolean fired = true;
        if(!alarmUp)
            fired = new TrainingAlarmReceiver().setAlarm(c);
        return fired;
    }
}
