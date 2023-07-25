package com.cnting.apm_trace_canary.tracer

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.cnting.apm_lib.Matrix
import com.cnting.apm_lib.lifecycle.owners.ProcessUiLifecycleOwner
import com.cnting.apm_lib.report.Issue
import com.cnting.apm_lib.util.APMHandlerThread
import com.cnting.apm_lib.util.DeviceUtil
import com.cnting.apm_trace_canary.TracePlugin
import com.cnting.apm_trace_canary.config.SharePluginInfo
import com.cnting.apm_trace_canary.constant.Constants
import com.cnting.apm_trace_canary.core.UiThreadMonitor
import com.cnting.apm_trace_canary.util.Utils
import org.json.JSONObject

/**
 * Created by cnting on 2023/7/24
 * ANR检测：新开一个线程发送ANR超时消息，5秒后如果该消息还在，ANR报错
 */
class LooperAnrTracer : Tracer() {

    private var anrHandler: Handler? = null
    private var lagHandler: Handler? = null

    override fun onAlive() {
        UiThreadMonitor.addObserver(this)
        anrHandler = Handler(APMHandlerThread.defaultHandler.looper)
        lagHandler = Handler(APMHandlerThread.defaultHandler.looper)
    }

    override fun onDead() {
        UiThreadMonitor.removeObserver(this)
        anrHandler?.removeCallbacksAndMessages(null)
        lagHandler?.removeCallbacksAndMessages(null)
    }

    override fun dispatchBegin(beginNs: Long, cpuBeginNs: Long, token: Long) {
        super.dispatchBegin(beginNs, cpuBeginNs, token)
        val cost = (System.nanoTime() - token) / Constants.TIME_MILLIS_TO_NANO
        anrTask.token = token
        anrHandler?.postDelayed(anrTask, Constants.DEFAULT_ANR - cost)
        lagHandler?.postDelayed(lagTask, Constants.DEFAULT_NORMAL_LAG - cost)
    }

    override fun dispatchEnd(
        beginNs: Long,
        cpuBeginMs: Long,
        endNs: Long,
        cpuEndMs: Long,
        token: Long,
        isVsyncFrame: Boolean
    ) {
        super.dispatchEnd(beginNs, cpuBeginMs, endNs, cpuEndMs, token, isVsyncFrame)
        anrHandler?.removeCallbacks(anrTask)
        lagHandler?.removeCallbacks(lagTask)
    }

    private val anrTask = object : Runnable {
        var token: Long = 0

        override fun run() {
            val curTime = SystemClock.uptimeMillis()
            val isForeground = isForeground()

            //process
            val processStat = Utils.getProcessPriority(android.os.Process.myPid())
            val scene = ProcessUiLifecycleOwner.visibleScene

            //memory
            val memoryInfo = dumpMemory()

            //Thread state
            val status = Looper.getMainLooper().thread.state
            val stackTrace = Looper.getMainLooper().thread.stackTrace
            val dumpStack = Utils.getWholeStack(stackTrace)

            //frame
            val inputCost = UiThreadMonitor.getQueueCost(UiThreadMonitor.CALLBACK_INPUT, token)
            val animationCost =
                UiThreadMonitor.getQueueCost(UiThreadMonitor.CALLBACK_ANIMATION, token)
            val traversalCost =
                UiThreadMonitor.getQueueCost(UiThreadMonitor.CALLBACK_TRAVERSAL, token)

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

            val issue = Issue(
                key = token.toString(),
                tag = SharePluginInfo.TAG_PLUGIN_EVIL_METHOD,
                content = jsonObject,
            )
            plugin.onDetectIssue(issue)
        }
    }

    private val lagTask = Runnable {
        val scene = ProcessUiLifecycleOwner.visibleScene
        val isForeground = isForeground()
        val plugin = Matrix.with().getPluginByClass(TracePlugin::class.java) ?: return@Runnable

        val stackTrace = Looper.getMainLooper().thread.stackTrace
        val dumpStack = Utils.getWholeStack(stackTrace)

        var jsonObject = JSONObject()
        jsonObject = DeviceUtil.getDeviceInfo(jsonObject, Matrix.with().application)
        jsonObject.put(SharePluginInfo.ISSUE_STACK_TYPE, Constants.Type.LAG)
        jsonObject.put(SharePluginInfo.ISSUE_SCENE, scene)
        jsonObject.put(SharePluginInfo.ISSUE_THREAD_STACK, dumpStack)
        jsonObject.put(SharePluginInfo.ISSUE_PROCESS_FOREGROUND, isForeground)

        val issue = Issue(
            tag = SharePluginInfo.TAG_PLUGIN_EVIL_METHOD,
            content = jsonObject,
        )
        plugin.onDetectIssue(issue)
    }

    private fun dumpMemory(): LongArray {
        return longArrayOf(
            DeviceUtil.getDalvikHeap(),
            DeviceUtil.getNativeHeap(),
            DeviceUtil.getVmSize()
        )
    }
}