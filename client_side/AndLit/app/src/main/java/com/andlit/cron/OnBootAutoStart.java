package com.andlit.cron;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.andlit.cron.training.TrainingAlarmReceiver;

// WARNING!!!! DON'T USE THIS CLASS! IT'S ONLY FOR AUTORUN!!!
public class OnBootAutoStart extends BroadcastReceiver {
    // all default alarms that autostart!
    private TrainingAlarmReceiver trainer = new TrainingAlarmReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            trainer.setAlarm(context);
        }
    }
}
