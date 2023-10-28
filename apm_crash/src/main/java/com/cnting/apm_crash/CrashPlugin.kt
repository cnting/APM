package com.cnting.apm_crash

import android.app.Application
import com.cnting.apm_crash.crash.CrashMonitor
import com.cnting.apm_lib.listener.PluginListener
import com.cnting.apm_lib.plugin.Plugin

/**
 * Created by cnting on 2023/10/28
 *
 */
class CrushPlugin(private val config: CrashPluginConfig) : Plugin() {

    private lateinit var crashMonitor: CrashMonitor

    override fun init(application: Application, pluginListener: PluginListener) {
        super.init(application, pluginListener)
        crashMonitor = CrashMonitor(application, config)
    }

    override fun start() {
        super.start()
        crashMonitor.start()
    }
}

data class CrashPluginConfig(
    val homeActivityName: String
)