package com.mgg.activityresult.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mgg.activityresult.R

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        findViewById<TextView>(R.id.appName).setOnClickListener {
            val intent = Intent()
            // 把返回数据存入Intent
            intent.putExtra("result", TestActivity::class.java.canonicalName)
            // 设置返回数据
            this.setResult(RESULT_OK, intent)
            // 关闭Activity
            this.finish()
        }
    }
}