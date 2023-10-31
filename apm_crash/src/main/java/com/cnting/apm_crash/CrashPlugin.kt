package com.cnting.apm_crash

import android.app.Application
import android.util.Log
import com.cnting.apm_crash.crash.JavaCrashMonitor
import com.cnting.apm_crash.crash.NativeCrashCallback
import com.cnting.apm_crash.crash.NativeCrashMonitor
import com.cnting.apm_lib.listener.PluginListener
import com.cnting.apm_lib.plugin.Plugin
import java.lang.Error

/**
 * Created by cnting on 2023/10/28
 *
 */
class CrushPlugin(private val config: CrashPluginConfig) : Plugin() {

    private lateinit var javaCrashMonitor: JavaCrashMonitor

    override fun init(application: Application, pluginListener: PluginListener) {
        super.init(application, pluginListener)
        javaCrashMonitor = JavaCrashMonitor(application, config)
    }

    override fun start() {
        super.start()
        javaCrashMonitor.start()
        NativeCrashMonitor.init(object : NativeCrashCallback {
            override fun onCrash(threadName: String, error: Error) {
                Log.e("CrashPlugin", "threadName:$threadName,error:${error.message}")
            }
        })
    }
}

data class CrashPluginConfig(
    val homeActivityName: String
)