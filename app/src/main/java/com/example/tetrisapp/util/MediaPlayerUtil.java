package com.example.tetrisapp.util;

import android.content.Context;
import android.media.MediaPlayer;

public class MediaPlayerUtil {
    private final Context context;

    public MediaPlayerUtil(Context context) {
        this.context = context;
    }

    public void playSound(int resId) {
        playSound(resId, 1f);
    }

    public void playSound(int resId, float volume) {
        final MediaPlayer mediaPlayer = MediaPlayer.create(context, resId);
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        mediaPlayer.setVolume(volume, volume);
        mediaPlayer.start();
    }
}
