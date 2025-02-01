package com.example.supermemory;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmDismissReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getIntExtra("notification_id", 0);
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        AlarmReceiver.stopAlarmSound();
        if (manager != null) {
            manager.cancel(notificationId);
        }
        Log.d("AlarmDebug", "用户手动关闭闹钟");
    }
}