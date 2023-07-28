package com.cnting.apm_trace_canary.tracer

import com.cnting.apm_lib.lifecycle.owners.ProcessUiLifecycleOwner
import com.cnting.apm_trace_canary.listener.LooperObserver

/**
 * Created by cnting on 2023/7/24
 *
 */
abstract class Tracer : ITracer, LooperObserver() {

    @Volatile
    protected var isAlive = false
        private set

    override fun onStartTrace() {
        if (!isAlive) {
            isAlive = true
            onAlive()
        }
    }

    override fun onCloseTrace() {
        if (isAlive) {
            isAlive = false
            onDead()
        }
    }


    abstract fun onAlive()
    abstract fun onDead()

    fun isForeground(): Boolean {
        return ProcessUiLifecycleOwner.isForeground
    }
}