package com.cnting.apm_lib

import android.app.Application
import android.util.Log
import com.cnting.apm_lib.lifecycle.owners.ProcessUiLifecycleOwner
import com.cnting.apm_lib.listener.DefaultPluginListener
import com.cnting.apm_lib.listener.PluginListener
import com.cnting.apm_lib.plugin.IPlugin
import com.cnting.apm_lib.plugin.Plugin
import java.lang.RuntimeException

/**
 * Created by cnting on 2023/7/24
 *
 */
class Matrix private constructor(
    val application: Application,
    private val plugins: Set<Plugin>,
    private val pluginListener: PluginListener
) {
    companion object {
        const val TAG = "Matrix"
        private var instance: Matrix? = null

        fun with(): Matrix {
            if (instance == null) {
                throw RuntimeException("you must init Matrix sdk first")
            }
            return instance!!
        }

        private fun init(matrix: Matrix) {
            if (instance == null) {
                instance = matrix
            } else {
                Log.e(TAG, "Matrix instance is already set. this invoking will be ignored")
            }
        }
    }

    init {
        ProcessUiLifecycleOwner.init(application)
        plugins.forEach { it.init(application, pluginListener) }
        init(this)
    }

    fun startAllPlugin() {
        plugins.forEach { it.start() }
    }

    fun <T> getPluginByClass(pluginClass: Class<T>): Plugin? {
        return plugins.find { it.javaClass.name == pluginClass.name }
    }

    class Builder(private val application: Application) {
        private val plugins = mutableSetOf<Plugin>()
        private var pluginListener: PluginListener? = null

        fun plugin(plugin: Plugin): Builder {
            if (plugins.find { it.getTag() == plugin.getTag() } != null) {
                Log.e(TAG, "repeat add plugin：${plugin.getTag()}")
                return this
            }
            plugins.add(plugin)
            return this
        }

        fun pluginListener(listener: PluginListener): Builder {
            this.pluginListener = listener
            return this
        }

        fun build(): Matrix {
            return Matrix(application, plugins, pluginListener ?: DefaultPluginListener())
        }
    }
}