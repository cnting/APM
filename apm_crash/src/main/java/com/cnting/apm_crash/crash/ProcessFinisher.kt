package com.cnting.apm_crash.crash

import android.content.Context
import android.util.Log

/**
 * Created by cnting on 2023/10/28
 *
 */
class ProcessFinisher(private val activityManager: ActivityManager) {
    private val TAG = "ProcessFinisher"
    fun finishAllActivities(uncaughtExceptionThread: Thread) {
        var wait = false
        activityManager.getAllActivities().forEach { activity ->
            val isMainThread = uncaughtExceptionThread == activity.mainLooper.thread
            val finisher = Runnable {
                activity.finish()
                Log.i(TAG, "Finished $activity")
            }
            if (isMainThread) {
                finisher.run()
            } else {
                wait = true
                activity.runOnUiThread(finisher)
            }
        }
        if (wait) {
            activityManager.waitForAllActivitiesDestroy()
        }
        activityManager.clearAllActivities()
    }

    fun endApplication() {
        val exitType = 10
        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(exitType)
    }

}