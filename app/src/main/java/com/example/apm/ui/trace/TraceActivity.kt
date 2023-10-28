package com.example.apm.ui.trace

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import com.example.apm.R

/**
 * Created by cnting on 2023/7/26
 *
 */
class TraceActivity : ComponentActivity() {
    private val tag = "TraceActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trace)
        findViewById<View>(R.id.testANR).setOnClickListener {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                Log.d(tag, "start ANR message")
                Thread.sleep(6000)
                Log.d(tag, "end ANR message")
            }
        }
        findViewById<View>(R.id.testIdleHandler).setOnClickListener {
            Looper.getMainLooper().queue.addIdleHandler {
                Log.d(tag, "start IdleHandler message")
                Thread.sleep(3000)
                Log.d(tag, "end IdleHandler message")
                false
            }
        }
    }
}