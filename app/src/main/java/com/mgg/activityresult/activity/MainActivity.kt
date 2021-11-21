package com.mgg.activityresult.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import com.mgg.activityresult.R
import com.mgg.activityresult.extensions.BetterActivityResult
import com.mgg.activityresult.extensions.isRunningForeground
import open.source.appstate.AppStateObserver

class MainActivity : AppCompatActivity(), AppStateObserver.OnChangeListener {
    private val mActivityLauncher: BetterActivityResult<Intent, ActivityResult> = BetterActivityResult.registerActivityForResult(this)
    private var mVideoView: VideoView? = null
    private var mMediaController: MediaController? = null
    private var mEnableZoom = false
    private var mVideoRatio = (9 / 16).toFloat()
    private var mEnableZoomInOut = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppStateObserver.addObserver(this)
        findViewById<TextView>(R.id.testBetterActivityResult).setOnClickListener {
            val intent = Intent(this, TestActivity::class.java)
            mActivityLauncher.launch(
                intent
            ) {
                Log.e("BetterActivityResult:", it.toString())
                it.resultCode
            }
        }
        findViewById<AppCompatButton>(R.id.btnStart).setOnClickListener {
            val videoPath = findViewById<AppCompatEditText>(R.id.etVideoPath).text.toString()
            if (videoPath.isNotEmpty()) {
                loadView(videoPath)
            } else {
                loadView("https://meeting-75420.picgzc.qpic.cn/launch_material/1623741507.mov")
                // https://meeting-75420.picgzc.qpic.cn/launch_material/1623741507.mov
                // loadView("http://200024424.vod.myqcloud.com/200024424_709ae516bdf811e6ad39991f76a4df69.f20.mp4")
            }
        }
        findViewById<AppCompatButton>(R.id.btnZoomInOut).setOnClickListener {
            if (!mEnableZoom) {
                return@setOnClickListener
            }
            if (mEnableZoomInOut) {
                updateControllerViewForBig()
                mVideoView?.pivotX = mVideoView?.width?.toFloat()!! / 2
                mVideoView?.pivotY = mVideoView?.height?.toFloat()!! / 2
                mVideoView?.scaleX = 1f
                mVideoView?.scaleY = 1f
            } else {
                updateTextureViewForSmall()
                mVideoView?.pivotX = mVideoView?.width?.toFloat()!!
                mVideoView?.pivotY = mVideoView?.height?.toFloat()!!
                mVideoView?.scaleX = 0.5f
                mVideoView?.scaleY = 0.5f
            }
            mEnableZoomInOut = !mEnableZoomInOut
        }

        mVideoView = findViewById(R.id.videoView)
        mMediaController = MediaController(this)
        // mVideoView?.setMediaController(mMediaController)
        mMediaController?.setMediaPlayer(mVideoView)
        loadView("https://meeting-75420.picgzc.qpic.cn/launch_material/1623741507.mov")
    }

    private fun loadView(path: String?) {
        val uri: Uri = Uri.parse(path)
        mVideoView?.setVideoURI(uri)
        mVideoView?.requestFocus()
        mVideoView?.start()
        mVideoView?.setOnPreparedListener { mp ->
            mVideoRatio = mp.videoWidth.toFloat() / mp.videoHeight.toFloat()
            val screenRatio = mVideoView?.width?.toFloat()!! / mVideoView?.height?.toFloat()!!
            val scaleX = mVideoRatio / screenRatio
            if (scaleX >= 1f) {
                mVideoView?.scaleX = scaleX
            } else {
                mVideoView?.scaleY = 1f / scaleX
            }
            updateControllerViewForBig()
            mp.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            mp.isLooping = true
            mp.start() // 播放
            mEnableZoom = true
            Toast.makeText(this@MainActivity, "开始播放！", Toast.LENGTH_LONG).show()
        }
        mVideoView?.setOnCompletionListener {
            Toast.makeText(this@MainActivity, "播放完毕", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        newConfig.let {
            updateControllerViewForBig()
            mVideoView?.scaleX = 1f
            mVideoView?.scaleY = 1f
        }
    }

    private fun setVideoContainerWidth(view: View) {
        val linearParams = view.layoutParams as RelativeLayout.LayoutParams // 取控件textView当前的布局参数
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            linearParams.width = dip2px(150f) // 控件的宽强制设成150
            linearParams.height = (dip2px(150f) * (1 / mVideoRatio)).toInt()// 控件的宽强制设成150
        } else if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            linearParams.width = dip2px(300f) // 控件的宽强制设成300
            linearParams.height = (dip2px(300f) * (1 / mVideoRatio)).toInt()// 控件的宽强制设成150
        }
        view.layoutParams = linearParams
    }

    private fun updateControllerViewForBig() {
        mVideoView?.let {
            val params = RelativeLayout.LayoutParams(it.width, it.height)
            params.addRule(RelativeLayout.CENTER_IN_PARENT)
            it.layoutParams = params
            setVideoContainerWidth(it)
        }
    }

    private fun updateTextureViewForSmall() {
        mVideoView?.let {
            val params = RelativeLayout.LayoutParams(it.width, it.height)
            params.addRule(RelativeLayout.ALIGN_PARENT_END)
            // params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            params.marginEnd = dip2px(30f)
            // params.bottomMargin = DisplayUtil.dip2px(30f)
            it.layoutParams = params
            setVideoContainerWidth(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppStateObserver.removeObserver(this)
        mVideoView?.stopPlayback()
    }

    @SuppressLint("SetTextI18n")
    override fun onChanged(isBackground: Boolean) {
        Log.e("onChanged:", isBackground.toString() + " " + this.application.isRunningForeground())
        if (isBackground && !this.application.isRunningForeground()) {
            findViewById<TextView>(R.id.testBetterActivityResult).text = " isBackground=$isBackground"
            return
        }

        if (!isBackground && this.application.isRunningForeground()) {
            findViewById<TextView>(R.id.testBetterActivityResult).text = " isBackground=$isBackground"
            return
        }
    }

    fun dip2px(dpValue: Float): Int {
        val scale = resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

}
