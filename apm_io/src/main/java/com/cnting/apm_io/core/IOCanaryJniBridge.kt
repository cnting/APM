package com.cnting.apm_io.core

import com.cnting.apm_io.util.IOCanaryUtil

/**
 * Created by cnting on 2023/11/9
 */
object IOCanaryJniBridge {
    private var isTryInstall = false
    private var isLoadJniLib = false

    fun install() {
        if (isTryInstall) {
            return
        }
        if (!loadJni()) {
            return
        }

        doHook()
        isTryInstall = true
    }

    fun uninstall() {
        if (!isTryInstall) {
            return
        }
        doUnHook()
        isTryInstall = false
    }

    private fun loadJni(): Boolean {
        if (isLoadJniLib) {
            return true
        }
        try {
            System.loadLibrary("apm_io")
        } catch (e: Exception) {
            isLoadJniLib = false
            return false
        }
        isLoadJniLib = true
        return true
    }

    private external fun doHook(): Int
    private external fun doUnHook(): Int

    class JavaContext {
        private val stack: String = IOCanaryUtil.getThrowableStack(Throwable())
        private val threadName: String = Thread.currentThread().name
    }

    //call by jni
    @JvmStatic
    private fun getJavaContext(): JavaContext {
        return JavaContext()
    }
}