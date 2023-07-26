package com.cnting.apm_lib.report

import com.cnting.apm_lib.plugin.Plugin
import org.json.JSONObject

/**
 * Created by cnting on 2023/7/25
 *
 */
data class Issue(
    var type: Int = 0,
    val tag: String,
    val key: String? = null,
    val content: JSONObject,
    var plugin: Plugin? = null
) {
    companion object {
        const val ISSUE_REPORT_TYPE = "type"
        const val ISSUE_REPORT_TAG = "tag"
        const val ISSUE_REPORT_PROCESS = "process"
        const val ISSUE_REPORT_TIME = "time"
    }

    override fun toString(): String {
        return String.format(
            "tag[%s]type[%d];key[%s];content[%s]",
            tag,
            type,
            key,
            content.toString()
        )
    }
}
