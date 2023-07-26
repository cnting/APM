package com.cnting.apm_lib.listener

import android.util.Log
import com.cnting.apm_lib.plugin.Plugin
import com.cnting.apm_lib.report.Issue

/**
 * Created by cnting on 2023/7/25
 *
 */
class DefaultPluginListener : PluginListener {
    private val tag = "PluginListener"

    override fun onInit(plugin: Plugin) {
        Log.i(tag, "onInit:${plugin.getTag()}")
    }

    override fun onStart(plugin: Plugin) {
        Log.i(tag, "onStart:${plugin.getTag()}")
    }

    override fun onStop(plugin: Plugin) {
        Log.i(tag, "onStop:${plugin.getTag()}")
    }

    override fun onDestroy(plugin: Plugin) {
        Log.i(tag, "onDestroy:${plugin.getTag()}")
    }

    override fun onReportIssue(issue: Issue) {
        Log.e(tag, "onReportIssue:$issue")
    }
}