package com.example.supermemory;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_EXACT_ALARM = 100;
    private static final int REQUEST_CODE_STORAGE = 101;
    private static final int REQUEST_CODE_BATTERY_OPTIMIZATION = 102;
    private static final int REQUEST_CODE_NOTIFICATION = 103; // 通知权限请求码
    private static final int PERMISSION_REQUEST_CODE = 0;

    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        Intent serviceIntent = new Intent(this, AlarmService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }



        // 初始化数据库
        db = new DatabaseHelper(this);

        // 初始化视图
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotesAdapter(this, db.getAllNotes());
        recyclerView.setAdapter(adapter);

        // 启动前台服务
        startForegroundServiceCompat();

        // 检查权限
        checkPermissions();

        // 设置FAB点击事件
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> openNoteEditor());
    }




    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_NOTIFICATION
                );
            }
        }
    }


    // 启动前台服务（兼容 Android 8.0+）
    private void startForegroundServiceCompat() {
        Intent serviceIntent = new Intent(this, AlarmService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);


        } else {
            startService(serviceIntent);
        }
    }

    // 打开笔记编辑界面
    private void openNoteEditor() {
        Intent intent = new Intent(this, NoteEditorActivity.class);
        startActivity(intent);
    }

    // 检查所有权限
    private void checkPermissions() {
        checkStoragePermission();
        checkExactAlarmPermission();
        checkBatteryOptimization();
        checkNotificationPermission(); // 检查通知权限
    }

    // 检查存储权限
    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    REQUEST_CODE_STORAGE
            );
        }
    }

    // 检查精确闹钟权限（Android 12+）
    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivityForResult(intent, REQUEST_CODE_EXACT_ALARM);
            }
        }
    }

    // 检查电池优化白名单
    private void checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }






    // 处理权限请求结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_STORAGE:
                handleStoragePermissionResult(grantResults);
                break;
            case REQUEST_CODE_NOTIFICATION: // 处理通知权限结果
                handleNotificationPermissionResult(grantResults);
                break;
        }
    }

    // 处理存储权限结果
    private void handleStoragePermissionResult(int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "存储权限已授予", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "存储权限被拒绝，文件功能将受限", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    // 处理通知权限结果
    private void handleNotificationPermissionResult(int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "通知权限已授予", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "通知权限被拒绝，您将无法收到提醒", Toast.LENGTH_SHORT).show();
            // 引导用户前往设置页
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            }
        }
    }

    // 处理Activity返回结果（精确闹钟/电池优化）
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_EXACT_ALARM:
                handleExactAlarmPermissionResult();
                break;
            case REQUEST_CODE_BATTERY_OPTIMIZATION:
                Log.d(TAG, "电池优化设置返回");
                break;
        }
    }

    // 处理精确闹钟权限结果
    private void handleExactAlarmPermissionResult() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "精确闹钟权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "精确闹钟权限被拒绝，闹铃可能不准确", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 刷新笔记列表
        refreshNotesList();
    }

    // 刷新RecyclerView数据
    private void refreshNotesList() {
        List<Note> notes = db.getAllNotes();
        adapter = new NotesAdapter(this, notes);
        recyclerView.setAdapter(adapter);
        Log.d(TAG, "笔记列表已刷新，数量：" + notes.size());
    }
}