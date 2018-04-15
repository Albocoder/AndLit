package com.andlit.cron;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.andlit.R;

public class CronMaster {
    static final int TRAINING_CODE = 0;
    static final int SYNC_CODE = 1;


    public static boolean fireAllCrons(Context c) {
        boolean fired = fireTrainingAlarm(c);
         fired &= fireSyncAlarm(c);
        return fired;
    }

    /** *********************************** Static functions *********************************** **/
    static void notifyUserOnCron(Context context, String title, String msg, int id) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        // todo: get user preference of notifications on cron
//        if(user doesnt want to be notified)
//            return;
        NotificationCompat.Builder mBuilder = new
                NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.icon);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        int currentApiVersion = Build.VERSION.SDK_INT;

        Notification notification;
        if (currentApiVersion >= Build.VERSION_CODES.JELLY_BEAN)
            notification = mBuilder.build();
        else
            notification = mBuilder.getNotification();
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.FLAG_AUTO_CANCEL;
        notification.flags = Notification.DEFAULT_LIGHTS
                | Notification.FLAG_AUTO_CANCEL;

        if(notificationManager != null)
            notificationManager.notify(id, notification);
    }

    /* ************************ Alarm firing routines ****************************/
    public static boolean fireTrainingAlarm(Context c) {
        boolean alarmUp = (PendingIntent.getBroadcast(c, TRAINING_CODE,
                new Intent(c, TrainingAlarmReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);
        return alarmUp || new TrainingAlarmReceiver().setAlarm(c);
    }

    public static boolean fireSyncAlarm(Context c) {
        boolean alarmUp = (PendingIntent.getBroadcast(c, SYNC_CODE,
                new Intent(c, DataBackupAlarmReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);
        return alarmUp || new DataBackupAlarmReceiver().setAlarm(c);
    }
}
