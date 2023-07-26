package com.cnting.apm_lib.plugin

import android.app.Application
import com.cnting.apm_lib.listener.PluginListener
import com.cnting.apm_lib.report.Issue
import com.cnting.apm_lib.report.OnIssueDetectListener
import com.cnting.apm_lib.util.MatrixUtil

/**
 * Created by cnting on 2023/7/24
 *
 */
abstract class Plugin : IPlugin, OnIssueDetectListener {

    private var application: Application? = null
    private var pluginListener: PluginListener? = null
    private var status = PLUGIN_CREATED

    override fun init(application: Application, pluginListener: PluginListener) {
        this.application = application
        this.pluginListener = pluginListener
        status = PLUGIN_INITED
        pluginListener.onInit(this)
    }

    override fun start() {
        status = PLUGIN_START
        pluginListener?.onStart(this)
    }

    override fun stop() {
        status = PLUGIN_STOP
        pluginListener?.onStop(this)
    }

    override fun destroy() {
        if (status == PLUGIN_START) {
            stop()
        }
        status = PLUGIN_DESTORY
        pluginListener?.onDestroy(this)
    }

    override fun getApplication(): Application? {
        return application
    }

    override fun getTag(): String {
        return javaClass.name
    }

    override fun onDetectIssue(issue: Issue) {
        issue.plugin = this
        val content = issue.content
        content.put(Issue.ISSUE_REPORT_TYPE, issue.type)
        content.put(Issue.ISSUE_REPORT_TAG, issue.tag)
        content.put(Issue.ISSUE_REPORT_PROCESS, MatrixUtil.getProcessName(application))
        content.put(Issue.ISSUE_REPORT_TIME, System.currentTimeMillis())
        pluginListener?.onReportIssue(issue)
    }

    companion object {
        const val PLUGIN_CREATED = 0x00
        const val PLUGIN_INITED = 0x01
        const val PLUGIN_START = 0x02
        const val PLUGIN_STOP = 0x03
        const val PLUGIN_DESTORY = 0x04
    }
}