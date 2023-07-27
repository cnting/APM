package com.cnting.apm_trace_canary.history

import android.os.Handler

/**
 * Created by cnting on 2023/7/27
 *
 */
sealed class Msg(val type: Int) {
    var id: Int = 0

    //wallTime<300ms，聚合成一条数据
    data class ClusterMsg(
        var wallTime: Int = 0,
        var cpuTime: Int = 0,
        var counts: Int
    ) : Msg(TYPE_CLUSTER)

    //wallTime>=300ms
    data class FatMsg(
        var wallTime: Int = 0,
        var cpuTime: Int = 0,
        var target: Handler?,
        var callback: Runnable?,
        var stackTrace: String?
    ) : Msg(TYPE_FAT)

    //四大组件的消息
    //msg.target=ActivityThread.H
    data class SystemMsg(
        var target: Handler?,
        var callback: Runnable?,
        var what: Int
    ) : Msg(TYPE_SYSTEM)

    //正在调用的消息
    data class CurMsg(
        var wallTime: Int = 0,
        var cpuTime: Int = 0,
    ) : Msg(TYPE_CUR)

    //未执行的消息
    data class PendingMsg(
        var waitingTime: Long,
        var target: Handler?,
        var callback: Runnable?,
        var what: Int
    ) : Msg(TYPE_PENDING)

    companion object {
        const val TYPE_CLUSTER = 0
        const val TYPE_FAT = 1
        const val TYPE_SYSTEM = 2
        const val TYPE_CUR = 3
        const val TYPE_PENDING = 4
    }
}

data class BoxMessage(
    val target: String?,
    val callback: String?,
    val what: String?,
)

