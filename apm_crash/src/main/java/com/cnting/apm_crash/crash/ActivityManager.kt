package com.cnting.apm_crash.crash

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.cnting.apm_crash.util.WeakStack
import com.cnting.apm_lib.lifecycle.owners.ProcessUiLifecycleOwner
import java.util.concurrent.CountDownLatch

/**
 * Created by cnting on 2023/10/28
 *
 */
class ActivityManager(private val bottomActivityName: String) :
    Application.ActivityLifecycleCallbacks {

    private val activityStack = WeakStack<Activity>()
    private var waitAllActivitiesDestroyCountDownLatch: CountDownLatch? = null

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        //从bottomActivity后的Activity开始记录
        if (activity::class.java.name == bottomActivityName) {
            //遇到bottomActivity，把之前的activity都清除
            activityStack.clear()
        } else {
            activityStack.add(activity)
        }
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        activityStack.remove(activity)
        waitAllActivitiesDestroyCountDownLatch?.countDown()
    }

    fun getLastActivity(): Activity {
        return activityStack.peek()
    }

    fun getAllActivities(): List<Activity> {
        return ArrayList(activityStack)
    }

    fun clearAllActivities() {
        activityStack.clear()
    }

    fun waitForAllActivitiesDestroy() {
        waitAllActivitiesDestroyCountDownLatch = CountDownLatch(activityStack.size)
        waitAllActivitiesDestroyCountDownLatch?.await()
        waitAllActivitiesDestroyCountDownLatch = null
        Log.i("===>", "now kill all activity")
    }


}