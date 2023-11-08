package com.cnting.apm_lib

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.cnting.apm_lib.db.APMDatabase
import com.cnting.apm_lib.db.DBRepository
import com.cnting.apm_lib.lifecycle.owners.ProcessUiLifecycleOwner
import com.cnting.apm_lib.listener.DefaultPluginListener
import com.cnting.apm_lib.listener.PluginListener
import com.cnting.apm_lib.plugin.Plugin
import com.cnting.apm_lib.util.DisplayUtil
import java.lang.RuntimeException

/**
 * Created by cnting on 2023/7/24
 *
 */
class APM private constructor(
    val application: Application,
    private val plugins: Set<Plugin>,
    private var pluginListener: PluginListener?,
    private val analyzeIssue: Boolean
) {
    companion object {
        const val TAG = "APM"

        @Volatile
        private var instance: APM? = null

        fun with(): APM {
            if (instance == null) {
                throw RuntimeException("you must init APM sdk first")
            }
            return instance!!
        }

        private fun init(apm: APM) {
            if (instance == null) {
                instance = apm
            } else {
                throw RuntimeException("APM instance is already set")
            }
        }
    }

    lateinit var dbRepository: DBRepository
        private set

    init {
        ProcessUiLifecycleOwner.init(application)
        initDB()
        if (analyzeIssue) {
            initAnalyzeLauncher()
        }
        plugins.run {
            val listener = pluginListener ?: DefaultPluginListener(dbRepository)
            forEach {
                it.init(application, listener)
            }
        }
        init(this)
    }

    private fun initDB() {
        val db = Room.databaseBuilder(application, APMDatabase::class.java, "apm").build()
        dbRepository = DBRepository(db)
    }

    private fun initAnalyzeLauncher() {
        DisplayUtil.showAnalyzeActivityInLauncher(application)
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
        private var analyzeIssue = true

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

        fun analyzeIssue(analyze: Boolean): Builder {
            this.analyzeIssue = analyze
            return this
        }

        fun build(): APM {
            return APM(application, plugins, pluginListener, analyzeIssue)
        }
    }


}