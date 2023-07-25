package com.cnting.apm_lib.util

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper

/**
 * Created by cnting on 2023/7/24
 *
 */
object APMHandlerThread {
    val mainHandler = Handler(Looper.getMainLooper())
    val defaultHandler: Handler by lazy {
        val handlerThread = HandlerThread("APMHandlerThread")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        handler
    }
}