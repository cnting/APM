package com.example.apm

import android.app.Application
import com.cnting.apm_crash.CrashPluginConfig
import com.cnting.apm_crash.CrushPlugin
import com.cnting.apm_lib.Matrix
import com.cnting.apm_trace_canary.TracePlugin

/**
 * Created by cnting on 2023/10/28
 *
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val matrix = Matrix.Builder(this)
            .plugin(TracePlugin())
            .plugin(CrushPlugin(CrashPluginConfig(homeActivityName = MainActivity::class.java.name)))
//                .pluginListener(pluginListener)
            .build()
        matrix.startAllPlugin()
    }
}