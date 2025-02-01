package com.example.supermemory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "设备重启，恢复闹钟...");
            DatabaseHelper db = new DatabaseHelper(context);
            List<Note> notes = db.getAllNotes();
            for (Note note : notes) {
                if (note.isAlarmSet() && note.getAlarmTime() != null) {
                    AlarmScheduler.scheduleAlarm(context, note);
                }
            }
        }
    }
}