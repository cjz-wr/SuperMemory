package com.example.supermemory;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import java.util.Calendar;

public class NoteEditorActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private EditText etTitle, etContent;
    private Button btnSetDateTime, btnRemoveAlarm, btnSave;
    private DatabaseHelper db;
    private Note existingNote;
    private Calendar alarmCalendar = Calendar.getInstance();
    private boolean isAlarmSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        // 初始化视图和数据库
        initViews();
        db = new DatabaseHelper(this);
        loadExistingNote();

        // 设置按钮事件
        btnSetDateTime.setOnClickListener(v -> showDateTimePicker());
        btnRemoveAlarm.setOnClickListener(v -> removeAlarm());
        btnSave.setOnClickListener(v -> saveNote());
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnSetDateTime = findViewById(R.id.btnSetDateTime);
        btnRemoveAlarm = findViewById(R.id.btnRemoveAlarm);
        btnSave = findViewById(R.id.btnSave);
    }

    private void loadExistingNote() {
        int noteId = getIntent().getIntExtra("note_id", -1);
        if (noteId != -1) {
            existingNote = db.getNoteById(noteId);
            if (existingNote != null) {
                etTitle.setText(existingNote.getTitle());
                etContent.setText(existingNote.getContent());
                if (existingNote.getAlarmTime() != null) {
                    alarmCalendar.setTimeInMillis(Long.parseLong(existingNote.getAlarmTime()));
                    isAlarmSet = true;
                    updateAlarmUI();
                }
            }
        }
    }

    private void showDateTimePicker() {
        // 先选择日期
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show(getSupportFragmentManager(), "DatePickerDialog");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        // 设置日期
        alarmCalendar.set(Calendar.YEAR, year);
        alarmCalendar.set(Calendar.MONTH, monthOfYear);
        alarmCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        // 然后选择时间
        Calendar now = Calendar.getInstance();
        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show(getSupportFragmentManager(), "TimePickerDialog");
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        // 设置时间
        alarmCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        alarmCalendar.set(Calendar.MINUTE, minute);
        alarmCalendar.set(Calendar.SECOND, 0);

        // 更新 UI
        isAlarmSet = true;
        updateAlarmUI();
        Toast.makeText(this, "闹铃已设置: " + alarmCalendar.getTime(), Toast.LENGTH_SHORT).show();
    }

    private void removeAlarm() {
        isAlarmSet = false;
        updateAlarmUI();
        Toast.makeText(this, "闹铃已移除", Toast.LENGTH_SHORT).show();
    }

    private void updateAlarmUI() {
        btnSetDateTime.setText(isAlarmSet ? "修改时间 (" + alarmCalendar.getTime() + ")" : "设置闹铃");
        btnRemoveAlarm.setVisibility(isAlarmSet ? View.VISIBLE : View.GONE);
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("标题不能为空");
            return;
        }

        try {
            if (existingNote == null) {
                // 新建笔记
                long id = db.insertNote(
                        title,
                        content,
                        null,
                        isAlarmSet ? String.valueOf(alarmCalendar.getTimeInMillis()) : null,
                        isAlarmSet
                );
                if (id != -1) {
                    scheduleAlarm(id, title, content);
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 更新笔记
                existingNote.setTitle(title);
                existingNote.setContent(content);
                existingNote.setAlarmTime(isAlarmSet ? String.valueOf(alarmCalendar.getTimeInMillis()) : null);
                existingNote.setAlarmSet(isAlarmSet);
                db.updateNote(existingNote);
                scheduleAlarm(existingNote.getId(), title, content);
                Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
            }
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleAlarm(long noteId, String title, String content) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("note_id", noteId);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putExtra("notification_id", (int) noteId); // 唯一通知ID

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) noteId, // 唯一requestCode
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (isAlarmSet) {
            // 设置闹铃
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmCalendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        alarmCalendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        } else {
            // 取消闹铃
            alarmManager.cancel(pendingIntent);
        }
    }
}