package com.cnting.apm_crash.crash

/**
 * Created by cnting on 2023/10/29
 *
 */
object NativeCrashMonitor {
    init {
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
}