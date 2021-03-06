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
                mVideoView?.let {
                    it.pivotX = it.width.toFloat() / 2
                    it.pivotY = it.height.toFloat() / 2
                    it.scaleX = 1f
                    it.scaleY = 1f
                }
            } else {
                updateTextureViewForSmall()
                mVideoView?.let {
                    it.pivotX = it.width.toFloat()
                    it.pivotY = it.height.toFloat()
                    it.scaleX = 0.5f
                    it.scaleY = 0.5f
                }
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
            mVideoView?.let {
                val screenRatio = it.width.toFloat() / it.height.toFloat()
                val scaleX = mVideoRatio / screenRatio
                if (scaleX >= 1f) {
                    mVideoView?.scaleX = scaleX
                } else {
                    mVideoView?.scaleY = 1f / scaleX
                }
            }
            updateControllerViewForBig()
            mp.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            mp.isLooping = true
            mp.start() // ??????
            mEnableZoom = true
            Toast.makeText(this@MainActivity, "???????????????", Toast.LENGTH_LONG).show()
        }
        mVideoView?.setOnCompletionListener {
            Toast.makeText(this@MainActivity, "????????????", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        newConfig.let {
            updateControllerViewForBig()
            mVideoView?.scaleX = 1f
            mVideoView?.scaleY = 1f
            mEnableZoomInOut = false
        }
    }

    private fun setVideoContainerWidthHeight(view: View) {
        val linearParams = view.layoutParams as RelativeLayout.LayoutParams // ?????????textView?????????????????????
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (mVideoRatio < 1) {
                linearParams.width = dip2px(150f) // ????????????????????????150
                linearParams.height = (dip2px(150f) * (1 / mVideoRatio)).toInt()// ????????????????????????150
            } else {
                linearParams.width = dip2px(300f) // ????????????????????????150
                linearParams.height = (dip2px(300f) * (1 / mVideoRatio)).toInt()// ????????????????????????150
            }
        } else if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            linearParams.width = dip2px(300f) // ????????????????????????300
            linearParams.height = (dip2px(300f) * (1 / mVideoRatio)).toInt()// ????????????????????????150
        }
        view.layoutParams = linearParams
    }

    private fun updateControllerViewForBig() {
        mVideoView?.let {
            val params = RelativeLayout.LayoutParams(it.width, it.height)
            params.addRule(RelativeLayout.CENTER_IN_PARENT)
            it.layoutParams = params
            setVideoContainerWidthHeight(it)
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
            setVideoContainerWidthHeight(it)
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
