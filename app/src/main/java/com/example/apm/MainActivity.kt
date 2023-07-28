package com.example.apm

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import com.cnting.apm_lib.Matrix
import com.cnting.apm_lib.listener.DefaultPluginListener
import com.cnting.apm_lib.report.Issue
import com.cnting.apm_trace_canary.TracePlugin
import com.example.apm.ui.TraceActivity
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.startMatrix).setOnClickListener {
            val matrix = Matrix.Builder(application)
                .plugin(TracePlugin())
//                .pluginListener(pluginListener)
                .build()
            matrix.startAllPlugin()
        }
        findViewById<View>(R.id.toTrace).setOnClickListener {
            startActivity(Intent(this, TraceActivity::class.java))
        }
    }

    private val pluginListener = object : DefaultPluginListener() {
        override fun onReportIssue(issue: Issue) {
            super.onReportIssue(issue)
        }
    }
}
