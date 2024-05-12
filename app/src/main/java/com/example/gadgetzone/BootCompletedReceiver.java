package com.example.gadgetzone;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.send("Phone powered on");

            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo jobInfo = new JobInfo.Builder(0, new ComponentName(context, NotificationJobService.class))
                    .setPersisted(true)
                    .build();
            jobScheduler.schedule(jobInfo);
        }
    }
}
