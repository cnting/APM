package com.cnting.apm_lib.lifecycle.owners

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle

/**
 * Created by cnting on 2023/7/25
 *
 */
object ProcessUiLifecycleOwner {
    var visibleScene = "default"
        private set
    var isForeground: Boolean = false
        private set
        get() = startCounter > 0

    private var resumeCounter = 0
    private var startCounter = 0

    internal fun init(application: Application) {
        attach(application)
    }

    private fun attach(application: Application) {
        application.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityStarted(activity: Activity) {
                updateScene(activity)
                startCounter++
            }

            override fun onActivityResumed(activity: Activity) {
                resumeCounter++
            }

            override fun onActivityPaused(activity: Activity) {
                resumeCounter--
            }

            override fun onActivityStopped(activity: Activity) {
                startCounter--
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }
        })
    }

    private fun updateScene(activity: Activity) {
        visibleScene = activity.javaClass.name
    }
}