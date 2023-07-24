package com.cnting.apm_lib.plugin

import android.app.Application
import com.cnting.apm_lib.listener.PluginListener

/**
 * Created by cnting on 2023/7/21
 *
 */
interface IPlugin {
    fun init(application: Application, pluginListener: PluginListener)
    fun start()
    fun stop()
    fun destroy()
    fun getTag(): String
    fun getApplication(): Application?
}