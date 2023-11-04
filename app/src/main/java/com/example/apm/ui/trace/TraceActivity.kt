package com.example.apm.ui.trace

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import androidx.activity.ComponentActivity
import com.cnting.apm_trace_canary.watchdog.ANRWatchDog
import com.example.apm.R
import com.example.apm.ui.crash.CrashActivity

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
            //测试input ANR，第一次不会出现，连续点击会出现
            Log.d(tag, "start ANR message")
            Thread.sleep(20 * 1000)
            Log.d(tag, "end ANR message")
        }
        findViewById<View>(R.id.testIdleHandler).setOnClickListener {
            Looper.getMainLooper().queue.addIdleHandler {
                Log.d(tag, "start IdleHandler message")
                Thread.sleep(3000)
                Log.d(tag, "end IdleHandler message")
                false
            }
        }
        findViewById<Button>(R.id.anrWatchDogBtn).setOnClickListener(object : OnClickListener {
            var isStart = false
            var anrWatchDog = ANRWatchDog(this@TraceActivity)
            override fun onClick(view: View?) {
                if (isStart) {
                    anrWatchDog.stop()
                    (view as? Button)?.text = "ANRWatchDog已停止"
                } else {
                    anrWatchDog.start()
                    (view as? Button)?.text = "ANRWatchDog已启动"
                }
                isStart = !isStart
            }
        })

        //测试在onCreate是否会ANR，会出现
//        Thread.sleep(30 * 1000)
    }

    override fun onStop() {
        super.onStop()
        //测试在onStop是否会ANR，不会出现
        Log.e("===>", "点击onStop")
        Thread.sleep(30 * 1000)
        Log.e("===>", "onStop完成")
    }
}