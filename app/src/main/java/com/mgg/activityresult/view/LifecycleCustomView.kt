package com.mgg.activityresult.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

open class LifecycleCustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs), LifecycleObserver {
    init {
        (context as? AppCompatActivity)?.lifecycle?.addObserver(this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        Log.e("mgg", "LifecycleWindow :onFinishInflate")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.e("mgg", "LifecycleWindow :onAttachedToWindow")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.e("mgg", "LifecycleWindow :onDetachedFromWindow")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        Log.e("mgg", "LifecycleWindow :onCreate")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        Log.e("mgg", "LifecycleWindow :onDestroy")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        Log.e("mgg", "LifecycleWindow :onPause")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        Log.e("mgg", "LifecycleWindow :onStop")
        this.visibility = View.GONE
        (context as? AppCompatActivity)?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        Log.e("mgg", "LifecycleWindow :onResume")
        this.visibility = View.VISIBLE
        (context as? AppCompatActivity)?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        Log.e("onWindowVisibilityChanged", "visibility$visibility")
    }
}