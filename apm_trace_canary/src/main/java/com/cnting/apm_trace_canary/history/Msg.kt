package com.cnting.apm_trace_canary.history

import android.os.Handler

/**
 * Created by cnting on 2023/7/27
 *
 */
data class Msg(
    val type: Int,
    var wallTime: Long = 0,
    var cpuTime: Long = 0,
    var count: Int = 0,  //聚合消息的个数
    var target: String? = null,
    var callback: String? = null,
    var what: String? = null,
    var stackTrace: String? = null,
    var waitingTime: Long? = 0,
) {
    companion object {
        const val TYPE_CLUSTER = 0  //wallTime<300ms，聚合成一条数据
        const val TYPE_FAT = 1      //wallTime>=300ms
        const val TYPE_SYSTEM = 2   //系统组件的消息 msg.target=ActivityThread.H
        const val TYPE_CUR = 3      //正在调用的消息
        const val TYPE_PENDING = 4  //未执行的消息
        const val TYPE_GAP = 5      //两条消息gap时间>=50ms
    }
}

data class BoxMessage(
    val target: String?,
    val callback: String?,
    val what: String?,
)

