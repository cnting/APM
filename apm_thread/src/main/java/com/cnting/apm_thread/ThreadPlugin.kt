package com.cnting.apm_thread

import android.app.Application
import com.cnting.apm_lib.listener.PluginListener
import com.cnting.apm_lib.plugin.Plugin

/**
 * Created by cnting on 2023/11/7
 *
 */
class ThreadPlugin : Plugin() {
    private lateinit var nativeThreadMonitor: NativeThreadMonitor
    override fun init(application: Application, pluginListener: PluginListener) {
        super.init(application, pluginListener)
        nativeThreadMonitor = NativeThreadMonitor()
        nativeThreadMonitor.init()
    }

    override fun start() {
        super.start()
        nativeThreadMonitor.monitorThreadAlive()
    }

    // TODO: 要跟apm_trace_canary融合
    fun checkDeadLock() {
        nativeThreadMonitor.checkDeadLock()
    }
}