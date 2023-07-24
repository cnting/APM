package com.cnting.apm_trace_canary

import android.app.Application
import android.os.Looper
import com.cnting.apm_lib.listener.PluginListener
import com.cnting.apm_lib.plugin.IPlugin
import com.cnting.apm_lib.plugin.Plugin
import com.cnting.apm_lib.util.APMHandlerThread
import com.cnting.apm_trace_canary.core.UiThreadMonitor

/**
 * Created by cnting on 2023/7/21
 *
 */
class TracePlugin : Plugin() {
    override fun init(application: Application, pluginListener: PluginListener) {
        super.init(application, pluginListener)
    }

    override fun start() {
        super.start()
        val runnable = Runnable {
            if (!UiThreadMonitor.isInit) {
                UiThreadMonitor.init()
            }
            UiThreadMonitor.onStart()
        }
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            runnable.run()
        } else {
            APMHandlerThread.mainHandler.post(runnable)
        }
    }

    override fun stop() {
        super.stop()
    }

}