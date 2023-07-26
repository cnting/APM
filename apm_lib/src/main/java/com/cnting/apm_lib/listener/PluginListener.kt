package com.cnting.apm_lib.listener

import com.cnting.apm_lib.plugin.Plugin
import com.cnting.apm_lib.report.Issue

/**
 * Created by cnting on 2023/7/24
 *
 */
interface PluginListener {
    fun onInit(plugin: Plugin)
    fun onStart(plugin: Plugin)
    fun onStop(plugin: Plugin)
    fun onDestroy(plugin: Plugin)
    fun onReportIssue(issue: Issue)
}