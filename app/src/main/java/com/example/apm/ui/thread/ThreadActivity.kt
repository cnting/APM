package com.example.apm.ui.thread

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import com.cnting.apm_lib.APM
import com.cnting.apm_thread.ThreadPlugin
import com.example.apm.R

/**
 * Created by cnting on 2023/11/6
 *
 */
class ThreadActivity : ComponentActivity() {
    val lock1 = Object()
    val lock2 = Object()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thread)

        findViewById<View>(R.id.checkDeadLockBtn).setOnClickListener {
            // TODO: 跟慢函数检测融合，自动检测死锁
            (APM.with().getPluginByClass(ThreadPlugin::class.java) as ThreadPlugin).checkDeadLock()
        }

        //测试死锁
        val thread1 = Thread(Runnable {
            synchronized(lock1) {
                Thread.sleep(2)
                synchronized(lock2) {
                    Log.d("===>", "Thread1")
                }
            }
        })
        val thread2 = Thread(Runnable {
            synchronized(lock2) {
                Thread.sleep(2)
                synchronized(lock1) {
                    Log.d("===>", "Thread2")
                }
            }
        })
        thread1.start()
        thread2.start()

        //检测线程存活时间
        Thread(Runnable { Thread.sleep(4000) }, "TestThread").start()

    }
}