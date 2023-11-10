package com.cnting.apm_io

import android.app.Application
import com.cnting.apm_io.core.IOCanaryCore
import com.cnting.apm_lib.listener.PluginListener
import com.cnting.apm_lib.plugin.Plugin

/**
 * Created by cnting on 2023/11/9
 *
 */
class IOPlugin : Plugin() {
    private lateinit var ioCanaryCore: IOCanaryCore
    override fun init(application: Application, pluginListener: PluginListener) {
        super.init(application, pluginListener)
        ioCanaryCore = IOCanaryCore()
    }

    override fun start() {
        super.start()
        ioCanaryCore.start()
    }

    override fun stop() {
        super.stop()
        ioCanaryCore.stop()
    }
}