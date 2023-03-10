package com.example.tetrisapp.util

import android.content.Context
import android.media.MediaPlayer

class MediaPlayerUtil(private val context: Context) {
    fun playSound(resId: Int, volume: Float = 1f) {
        val mediaPlayer = MediaPlayer.create(context, resId)
        mediaPlayer.setOnCompletionListener { player -> player.release() }
        mediaPlayer.setVolume(volume, volume)
        mediaPlayer.start()
    }
}