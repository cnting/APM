package com.cnting.apm_trace_canary.core

import android.os.Build
import android.os.Looper
import android.os.MessageQueue.IdleHandler
import android.os.SystemClock
import android.util.Log
import android.util.Printer
import androidx.annotation.CallSuper
import com.cnting.apm_lib.util.ReflectUtils
import java.lang.Exception

/**
 * Created by cnting on 2023/7/24
 * 使用自定义的Printer，监听Message执行开始和结束
 */
class LooperMonitor(private val looper: Looper) : IdleHandler {
    companion object {
        private const val TAG = "LooperMonitor"
        private const val CHECK_TIME = 60 * 1000
        private val sMainMonitor = of(Looper.getMainLooper())

        private val sLooperMonitorMap = mutableMapOf<Looper, LooperMonitor>()

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
        listeners.forEach {
            if (it.isValid()) {
                if (isBegin) {
                    if (!it.isHasDispatchStart) {
                        it.onDispatchStart(s)
                    }
                } else {
                    if (it.isHasDispatchStart) {
                        it.onDispatchEnd(s)
                    }
                }
            } else if (!isBegin && it.isHasDispatchStart) {
                it.onDispatchEnd(s)
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
    fun onDispatchStart(s: String) {
        isHasDispatchStart = true
        dispatchStart()
    }

    @CallSuper
    fun onDispatchEnd(s: String) {
        isHasDispatchStart = false
        dispatchEnd()
    }

    abstract fun isValid(): Boolean

    abstract fun dispatchStart()
    abstract fun dispatchEnd()
}