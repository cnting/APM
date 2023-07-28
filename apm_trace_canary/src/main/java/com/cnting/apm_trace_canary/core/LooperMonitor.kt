package com.cnting.apm_trace_canary.core

import android.os.Looper
import android.os.MessageQueue.IdleHandler
import android.os.SystemClock
import android.util.Log
import android.util.Printer
import androidx.annotation.CallSuper
import com.cnting.apm_lib.util.ReflectUtils

/**
 * Created by cnting on 2023/7/24
 * 使用自定义的Printer，监听Message执行开始和结束
 */
class LooperMonitor(private val looper: Looper) : IdleHandler {
    companion object {
        private const val TAG = "LooperMonitor"
        private const val CHECK_TIME = 60 * 1000
        private val sLooperMonitorMap = mutableMapOf<Looper, LooperMonitor>()

        private val sMainMonitor = of(Looper.getMainLooper())

        private fun of(looper: Looper): LooperMonitor {
            return sLooperMonitorMap.getOrPut(looper) {
                LooperMonitor(looper)
            }
        }

        fun register(listener: LooperDispatchListener) {
            sMainMonitor.addListener(listener)
        }

        fun unregister(listener: LooperDispatchListener) {
            sMainMonitor.removeListener(listener)
        }
    }

    private val listeners = mutableSetOf<LooperDispatchListener>()
    private var printer: LooperPrinter? = null
    private var isReflectPrinterError = false
    private var lastCheckPrinterTime = 0L
    private val dispatchTimeMs = LongArray(4)

    init {
        resetPrinter()
        addIdleHandler()
    }

    @Synchronized
    private fun resetPrinter() {
        var originPrinter: Printer? = null
        if (!isReflectPrinterError) {
            try {
                originPrinter = ReflectUtils.get<Printer>(looper.javaClass, "mLogging", looper)
                if (originPrinter == printer && printer != null) {
                    return
                }
            } catch (e: Exception) {
                isReflectPrinterError = true
                Log.e(TAG, "[resetPrinter] $e")
            }
        }
        printer = LooperPrinter(originPrinter)
        looper.setMessageLogging(printer)
    }

    //每隔60秒检查一次printer
    private fun addIdleHandler() {
        looper.queue.addIdleHandler(this)
    }

    private fun addListener(listener: LooperDispatchListener) {
        listeners.add(listener)
    }

    private fun removeListener(listener: LooperDispatchListener) {
        listeners.remove(listener)
    }

    private fun dispatch(isBegin: Boolean, s: String) {
        //时间方法介绍：https://juejin.cn/post/6844904147628589070
        //System.nanoTime()：android系统开机到当前的时间，返回纳秒
        //SystemClock.currentThreadTimeMillis()：线程running的时间，线程Sleep的时间不会计入。
        if (isBegin) {
            dispatchTimeMs[0] = System.nanoTime()
            dispatchTimeMs[1] = SystemClock.currentThreadTimeMillis()
        } else {
            dispatchTimeMs[2] = System.nanoTime()
            dispatchTimeMs[3] = SystemClock.currentThreadTimeMillis()
        }
        listeners.forEach {
            if (it.isValid()) {
                if (isBegin) {
                    if (!it.isHasDispatchStart) {
                        it.onDispatchStart(s, dispatchTimeMs[0], dispatchTimeMs[1])
                    }
                } else {
                    if (it.isHasDispatchStart) {
                        it.onDispatchEnd(
                            s,
                            dispatchTimeMs[0],
                            dispatchTimeMs[1],
                            dispatchTimeMs[2],
                            dispatchTimeMs[3]
                        )
                    }
                }
            } else if (!isBegin && it.isHasDispatchStart) {
                it.onDispatchEnd(
                    s,
                    dispatchTimeMs[0],
                    dispatchTimeMs[1],
                    dispatchTimeMs[2],
                    dispatchTimeMs[3]
                )
            }
        }

    }

    override fun queueIdle(): Boolean {
        if (SystemClock.uptimeMillis() - lastCheckPrinterTime >= CHECK_TIME) {
            resetPrinter()
            lastCheckPrinterTime = SystemClock.uptimeMillis()
        }
        return true
    }

    private inner class LooperPrinter(private val originPrinter: Printer?) : Printer {

        override fun println(x: String?) {
            if (x == null) return

            originPrinter?.println(x)

            dispatch(x[0] == '>', x)
        }

    }


}


abstract class LooperDispatchListener {

    var isHasDispatchStart = false

    @CallSuper
    fun onDispatchStart(s: String, beginNs: Long, cpuBeginMs: Long) {
        isHasDispatchStart = true
        dispatchStart(s, beginNs, cpuBeginMs)
    }

    @CallSuper
    fun onDispatchEnd(
        s: String,
        beginNs: Long,
        cpuBeginMs: Long,
        endNs: Long,
        cpuEndMs: Long
    ) {
        isHasDispatchStart = false
        dispatchEnd(s, beginNs, cpuBeginMs, endNs, cpuEndMs)
    }

    abstract fun isValid(): Boolean

    abstract fun dispatchStart(s: String, beginNs: Long, cpuBeginMs: Long)
    abstract fun dispatchEnd(
        s: String, beginNs: Long,
        cpuBeginMs: Long,
        endNs: Long,
        cpuEndMs: Long
    )
}