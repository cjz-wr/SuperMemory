package com.example.supermemory;

import static android.content.Context.POWER_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";
    private static Ringtone ringtone;

    @Override
    public void onReceive(Context context, Intent intent) {
        // 获取 WakeLock 防止设备休眠
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                TAG + "::AlarmWakelock"
        );
        wakeLock.acquire(10 * 60 * 1000L); // 10分钟

        try {
            // 获取闹钟数据
            long noteId = intent.getLongExtra("note_id", -1);
            String title = intent.getStringExtra("title");
            String content = intent.getStringExtra("content");
            int notificationId = (int) noteId;

            // 1. 播放闹钟声音
            playAlarmSound(context);

            // 2. 发送带有关闭按钮的通知
            NotificationUtils.sendAlarmNotification(context, title, content, notificationId);

            Log.d(TAG, "闹钟触发: ID=" + noteId);
        } finally {
            wakeLock.release();
        }
    }

    private void playAlarmSound(Context context) {
        if (ringtone == null) {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            ringtone = RingtoneManager.getRingtone(context, alarmUri);
        }
        if (!ringtone.isPlaying()) {
            ringtone.play();
//            Log.d("AlarmDebug", "闹钟声音开始播放: URI=" + alarmUri);
        }
    }

    // 停止闹钟声音的公共方法（通过通知操作调用）
    public static void stopAlarmSound() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
            Log.d("AlarmDebug", "闹钟声音已停止");
        }
    }
}