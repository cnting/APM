package com.cnting.apm_crash.crash

import android.os.Looper
import androidx.annotation.Keep
import com.cnting.apm_lib.util.ThreadUtil
import java.lang.StringBuilder

/**
 * Created by cnting on 2023/10/29
 *
 */
object NativeCrashMonitor {
    private var systemThreadGroup: ThreadGroup? = null

    init {
        systemThreadGroup = ThreadUtil.getSystemThreadGroup()
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

    @Keep
    @JvmStatic
    fun getStackInfoByThreadName(threadName: String): String {
        return ThreadUtil.getStackInfoByThreadName(threadName, systemThreadGroup)
    }
}