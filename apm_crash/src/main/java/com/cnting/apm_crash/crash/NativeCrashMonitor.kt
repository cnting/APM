package com.cnting.apm_crash.crash

import android.os.Looper
import java.lang.StringBuilder

/**
 * Created by cnting on 2023/10/29
 *
 */
object NativeCrashMonitor {
    private var systemThreadGroup: ThreadGroup? = null

    init {
        //systemThreadGroup属性不是public的，没法直接用
        try {
            val threadGroupClass = Class.forName("java.lang.ThreadGroup")
            val systemThreadGroupField = threadGroupClass.getDeclaredField("systemThreadGroup")
            systemThreadGroupField.isAccessible = true
            systemThreadGroup = systemThreadGroupField.get(null) as ThreadGroup
        } catch (e: Exception) {
            e.printStackTrace()
        }
        System.loadLibrary("apm_crash")
    }

    private var isInit = false
    fun init(callback: NativeCrashCallback) {
        if (isInit) {
            return
        }
        isInit = true
        nativeInit(callback)
        nativeSetup()
    }

    private external fun nativeInit(callback: NativeCrashCallback)

    private external fun nativeSetup()

    @JvmStatic
    fun getStackInfoByThreadName(threadName: String): String {
        val thread = getThreadByName(threadName) ?: return "找不到线程：$threadName"
        val sb = StringBuilder()
        thread.stackTrace.forEach {
            sb.append(it.toString()).append("\r\n")
        }
        return sb.toString()
    }

    @JvmStatic
    private fun getThreadByName(threadName: String): Thread? {
        if (threadName.isEmpty()) return null
        val thread: Thread? = if (threadName == "name") {
            Looper.getMainLooper().thread
        } else {
            val threadSet = getAllStackTraces().keys
            threadSet.find { it.name == threadName }
        }
        return thread
    }

    @JvmStatic
    private fun getAllStackTraces(): Map<Thread, Array<StackTraceElement>> {
        val finalSystemThreadGroup = systemThreadGroup ?: return Thread.getAllStackTraces()

        //下面这段代码跟Thread.getAllStackTraces()里是一样的
        val map = mutableMapOf<Thread, Array<StackTraceElement>>()
        var count = finalSystemThreadGroup.activeCount()
        val threads = arrayOfNulls<Thread>(count + count / 2)
        count = finalSystemThreadGroup.enumerate(threads)
        (0..count).forEach {
            threads[it]?.apply {
                map[this] = stackTrace
            }
        }
        return map
    }
}