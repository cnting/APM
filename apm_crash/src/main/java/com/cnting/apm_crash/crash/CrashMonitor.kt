package com.cnting.apm_crash.crash

import android.app.Application
import android.os.Looper
import android.os.MessageQueue
import android.util.Log
import com.cnting.apm_crash.CrashPluginConfig

/**
 * Created by cnting on 2023/10/28
 *
 */
class CrashMonitor(private val application: Application, private val config: CrashPluginConfig) :
    MessageQueue.IdleHandler {
    fun start() {
        Looper.myQueue().addIdleHandler(this)
    }

    override fun queueIdle(): Boolean {
        val activityManager = ActivityManager(config.homeActivityName)
        application.registerActivityLifecycleCallbacks(activityManager)
        val processFinisher = ProcessFinisher(activityManager)
        // TODO: 这么处理有问题
//        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
//            exception.printStackTrace()
//            //这里上报服务器
//
//            //思路：遇到崩溃时，清空首页以上的所有Activity
//            processFinisher.finishAllActivities(thread)
//            processFinisher.endApplication()
//        }
        return false
    }
}