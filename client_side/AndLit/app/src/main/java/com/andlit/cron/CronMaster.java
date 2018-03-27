package com.andlit.cron;

import android.content.Context;

import com.andlit.cron.training.TrainingAlarmReceiver;

public class CronMaster {
    public static final int TRAINING_CODE = 0;
    public static final int SYNC_DETECTIONS_CODE = 1;


    public static boolean fireAllCrons(Context c) {
        // todo: add all other alarms here!

        boolean fired = new TrainingAlarmReceiver().setAlarm(c);
        // fired = ...
        return fired;
    }
}
