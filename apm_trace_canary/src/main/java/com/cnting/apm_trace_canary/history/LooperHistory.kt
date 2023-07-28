package com.cnting.apm_trace_canary.history

import android.os.HandlerThread
import com.cnting.apm_trace_canary.constant.Constants
import com.cnting.apm_trace_canary.constant.HistoryConstants
import com.cnting.apm_trace_canary.core.LooperDispatchListener
import com.cnting.apm_trace_canary.util.FIFOQueue
import com.cnting.apm_trace_canary.util.MessageUtil

/**
 * Created by cnting on 2023/7/27
 * 记录消息队列历史
 * 聚合规则：
 * 1. 累计耗时超过阈值(比如300ms)，将这些消息合并成一条记录
 * 2. 单条消息耗时严重时单独记录
 * 3. 四大组件消息单独记录
 * 4. idle状态间隔较长的也要单独记录
 */
class LooperHistory : LooperDispatchListener() {

    private val msgQueue = FIFOQueue<Msg>(100)
    private var curBoxMessage: BoxMessage? = null
    private var curClusterMsg: Msg? = null
    private var lastMsgEndNs: Long = -1

    override fun isValid(): Boolean {
        return true
    }

    override fun dispatchStart(s: String, beginNs: Long, cpuBeginMs: Long) {
        tryAddGapMsg(beginNs)
        curBoxMessage = MessageUtil.parse(s)
    }

    override fun dispatchEnd(
        s: String, beginNs: Long, cpuBeginMs: Long, endNs: Long, cpuEndMs: Long
    ) {
        val message = curBoxMessage ?: return
        lastMsgEndNs = endNs
        val wallTime = (endNs - beginNs) / Constants.TIME_MILLIS_TO_NANO
        val cpuTime = cpuEndMs - cpuBeginMs
        val isSystemMsg = MessageUtil.isSystemMessage(message)
        if (isSystemMsg || wallTime >= HistoryConstants.WARN_TIME) {
            //当前存在一个聚合消息，要先保存起来
            if (curClusterMsg != null) {
                addClusterMsg()
            }
            if (isSystemMsg) {
                addSystemMsg(message, wallTime, cpuTime)
            } else {
                addFatMsg(message, wallTime, cpuTime)
            }
        } else {
            addNormalMsg(message, wallTime, cpuTime)
        }
    }

    private fun tryAddGapMsg(beginNs: Long) {
        if (lastMsgEndNs < 0) return
        val duration = (beginNs - lastMsgEndNs) / Constants.TIME_MILLIS_TO_NANO
        if (duration > HistoryConstants.GAP_TIME) {
            msgQueue.add(Msg(type = Msg.TYPE_GAP, wallTime = duration))
        }
    }

    private fun addSystemMsg(message: BoxMessage, wallTime: Long, cpuTime: Long) {
        msgQueue.add(
            Msg(
                type = Msg.TYPE_SYSTEM,
                wallTime = wallTime,
                cpuTime = cpuTime,
                target = message.target,
                callback = message.callback,
                what = message.what
            )
        )
    }

    private fun addFatMsg(message: BoxMessage, wallTime: Long, cpuTime: Long) {
        // TODO: 获取栈信息
        msgQueue.add(
            Msg(
                type = Msg.TYPE_FAT,
                wallTime = wallTime,
                cpuTime = cpuTime,
                target = message.target,
                callback = message.callback,
                what = message.what
            )
        )
    }

    private fun addNormalMsg(message: BoxMessage, wallTime: Long, cpuTime: Long) {
        var clusterMsg = curClusterMsg
        //合并后超过300ms，分两个消息
        if (clusterMsg != null
            && (clusterMsg.wallTime + wallTime) >= HistoryConstants.WARN_TIME
        ) {
            addClusterMsg()
            clusterMsg = null
        }
        if (clusterMsg == null) {
            clusterMsg = Msg(type = Msg.TYPE_CLUSTER)
        }
        clusterMsg.wallTime += wallTime
        clusterMsg.cpuTime += cpuTime
        clusterMsg.count++
        //聚合消息只记录最后一个消息的target、callback、what
        clusterMsg.target = message.target
        clusterMsg.callback = message.callback
        clusterMsg.what = message.what
    }

    private fun addClusterMsg() {
        curClusterMsg?.run { msgQueue.add(this) }
    }
}