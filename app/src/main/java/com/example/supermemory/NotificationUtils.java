package com.example.supermemory;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationUtils {
    // 发送闹钟通知（带关闭按钮）
    public static void sendAlarmNotification(Context context, String title, String content, int notificationId) {
        createNotificationChannel(context);

        // 关闭闹钟的PendingIntent
        Intent dismissIntent = new Intent(context, AlarmDismissReceiver.class);
        dismissIntent.putExtra("notification_id", notificationId);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(context, "alarm_channel")
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_launcher_foreground, "关闭", dismissPendingIntent)
                .build();

        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notificationId, notification);
        }
    }

    // 创建高优先级通知渠道
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "alarm_channel",
                    "闹钟提醒",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("闹钟通知渠道");
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
}