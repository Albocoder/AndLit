package com.andlit.cron.jobs;

import android.content.Context;
import android.os.PowerManager;
import android.support.annotation.NonNull;

import com.andlit.face.FaceRecognizerSingleton;
import com.evernote.android.job.Job;

import static com.andlit.cron.CronMaster.TRAINING_CODE;
import static com.andlit.cron.CronMaster.notifyUserOnCron;

public class TrainingJob extends Job{
    public static final String TAG = "TrainingJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Context context = this.getContext();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if(pm == null)
            return Result.FAILURE;
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
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
        return Result.SUCCESS;
    }
}
