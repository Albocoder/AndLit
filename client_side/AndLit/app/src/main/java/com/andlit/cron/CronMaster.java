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
import com.andlit.settings.SettingsDefinedKeys;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

public class CronMaster {
    public static final int TRAINING_CODE = 0;
    public static final int SYNC_CODE = 1;

    /* *********************************** Static functions *********************************** */
    public static void notifyUserOnCron(Context context, String title, String msg, int id) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(sharedPreferences.getBoolean(SettingsDefinedKeys.NOTIFY_SCHEDULED_TASKS,false))
            return;
        NotificationCompat.Builder mBuilder = new
                NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setVibrate(new long[]{0L})
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.mipmap.icon);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        int currentApiVersion = Build.VERSION.SDK_INT;

        Notification notification;
        if (currentApiVersion >= Build.VERSION_CODES.JELLY_BEAN)
            notification = mBuilder.build();
        else
            notification = mBuilder.getNotification();
        notification.defaults = Notification.DEFAULT_ALL;
        notification.flags = Notification.FLAG_AUTO_CANCEL;

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

    /* ************************ Alarm firing routines *************************** */

    public static void fireAllCrons(Context c) {
        scheduleJob(c,BackupJob.TAG,false);
        scheduleJob(c,TrainingJob.TAG,false);
    }

    public static void scheduleJob(Context c,String tag,boolean runnow) {
        JobManager.create(c).addJobCreator(new CronJobCreator());
        if(JobManager.instance().getAllJobRequestsForTag(TrainingJob.TAG).size() <= 0)
            rescheduleJob(c,tag,false);
    }

    public static void rescheduleJob(Context c,String tag,boolean runnow) {
        JobManager.create(c).addJobCreator(new CronJobCreator());
        long period,flex;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
        switch (tag){
            case(TrainingJob.TAG):
                String trainingFreq = sharedPreferences.getString(SettingsDefinedKeys.TRAINING_FREQUENCY
                        , "Never");
                if(trainingFreq.equalsIgnoreCase("Daily")) {
                    period = TimeUnit.DAYS.toMillis(1);
                    flex = TimeUnit.HOURS.toMillis(4);
                }
                else if(trainingFreq.equalsIgnoreCase("Weekly")){
                    period = TimeUnit.DAYS.toMillis(7);
                    flex = TimeUnit.HOURS.toMillis(12);
                }
                else if(trainingFreq.equalsIgnoreCase("Monthly")){
                    period = TimeUnit.DAYS.toMillis(30);
                    flex = TimeUnit.DAYS.toMillis(1);
                }
                else {
                    JobManager.instance().cancelAllForTag(TrainingJob.TAG);
                    return;
                }
                break;
            case (BackupJob.TAG):
                String backupFreq = sharedPreferences.getString(SettingsDefinedKeys.BACKUP_FREQUENCY
                        , "Daily");
                if(backupFreq.equalsIgnoreCase("Weekly")){
                    period = TimeUnit.DAYS.toMillis(7);
                    flex = TimeUnit.HOURS.toMillis(12);
                }
                else if(backupFreq.equalsIgnoreCase("Monthly")){
                    period = TimeUnit.DAYS.toMillis(30);
                    flex = TimeUnit.DAYS.toMillis(1);
                }
                else{
                    period = TimeUnit.DAYS.toMillis(1);
                    flex = TimeUnit.HOURS.toMillis(4);
                }
                break;
            /* ADD NEW LABELS HERE IF NEEDED TO SET UP THEIR PERIOD AND FLEX */
            default:
                return;
        }
        if(runnow)
            new JobRequest.Builder(tag)
                .setRequiresDeviceIdle(false)
                .setRequiresBatteryNotLow(true)
                .setRequirementsEnforced(true)
                .setPeriodic(period, flex)
                .setUpdateCurrent(true)
                .startNow()
                .build()
                .schedule();
        else
            new JobRequest.Builder(tag)
                    .setRequiresDeviceIdle(false)
                    .setRequiresBatteryNotLow(true)
                    .setRequirementsEnforced(true)
                    .setPeriodic(period, flex)
                    .setUpdateCurrent(true)
                    .build()
                    .schedule();
    }

    /* ************************ Alarm canceling routines *************************** */

    public static void cancelJob(Context c, String tag) {
        JobManager.create(c).addJobCreator(new CronJobCreator());
        JobManager.instance().cancelAllForTag(tag);
    }

    public static void cancelAllJobs(Context c) {
        JobManager.create(c).addJobCreator(new CronJobCreator());
        JobManager.instance().cancelAll();
    }
}
