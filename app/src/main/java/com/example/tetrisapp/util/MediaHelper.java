package com.example.tetrisapp.util;

import android.content.Context;
import android.media.MediaPlayer;

public class MediaHelper {
    private Context context;

    public MediaHelper(Context context) {
        this.context = context;
    }

    public void playSound(int resId) {
        final MediaPlayer mediaPlayer = MediaPlayer.create(context, resId);
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        mediaPlayer.start();
    }
}
