package com.example.tetris.Utils;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class JSON {
    public static JSONObject read(Context context, String fileName) {
        try {
            createFile(context, fileName, false);
            FileInputStream fis = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                text.append(line);
            }

            return new JSONObject(text.toString());
        } catch (IOException | JSONException e) {
            return new JSONObject();
        }
    }

    public static boolean write(Context context, String fileName, JSONObject json, boolean append) {
        try {
            createFile(context, fileName, false);
            String jsonStr = json.toString(3);
            FileOutputStream fos;

            if (!append) {
                fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            } else {
                fos = context.openFileOutput(fileName, Context.MODE_APPEND);
            }

            fos.write(jsonStr.getBytes());
            fos.close();
            return true;
        } catch (JSONException | IOException e) {
            return false;
        }
    }

    public static void createFile(Context context, String fileName, boolean overrideExisting) {
        try {
            File file = new File(context.getFilesDir(), fileName);
            if (overrideExisting) {
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
            } else {
                if (!file.exists()) {
                    file.createNewFile();
                }
            }
        } catch (IOException ignored) {
        }
    }
}
