package com.example.supermemory;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtils {
    private static final String TAG = "FileUtils";
    private static final String EXPORT_DIR = "MyNotes";

    public static boolean exportNoteToFile(Context context, Note note) {
        File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), EXPORT_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            Toast.makeText(context, "无法创建导出目录", Toast.LENGTH_SHORT).show();
            return false;
        }

        String fileName = "Note_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date()) + ".txt";
        File file = new File(dir, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            String content = "标题: " + note.getTitle() + "\n\n" + note.getContent();
            fos.write(content.getBytes());
            Toast.makeText(context, "导出成功: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            Log.d(TAG, "笔记导出成功: " + file.getAbsolutePath());
            return true;
        } catch (Exception e) {
            Toast.makeText(context, "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "导出失败", e);
            return false;
        }
    }

    public static Note importNoteFromFile(Context context, File file) {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            String title = file.getName().replace(".txt", "");
            return new Note(0, title, content.toString(), "", file.getAbsolutePath(), null, false);
        } catch (Exception e) {
            Toast.makeText(context, "导入失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "导入失败", e);
            return null;
        }
    }

    public static boolean deleteFile(Context context, String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        File file = new File(filePath);
        if (file.exists() && file.delete()) {
            Toast.makeText(context, "文件删除成功", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "文件删除成功: " + filePath);
            return true;
        } else {
            Toast.makeText(context, "文件删除失败", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "文件删除失败: " + filePath);
            return false;
        }
    }

    public static String getExportDirPath(Context context) {
        File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), EXPORT_DIR);
        return dir.getAbsolutePath();
    }
}