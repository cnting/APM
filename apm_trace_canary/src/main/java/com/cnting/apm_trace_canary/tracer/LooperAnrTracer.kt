package com.cnting.apm_trace_canary.tracer

import android.os.Handler
import android.os.Handler.Callback
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import android.util.Log
import com.cnting.apm_lib.Matrix
import com.cnting.apm_lib.lifecycle.owners.ProcessUiLifecycleOwner
import com.cnting.apm_lib.report.Issue
import com.cnting.apm_lib.util.DeviceUtil
import com.cnting.apm_trace_canary.TracePlugin
import com.cnting.apm_trace_canary.config.SharePluginInfo
import com.cnting.apm_trace_canary.constant.Constants
import com.cnting.apm_trace_canary.constant.HistoryConstants
import com.cnting.apm_trace_canary.core.UiThreadMonitor
import com.cnting.apm_trace_canary.bean.BoxMessage
import com.cnting.apm_trace_canary.bean.Msg
import com.cnting.apm_trace_canary.util.FIFOQueue
import com.cnting.apm_trace_canary.util.MessageUtil
import com.cnting.apm_trace_canary.util.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by cnting on 2023/7/24
 * ANR检测：新开一个线程发送ANR超时消息，5秒后如果该消息还在，ANR报错
 *
 * 消息队列聚合规则：
 * 1. 累计耗时超过阈值(比如300ms)，将这些消息合并成一条记录
 * 2. 单条消息耗时严重时单独记录
 * 3. 四大组件消息单独记录
 * 4. idle状态间隔较长的也要单独记录
 */
class LooperAnrTracer : Tracer() {

    private val msgQueue = FIFOQueue<Msg>(50)
    private val handlerThread = HandlerThread("LooperAnrTracer")
    private var handler: Handler? = null
    private var monitorMsgId = 0
    private var monitorReportAnrTime: Long = 0
    private var lastMsgEndNs: Long = -1
    private var curBoxMessage: BoxMessage? = null
    private val gson = Gson()

    override fun onAlive() {
        UiThreadMonitor.addObserver(this)
        handlerThread.start()
        handler = Handler(
            handlerThread.looper, handlerCallback
        )
        anrMonitorThread.start()
    }

    override fun onDead() {
        UiThreadMonitor.removeObserver(this)
        handlerThread.quitSafely()
    }

    override fun dispatchBegin(s: String, beginNs: Long, cpuBeginMs: Long, token: Long) {
        super.dispatchBegin(s, beginNs, cpuBeginMs, token)
        monitorMsgId++
        monitorReportAnrTime = SystemClock.elapsedRealtime() + Constants.DEFAULT_ANR
        curBoxMessage = MessageUtil.parse(s).apply {
            this.beginNs = beginNs
            this.cpuBeginMs = cpuBeginMs
        }
        handler?.sendEmptyMessage(MSG_START)
    }

    override fun dispatchEnd(
        s: String,
        beginNs: Long,
        cpuBeginMs: Long,
        endNs: Long,
        cpuEndMs: Long,
        token: Long,
        isVsyncFrame: Boolean
    ) {
        super.dispatchEnd(s, beginNs, cpuBeginMs, endNs, cpuEndMs, token, isVsyncFrame)
        curBoxMessage?.apply {
            this.endNs = endNs
            this.cpuEndMs = cpuEndMs
        }
        handler?.sendEmptyMessage(MSG_END)
        lastMsgEndNs = endNs
    }

    private val handlerCallback = object : Callback {
        private var curClusterMsg: Msg? = null

        override fun handleMessage(msg: Message): Boolean {
            when (msg.what) {
                MSG_START -> {
                    tryAddGapMsg(lastMsgEndNs, curBoxMessage?.beginNs ?: 0)
                }

                MSG_END -> {
                    val message = curBoxMessage ?: return true
                    val isSystemMsg = MessageUtil.isSystemMessage(message)
                    if (isSystemMsg || message.wallTime >= HistoryConstants.WARN_TIME) {
                        //当前存在一个聚合消息，要先保存起来
                        if (curClusterMsg != null) {
                            addClusterMsg(curClusterMsg)
                        }
                        if (isSystemMsg) {
                            addSystemMsg(message)
                        } else {
                            addFatMsg(message)
                        }
                    } else {
                        curClusterMsg = addNormalMsg(curClusterMsg, message)
                    }
                }

                MSG_ANR -> {
                    if (curClusterMsg != null) {
                        addClusterMsg(curClusterMsg)
                        curClusterMsg = null
                    }
                    curBoxMessage?.apply { addAnrMsg(this) }
                    addPendingMessage()
                    reportAnr()
                    clearQueue()
                }
            }
            return true
        }
    }

    private fun clearQueue() {
        msgQueue.clear()
    }

    private val anrMonitorThread = object : Thread() {
        private var msgId: Int = -1
        private var reportAnrTime: Long = -1

        override fun start() {
            super.start()
            updateReportAnrTimeFromNow()
        }

        override fun run() {
            while (isAlive) {
                //有消息在分发
                if (isDispatchBegin) {
                    val now = SystemClock.elapsedRealtime()
                    //该检查了
                    if (now > reportAnrTime) {
                        //还是同一个消息id，说明该消息5秒了还没执行完，报ANR
                        if (msgId == monitorMsgId) {
                            handler?.sendEmptyMessage(MSG_ANR)
                            updateReportAnrTimeFromNow()
                        } else {
                            msgId = monitorMsgId
                            reportAnrTime = monitorReportAnrTime
                        }
                    }
                } else {
                    //处于idle状态，5秒后再检查
                    updateReportAnrTimeFromNow()
                }
                val sleepTime: Long = reportAnrTime - SystemClock.elapsedRealtime()
                if (sleepTime > 0) {
                    SystemClock.sleep(sleepTime)
                }
            }
        }

        private fun updateReportAnrTimeFromNow() {
            reportAnrTime = SystemClock.elapsedRealtime() + Constants.DEFAULT_ANR
        }
    }

    private fun reportAnr() {
        //Thread state
        val status = Looper.getMainLooper().thread.state
        val stackTrace = Looper.getMainLooper().thread.stackTrace
        val dumpStack = Utils.getWholeStack(stackTrace)

        val isForeground = isForeground()

        //process
        val processStat = Utils.getProcessPriority(android.os.Process.myPid())
        val scene = ProcessUiLifecycleOwner.visibleScene

        //memory
        val memoryInfo = dumpMemory()

        val plugin = Matrix.with().getPluginByClass(TracePlugin::class.java) ?: return
        var jsonObject = JSONObject()
        jsonObject = DeviceUtil.getDeviceInfo(jsonObject, Matrix.with().application)
        jsonObject.put(SharePluginInfo.ISSUE_STACK_TYPE, Constants.Type.ANR)
        jsonObject.put(SharePluginInfo.ISSUE_SCENE, scene)
        jsonObject.put(SharePluginInfo.ISSUE_THREAD_STACK, dumpStack)
        jsonObject.put(SharePluginInfo.ISSUE_PROCESS_PRIORITY, processStat[0])
        jsonObject.put(SharePluginInfo.ISSUE_PROCESS_NICE, processStat[1])
        jsonObject.put(SharePluginInfo.ISSUE_PROCESS_FOREGROUND, isForeground)

        // memory info
        val memJsonObject = JSONObject()
        memJsonObject.put(SharePluginInfo.ISSUE_MEMORY_DALVIK, memoryInfo[0])
        memJsonObject.put(SharePluginInfo.ISSUE_MEMORY_NATIVE, memoryInfo[1])
        memJsonObject.put(SharePluginInfo.ISSUE_MEMORY_VM_SIZE, memoryInfo[2])
        jsonObject.put(SharePluginInfo.ISSUE_MEMORY, memJsonObject)

        // msgQueue
        val queueObject = gson.toJson(msgQueue, object : TypeToken<ArrayList<Msg>>() {}.type)
        jsonObject.put(SharePluginInfo.ISSUE_EXTRA, JSONArray(queueObject))

        val issue = Issue(
            key = curBoxMessage?.beginNs?.toString(),
            tag = SharePluginInfo.TAG_PLUGIN_EVIL_METHOD,
            content = jsonObject,
        )
        plugin.onDetectIssue(issue)
    }

    private fun dumpMemory(): LongArray {
        return longArrayOf(
            DeviceUtil.getDalvikHeap(), DeviceUtil.getNativeHeap(), DeviceUtil.getVmSize()
        )
    }

    private fun tryAddGapMsg(lastMsgEndNs: Long, beginNs: Long) {
        if (lastMsgEndNs < 0) return
        val duration = (beginNs - lastMsgEndNs) / Constants.TIME_MILLIS_TO_NANO
        if (duration > HistoryConstants.GAP_TIME) {
            msgQueue.add(Msg(type = Msg.TYPE_GAP, wallTime = duration))
        }
    }

    private fun addSystemMsg(message: BoxMessage) {
        msgQueue.add(
            Msg(
                type = Msg.TYPE_SYSTEM,
                wallTime = message.wallTime,
                cpuTime = message.cpuTime,
                detail = message.originMsg,
            )
        )
    }

    private fun addFatMsg(message: BoxMessage) {
        val stackTrace = Looper.getMainLooper().thread.stackTrace
        val dumpStack = Utils.getWholeStack(stackTrace)
        msgQueue.add(
            Msg(
                type = Msg.TYPE_FAT,
                wallTime = message.wallTime,
                cpuTime = message.cpuTime,
                detail = message.originMsg,
                stackTrace = dumpStack
            )
        )
    }

    private fun addAnrMsg(message: BoxMessage) {
        msgQueue.add(
            Msg(
                type = Msg.TYPE_ANR,
                wallTime = message.wallTime,
                cpuTime = message.cpuTime,
                detail = message.originMsg,
            )
        )
    }

    private fun addNormalMsg(
        curClusterMsg: Msg?,
        message: BoxMessage,
    ): Msg {
        var clusterMsg = curClusterMsg
        //合并后超过300ms，分两个消息
        if (clusterMsg != null && (clusterMsg.wallTime + message.wallTime) >= HistoryConstants.WARN_TIME) {
            addClusterMsg(curClusterMsg)
            clusterMsg = null
        }
        if (clusterMsg == null) {
            clusterMsg = Msg(type = Msg.TYPE_CLUSTER)
        }
        clusterMsg.wallTime += message.wallTime
        clusterMsg.cpuTime += message.cpuTime
        clusterMsg.count++
        clusterMsg.detail = message.originMsg
        return clusterMsg
    }

    private fun addClusterMsg(curClusterMsg: Msg?) {
        curClusterMsg?.run { msgQueue.add(this) }
    }

    //未执行的消息
    private fun addPendingMessage() {
        var i = 0
        Looper.getMainLooper().dump({
            if (i > 20) return@dump
            msgQueue.add(
                Msg(
                    type = Msg.TYPE_PENDING, detail = it
                )
            )
            i++
        }, "")
    }

    companion object {
        private const val MSG_START = 1
        private const val MSG_END = 2
        private const val MSG_ANR = 3
    }
}