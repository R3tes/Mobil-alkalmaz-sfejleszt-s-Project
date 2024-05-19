package com.example.gadgetzone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.send("Come check out the latest products in our store!");
        }
    }
}