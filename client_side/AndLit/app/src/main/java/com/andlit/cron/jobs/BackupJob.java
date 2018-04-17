package com.andlit.cron.jobs;

import android.content.Context;
import android.os.PowerManager;
import android.support.annotation.NonNull;

import com.andlit.cloudInterface.synchronizers.classifier.ClassifierBackup;
import com.andlit.cloudInterface.synchronizers.database.DatabaseBackup;
import com.andlit.cloudInterface.synchronizers.photo.PhotoBackup;
import com.evernote.android.job.Job;

import java.io.IOException;

import static com.andlit.cron.CronMaster.SYNC_CODE;
import static com.andlit.cron.CronMaster.isNetworkAvailable;
import static com.andlit.cron.CronMaster.notifyUserOnCron;

public class BackupJob extends Job {
    public static final String TAG = "BackupJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        if(backupTheData(this.getContext()))
            return Result.SUCCESS;
        else
            return Result.FAILURE;
    }

    private boolean backupTheData(Context context){
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm == null)
            return false;
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        notifyUserOnCron(context, "AndLit backup", "Backing up your data!", SYNC_CODE);

        if ( !isNetworkAvailable(context) ) {
            notifyUserOnCron(context, "AndLit backup failed", "No internet connection", SYNC_CODE);
            return false;
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
        else if( !savedCls && !savedDB && !savedDetections && !savedTraining ) {
            notifyUserOnCron(context,"AndLit backup failed","No internet connection or server down",SYNC_CODE);
            return false;
        }
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
        return true;
    }
}
