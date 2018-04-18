package com.andlit.cron;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.andlit.R;
import com.andlit.cron.jobs.BackupJob;
import com.andlit.cron.jobs.TrainingJob;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

public class CronMaster {
    public static final int TRAINING_CODE = 0;
    public static final int SYNC_CODE = 1;


    public static void fireAllCrons(Context c) {
        scheduleBackupJob(c);
        scheduleTrainingJob(c);
    }

    /************************************ Static functions ************************************/
    public static void notifyUserOnCron(Context context, String title, String msg, int id) {
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

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm == null)
            return false;
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /************************* Alarm firing routines ****************************/
    public static void scheduleBackupJob(Context c) {
        JobManager.create(c).addJobCreator(new CronJobCreator());
        // todo: add sharedpreferences to check settings about frequency
        // currently is only for 1 day with flex of 4 hours (can run up to 20 hours after last run)
        if(JobManager.instance().getAllJobRequestsForTag(BackupJob.TAG).size() <= 0) {
            new JobRequest.Builder(BackupJob.TAG)
                    .setRequiresDeviceIdle(false)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .setRequirementsEnforced(true)
                    .setPeriodic(TimeUnit.MINUTES.toMillis(16))
                    .setUpdateCurrent(false)
                    .build()
                    .schedule();
        }
    }

    public static void scheduleTrainingJob(Context c) {
        JobManager.create(c).addJobCreator(new CronJobCreator());
        if(JobManager.instance().getAllJobRequestsForTag(TrainingJob.TAG).size() <= 0) {
            new JobRequest.Builder(TrainingJob.TAG)
                    .setRequiresDeviceIdle(false)
                    .setRequiresBatteryNotLow(true)
                    .setRequirementsEnforced(true)
                    .setPeriodic(TimeUnit.DAYS.toMillis(1), TimeUnit.HOURS.toMillis(4))
                    .setUpdateCurrent(true)

                    .build()
                    .schedule();
        }
    }
}
