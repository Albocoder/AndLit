package com.andlit.cron;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.andlit.cron.jobs.BackupJob;
import com.andlit.cron.jobs.TrainingJob;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class CronJobCreator implements JobCreator {

    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case BackupJob.TAG:
                return new BackupJob();
            case TrainingJob.TAG:
                return new TrainingJob();
            default:
                return null;
        }
    }
}
