package com.example.tetrisapp.util;

import android.content.Context;
import android.media.MediaPlayer;

public class MediaPlayerUtil {
    private Context context;

    public MediaPlayerUtil(Context context) {
        this.context = context;
    }

    public void playSound(int resId) {
        final MediaPlayer mediaPlayer = MediaPlayer.create(context, resId);
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        mediaPlayer.start();
    }
}
