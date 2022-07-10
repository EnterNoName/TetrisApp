package com.example.tetris.util;

import android.content.Context;

import com.example.tetris.view.Tetris;

import org.json.JSONArray;
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

    public static int writeStats(Context context, Tetris.GameStatistics stats) {
        int score = stats.getScore(), lines = stats.getLines(), level = stats.getLevel();
        int highScore = 0;
        try {
            JSONObject data = JSON.read(context, "save.json");

            if (!data.isNull("bestAttempts")) {
                highScore = data.getJSONArray("bestAttempts").getJSONObject(0).getInt("score");

                JSONArray attempts = data.getJSONArray("bestAttempts");
                for (int i = 0; i < 5; i++) {
                    if (!attempts.isNull(i)) {
                        JSONObject attemptData = attempts.getJSONObject(i);
                        if (score > attemptData.getInt("score")) {
                            attemptData.put("score", score);
                            attemptData.put("lines", lines);
                            attemptData.put("level", level);
                            data.getJSONArray("bestAttempts").put(i, attemptData);
                            break;
                        }
                    } else {
                        JSONObject attemptData = new JSONObject();
                        attemptData.put("score", score);
                        attemptData.put("lines", lines);
                        attemptData.put("level", level);
                        data.getJSONArray("bestAttempts").put(attemptData);
                        break;
                    }
                }
            } else {
                data.put("bestAttempts", new JSONArray());
                JSONObject attemptData = new JSONObject();
                attemptData.put("score", score);
                attemptData.put("lines", lines);
                attemptData.put("level", level);
                data.getJSONArray("bestAttempts").put(attemptData);
            }

            JSON.write(context, "save.json", data, false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return highScore;
    }
}
