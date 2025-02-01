package com.example.supermemory;

public class Note {
    private int id;
    private String title;
    private String content;
    private String timestamp;
    private String filePath;
    private String alarmTime; // 新增：定时时间
    private boolean isAlarmSet; // 新增：闹铃是否设置

    public Note() {}

    public Note(int id, String title, String content, String timestamp, String filePath, String alarmTime, boolean isAlarmSet) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.filePath = filePath;
        this.alarmTime = alarmTime;
        this.isAlarmSet = isAlarmSet;
    }

    // Getter/Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getAlarmTime() { return alarmTime; }
    public void setAlarmTime(String alarmTime) { this.alarmTime = alarmTime; }
    public boolean isAlarmSet() { return isAlarmSet; }
    public void setAlarmSet(boolean alarmSet) { isAlarmSet = alarmSet; }


}

