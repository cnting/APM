package com.cnting.apm_trace_canary.watchdog

import android.app.ActivityManager
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import java.lang.Exception
import java.lang.RuntimeException
import java.util.concurrent.Executors

/**
 * Created by cnting on 2023/11/4
 * 思路：在子线程，每隔5s往主线程插入一个消息，检查消息是否被执行
 */
class ANRWatchDog(private val context: Context) {

    private val anrHandlerThread = HandlerThread("ANRHandlerThread").apply { start() }
    private val anrHandler = Handler(anrHandlerThread.looper)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val checkRunnable = CheckRunnable(mainHandler)
    private val CHECK_TIME = 5000L
    private val threadPoolExecutor = Executors.newCachedThreadPool()

    //子线程每隔5秒向主线程插入一个消息，检测是否block
    private val scheduleCheckRunnable = Runnable {
        checkRunnable.check()
        val start = SystemClock.uptimeMillis()
        var timeout = CHECK_TIME
        //确保休眠5s
        while (timeout > 0) {
            try {
                Thread.sleep(timeout)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            timeout = CHECK_TIME - (SystemClock.uptimeMillis() - start)
        }
        if (checkRunnable.isBlock()) {
            checkRunnable.reset()
            doubtANR()
        }
        start()
    }

    private fun doubtANR() {
        Log.e("===>", "可能有ANR了")
        threadPoolExecutor.execute {
            val errorInfo = getErrorInfo()
            if (errorInfo != null) {
                val exception = RuntimeException()
                exception.stackTrace = checkRunnable.getThread().stackTrace
                exception.printStackTrace()
            }
        }
    }

    private fun getErrorInfo(): ActivityManager.ProcessErrorStateInfo? {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        //ANR堆栈获取需要时间，这里等等
        val loopTimes = 20
        var i = 0
        try {
            while (i++ < loopTimes) {
                val list = activityManager.processesInErrorState
                list?.forEach {
                    if (it.condition == ActivityManager.ProcessErrorStateInfo.NOT_RESPONDING) {
                        return it
                    }
                }
                Thread.sleep(500)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun start() {
        anrHandler.post(scheduleCheckRunnable)
    }

    fun stop() {
        anrHandler.removeCallbacks(scheduleCheckRunnable)
        anrHandlerThread.quitSafely()
    }

    private class CheckRunnable(val handler: Handler) : Runnable {
        private var isComplete = true

        fun check() {
            if (!isComplete) return
            isComplete = false
            //往主线程插入一个消息
            handler.postAtFrontOfQueue(this)
        }

        override fun run() {
            isComplete = true
        }

        fun isBlock() = !isComplete

        fun reset() {
            isComplete = true
        }

        fun getThread() = handler.looper.thread
    }

}