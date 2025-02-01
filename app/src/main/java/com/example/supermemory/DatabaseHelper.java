package com.example.supermemory;



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_NAME = "notes";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_FILE_PATH = "file_path";
    private static final String COLUMN_ALARM_TIME = "alarm_time";
    private static final String COLUMN_IS_ALARM_SET = "is_alarm_set";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_CONTENT + " TEXT,"
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_FILE_PATH + " TEXT,"
                + COLUMN_ALARM_TIME + " TEXT,"
                + COLUMN_IS_ALARM_SET + " INTEGER"
                + ")";
        db.execSQL(CREATE_TABLE);
        Log.d("DatabaseHelper", "数据库表已创建");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_FILE_PATH + " TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_ALARM_TIME + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_IS_ALARM_SET + " INTEGER");
        }
    }

    public long insertNote(String title, String content, String filePath, String alarmTime, boolean isAlarmSet) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_FILE_PATH, filePath);
        values.put(COLUMN_ALARM_TIME, alarmTime);
        values.put(COLUMN_IS_ALARM_SET, isAlarmSet ? 1 : 0);

        long id = db.insert(TABLE_NAME, null, values);
        db.close();

        if (id != -1) {
            Log.d("DatabaseHelper", "插入成功：ID=" + id);
        } else {
            Log.e("DatabaseHelper", "插入失败");
        }
        return id;
    }


    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_TIMESTAMP + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                note.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
                note.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                note.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_PATH)));
                note.setAlarmTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALARM_TIME)));
                note.setAlarmSet(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ALARM_SET)) == 1);
                notes.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        Log.d("DatabaseHelper", "查询到 " + notes.size() + " 条笔记");
        return notes;
    }

    public Note getNoteById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_CONTENT, COLUMN_TIMESTAMP, COLUMN_FILE_PATH, COLUMN_ALARM_TIME, COLUMN_IS_ALARM_SET},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        Note note = null;
        if (cursor != null && cursor.moveToFirst()) {
            note = new Note(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getInt(6) == 1
            );
            cursor.close();
        }
        db.close();
        return note;
    }

    public int updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_CONTENT, note.getContent());
        values.put(COLUMN_FILE_PATH, note.getFilePath());
        values.put(COLUMN_ALARM_TIME, note.getAlarmTime());
        values.put(COLUMN_IS_ALARM_SET, note.isAlarmSet() ? 1 : 0);

        int rowsAffected = db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(note.getId())});
        db.close();

        if (rowsAffected > 0) {
            Log.d("DatabaseHelper", "更新成功：ID=" + note.getId());
        } else {
            Log.e("DatabaseHelper", "更新失败：ID=" + note.getId());
        }
        return rowsAffected;
    }

    public void deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        Log.d("DatabaseHelper", "删除笔记：ID=" + id);
    }
}