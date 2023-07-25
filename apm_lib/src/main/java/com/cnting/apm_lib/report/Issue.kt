package com.cnting.apm_lib.report

import com.cnting.apm_lib.plugin.Plugin
import org.json.JSONObject

/**
 * Created by cnting on 2023/7/25
 *
 */
data class Issue(
    val type: Int = 0,
    val tag: String,
    val key: String? = null,
    val content: JSONObject,
)
