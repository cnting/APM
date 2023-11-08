package com.cnting.apm_trace_canary.tracer

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.MessageQueue.IdleHandler
import com.cnting.apm_lib.APM
import com.cnting.apm_lib.lifecycle.owners.ProcessUiLifecycleOwner
import com.cnting.apm_lib.report.Issue
import com.cnting.apm_lib.util.DeviceUtil
import com.cnting.apm_trace_canary.TracePlugin
import com.cnting.apm_trace_canary.config.SharePluginInfo
import com.cnting.apm_trace_canary.constant.Constants
import com.cnting.apm_trace_canary.util.Utils
import org.json.JSONObject

/**
 * Created by cnting on 2023/7/25
 * Looper.printer的方式没法检测到耗时IdleHandler
 * 通过替换成自定义IdleHandler，在执行前后检测
 */
class IdleHandlerLagTracer : Tracer() {
    private var idleHandlerLagHandlerThread: HandlerThread? = null
    private var idleHandlerLagHandler: Handler? = null

    override fun onAlive() {
        idleHandlerLagHandlerThread = HandlerThread("IdleHandlerLagThread")
        detectIdleHandler()
    }

    override fun onDead() {
        idleHandlerLagHandler?.removeCallbacksAndMessages(null)
        idleHandlerLagHandlerThread?.quit()
    }

    private fun detectIdleHandler() {
        val messageQueue = Looper.getMainLooper().queue
        val field = messageQueue.javaClass.getDeclaredField("mIdleHandlers")
        field.isAccessible = true
        field.set(messageQueue, MyArrayList())
        idleHandlerLagHandlerThread?.also {
            it.start()
            idleHandlerLagHandler = Handler(it.looper)
        }
    }

    private val idleHandlerLagRunnable = Runnable {
        val plugin = APM.with().getPluginByClass(TracePlugin::class.java) ?: return@Runnable
        val stackTrace = Utils.getMainThreadJavaStackTrace()
        val isForeground = isForeground()
        val scene = ProcessUiLifecycleOwner.visibleScene
        var jsonObject = JSONObject()
        jsonObject = DeviceUtil.getDeviceInfo(jsonObject, APM.with().application)
        jsonObject.put(SharePluginInfo.ISSUE_STACK_TYPE, Constants.Type.LAG_IDLE_HANDLER)
        jsonObject.put(SharePluginInfo.ISSUE_SCENE, scene)
        jsonObject.put(SharePluginInfo.ISSUE_THREAD_STACK, stackTrace)
        jsonObject.put(SharePluginInfo.ISSUE_PROCESS_FOREGROUND, isForeground)

        val issue = Issue(
            tag = SharePluginInfo.TAG_PLUGIN_EVIL_METHOD,
            content = jsonObject,
        )
        plugin.onDetectIssue(issue)
    }

    inner class MyArrayList : ArrayList<Any>() {
        private val map = mutableMapOf<IdleHandler, MyIdleHandler>()

        //将IdleHandler换成MyIdleHandler
        override fun add(element: Any): Boolean {
            if (element is IdleHandler) {
                val myIdleHandler = MyIdleHandler(element)
                map[element] = myIdleHandler
                return super.add(myIdleHandler)
            }
            return super.add(element)
        }

        override fun remove(element: Any): Boolean {
            if (element is MyIdleHandler) {
                val idleHandler = element.origin
                map.remove(idleHandler)
                return super.remove(element)
            } else {
                val myIdleHandler = map.remove(element)
                if (myIdleHandler != null) {
                    return super.remove(myIdleHandler)
                }
                return super.remove(element)
            }
        }
    }

    inner class MyIdleHandler(val origin: IdleHandler) : IdleHandler {
        override fun queueIdle(): Boolean {
            idleHandlerLagHandler?.postDelayed(
                idleHandlerLagRunnable,
                Constants.DEFAULT_IDLE_HANDLER_LAG
            )
            val ret = origin.queueIdle()
            idleHandlerLagHandler?.removeCallbacks(idleHandlerLagRunnable)
            return ret
        }
    }
}