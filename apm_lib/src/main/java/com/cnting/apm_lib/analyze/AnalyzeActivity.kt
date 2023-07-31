package com.cnting.apm_lib.analyze

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cnting.apm_lib.R
import com.cnting.apm_lib.db.IssueEntity
import com.yuyh.jsonviewer.library.JsonRecyclerView
import org.json.JSONObject

/**
 * Created by cnting on 2023/7/31
 *
 */
class AnalyzeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apm_analyze)
        val issue = intent.getParcelableExtra<IssueEntity>("issue")
        if (issue?.content == null) return
        val jsonView = findViewById<JsonRecyclerView>(R.id.jsonView)
        jsonView.bindJson(issue.content)
//        analyze(issue.content)
    }

    private fun analyze(s: String) {
        val content = JSONObject(s)
    }
}