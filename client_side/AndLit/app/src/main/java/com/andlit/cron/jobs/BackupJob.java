package com.andlit.cron.jobs;

import android.content.Context;
import android.os.PowerManager;
import android.support.annotation.NonNull;

import com.andlit.cloudInterface.synchronizers.classifier.ClassifierBackup;
import com.andlit.cloudInterface.synchronizers.database.DatabaseBackup;
import com.andlit.cloudInterface.synchronizers.photo.PhotoBackup;
import com.andlit.cloudInterface.synchronizers.photo.model.SinglePhotoResponse;
import com.evernote.android.job.Job;

import java.io.IOException;
import java.util.List;

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
        wl.acquire(3*60*60*1000L); // 3 hour top for the backup, even with bad internet
        boolean savedDetectionsAndRecognitions = false,savedDB = false, savedCls = false;
        PhotoBackup pb = new PhotoBackup(context);
        List<SinglePhotoResponse> allPhotos = null;

        try { allPhotos = pb.listAllPhotos(); } catch (IOException ignored) {}

        try {
            if(allPhotos != null)
                savedDetectionsAndRecognitions = pb.backupBoth(allPhotos);
        } catch (IOException ignored){}

        try {
            DatabaseBackup dbb = new DatabaseBackup(context);
            savedDB = dbb.backupDatabase(dbb.getInfoAboutUploadedDB());
        } catch (Throwable ignored) { }


        try {
            ClassifierBackup cb = new ClassifierBackup(context);
            savedCls = cb.backupClassifier(cb.getInfoAboutUploadedCls());
        } catch (Exception ignored){}


        if( savedCls && savedDB && savedDetectionsAndRecognitions )
            notifyUserOnCron(context,"AndLit backup success","Backup finished successfully",SYNC_CODE);
        else if( !savedCls && !savedDB && !savedDetectionsAndRecognitions ) {
            notifyUserOnCron(context,"AndLit backup failed","No internet connection or server down",SYNC_CODE);
            wl.release();
            return false;
        }
        else {
            if( !savedCls )
                notifyUserOnCron(context,"AndLit backup","Failed to backup classifier!",1111);
            if( !savedDB )
                notifyUserOnCron(context,"AndLit backup.","Failed to backup database!",2222);
            if( !savedDetectionsAndRecognitions )
                notifyUserOnCron(context,"AndLit backup.","Failed to backup detected and training faces!",3333);
        }
        wl.release();
        return true;
    }
}
