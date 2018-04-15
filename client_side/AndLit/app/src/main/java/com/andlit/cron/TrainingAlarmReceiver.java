package com.andlit.cron;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import com.andlit.R;
import com.andlit.face.FaceRecognizerSingleton;
import com.andlit.settings.SettingsDefinedKeys;

import java.util.Calendar;

import static com.andlit.cron.CronMaster.TRAINING_CODE;
import static com.andlit.cron.CronMaster.notifyUserOnCron;

public class TrainingAlarmReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if(pm == null)
            return;
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "classifier");
        wl.acquire(30*60*1000L /* operation shouldn't last more than 30 minutes*/);

        FaceRecognizerSingleton frs = new FaceRecognizerSingleton(context);
        if(frs.mustTrainConditions()) {
            notifyUserOnCron(context,"AndLit app training",
                    "Training of AndLit face recognizer started!",TRAINING_CODE);
            frs.trainModel();
            notifyUserOnCron(context,"AndLit app training",
                    "Training of AndLit finished successfully!",TRAINING_CODE);
        }
        else
            notifyUserOnCron(context,"AndLit training aborted!",
                    "Training wouldn\'t be helpful to increase accuracy!",TRAINING_CODE);

        wl.release();
    }

    public boolean setAlarm(Context context) {
        Intent i = new Intent(context, TrainingAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, TRAINING_CODE, i, 0);
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if(am == null)
            return false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String trainingValue = sharedPreferences.getString(SettingsDefinedKeys.TRAINING_FREQUENCY, "Never");
        String [] entries = context.getResources().getStringArray(R.array.TrainingFrequencyValues);
        if (trainingValue.equals(entries[0]))
                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis()
                                +AlarmManager.INTERVAL_DAY,AlarmManager.INTERVAL_DAY, pi);
        else if (trainingValue.equals(entries[1]))
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis()
                            +AlarmManager.INTERVAL_DAY * 7,AlarmManager.INTERVAL_DAY * 7, pi);
        else if (trainingValue.equals(entries[2]))
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis()
                            +AlarmManager.INTERVAL_DAY * 30,AlarmManager.INTERVAL_DAY * 30, pi);
        else
            cancelAlarm(context);
        return true;
    }

    public boolean cancelAlarm(Context context) {
        Intent intent = new Intent(context, TrainingAlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, TRAINING_CODE, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if( alarmManager == null )
            return false;
        alarmManager.cancel(sender);
        return true;
    }
}
