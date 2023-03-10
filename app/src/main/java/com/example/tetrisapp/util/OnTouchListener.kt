package com.example.tetrisapp.util

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.view.View
import com.example.tetrisapp.R
import com.example.tetrisapp.ui.activity.MainActivity

class OnTouchListener(private val activity: Activity, private val callback: (View) -> Unit) :
    View.OnClickListener {
    private var mLastClickTime: Long = 0
    private var soundResId = R.raw.click

    fun setSound(resId: Int): OnTouchListener {
        soundResId = resId
        return this
    }

    override fun onClick(v: View) {
        (activity as MainActivity).hide()
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return
        mLastClickTime = SystemClock.elapsedRealtime()
        val mediaHelper = MediaPlayerUtil(v.context)
        val volume = activity.getPreferences(Context.MODE_PRIVATE).getInt(
            activity.getString(R.string.setting_sfx_volume), 5
        ) / 10f
        mediaHelper.playSound(soundResId, volume)
        callback(v)
    }
}