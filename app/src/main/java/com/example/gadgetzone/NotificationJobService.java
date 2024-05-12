package com.example.gadgetzone;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class NotificationJobService extends JobService {
    private  NotificationHelper mNotificationHelper;

    @Override
    public boolean onStartJob(JobParameters params) {
        mNotificationHelper = new NotificationHelper(getApplicationContext());
        mNotificationHelper.send("Check out our new products! We have a lot of new gadgets!");
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
