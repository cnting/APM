package com.cnting.apm_trace_canary.util

import com.cnting.apm_trace_canary.history.BoxMessage

/**
 * Created by cnting on 2023/7/27
 *
 */
object MessageUtil {
    /**
     * 解析：
     * logging.println(">>>>> Dispatching to " + msg.target + " "
     *                   + msg.callback + ": " + msg.what);
     * 比如：
     * >>>>> Dispatching to Handler (android.view.ViewRootImpl$ViewRootHandler) {3346d43} com.example.test.MainActivity$1@7250fab: 0
     */
    fun parse(msg: String): BoxMessage {
        //用()来分组，()里的?<target>是对这个group的命名
        val regex =
            Regex(">>>>> Dispatching to (?<target>Handler.*}) (?<callback>.*): (?<what>\\d)")

        val result = regex.find(msg)
        val target = result?.groups?.get("target")?.value
        val callback = result?.groups?.get("callback")?.value
        val what = result?.groups?.get("what")?.value
        return BoxMessage(
            target, callback, what
        )
    }

    fun isSystemMessage(message: BoxMessage):Boolean{
        return message.target!=null && message.target.contains("android.app.ActivityThread\$H")
    }
}