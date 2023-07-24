package com.cnting.apm_lib

import android.app.Application
import com.cnting.apm_lib.plugin.IPlugin

/**
 * Created by cnting on 2023/7/24
 *
 */
class Matrix {
    inner class Builder(private val application: Application) {
        private val plugins = mutableSetOf<IPlugin>()
        fun addPlugin(plugin: IPlugin):Builder {
            if (!plugins.contains(plugin)) {
                plugins.add(plugin)
            }
            return this
        }
    }
}