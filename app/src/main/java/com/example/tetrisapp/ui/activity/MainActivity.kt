package com.example.tetrisapp.ui.activity

import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.tetrisapp.BuildConfig
import com.example.tetrisapp.databinding.ActivityMainBinding
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pusher.client.Pusher
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var contentView: View
    var pusher: Pusher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        contentView = binding.fragmentContainerView
        contentView.setOnTouchListener { view: View, _: MotionEvent ->
            hide()
            view.performClick()
            false
        }

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        hide()
    }

    override fun onResume() {
        super.onResume()
        hide()
    }

    fun hide() {
        if (Build.VERSION.SDK_INT >= 30) {
            contentView.windowInsetsController!!.hide(
                WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
            )
        } else {
            WindowInsetsControllerCompat(window, contentView).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    private fun show() {
        if (Build.VERSION.SDK_INT >= 30) {
            contentView.windowInsetsController!!.show(
                WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
            )
        } else {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowInsetsControllerCompat(window, contentView).show(WindowInsetsCompat.Type.systemBars())
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}