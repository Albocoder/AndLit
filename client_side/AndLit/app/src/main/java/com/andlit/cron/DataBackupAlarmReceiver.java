package com.andlit.cron;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import com.andlit.cloudInterface.synchronizers.classifier.ClassifierBackup;
import com.andlit.cloudInterface.synchronizers.database.DatabaseBackup;
import com.andlit.cloudInterface.synchronizers.photo.PhotoBackup;

import java.io.IOException;

import static com.andlit.cron.CronMaster.SYNC_CODE;
import static com.andlit.cron.CronMaster.notifyUserOnCron;

public class DataBackupAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm == null)
            return;


        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "data");
        notifyUserOnCron(context, "AndLit backup", "Backing up your data!", SYNC_CODE);

        if ( !isNetworkAvailable(context) ) {
            notifyUserOnCron(context, "AndLit backup failed", "No internet connection", SYNC_CODE);
            return;
        }

        boolean savedDetections = false,savedTraining = false,savedDB = false, savedCls = false;
        PhotoBackup pb = new PhotoBackup(context);
        wl.acquire(30*60*1000L);
        try {
            savedDetections = pb.saveAllDetections();
        } catch (IOException ignored){}
        wl.release();

        wl.acquire(30*60*1000L);
        try {
            savedTraining = pb.saveAllTrainingData();
        } catch (IOException ignored){}
        wl.release();

        DatabaseBackup dbb = new DatabaseBackup(context);
        wl.acquire(30*60*1000L);
        try {
            savedDB = dbb.saveDatabase();
        } catch (Throwable ignored) { }
        wl.release();

        ClassifierBackup cb = new ClassifierBackup(context);
        wl.acquire(30*60*1000L);
        try {
            savedCls = cb.saveClassifier();
        } catch (IOException ignored){}
        wl.release();

        if( savedCls && savedDB && savedDetections && savedTraining )
            notifyUserOnCron(context,"AndLit backup success","Backup finished successfully",SYNC_CODE);
        else if( !savedCls && !savedDB && !savedDetections && !savedTraining )
            notifyUserOnCron(context,"AndLit backup failed","No internet connection or server down",SYNC_CODE);
        else {
            if( !savedCls )
                notifyUserOnCron(context,"AndLit backup","Failed to backup classifier!",1111);
            if( !savedDB )
                notifyUserOnCron(context,"AndLit backup.","Failed to backup database!",2222);
            if( !savedDetections )
                notifyUserOnCron(context,"AndLit backup.","Failed to backup detected faces!",3333);
            if( !savedTraining )
                notifyUserOnCron(context,"AndLit backup.","Failed to backup training faces!",4444);
        }
    }

    public boolean setAlarm(Context context) {
        Intent i = new Intent(context,DataBackupAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, SYNC_CODE, i, 0);
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if(am == null)
            return false;
        //todo: finish this function and decide on setting alarm on network change (if so delete line 36)
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return false;
    }

    public boolean cancelAlarm(Context context) {
        Intent i = new Intent(context,DataBackupAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, SYNC_CODE, i, 0);
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if(am == null)
            return false;
        am.cancel(pi);
        return true;
    }

    public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm == null)
            return true;
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
