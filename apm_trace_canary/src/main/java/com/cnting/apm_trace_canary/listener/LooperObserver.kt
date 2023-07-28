package com.cnting.apm_trace_canary.listener

import androidx.annotation.CallSuper

/**
 * Created by cnting on 2023/7/24
 *
 */
abstract class LooperObserver {
    var isDispatchBegin = false
        private set

    @CallSuper
    open fun dispatchBegin(s: String, beginNs: Long, cpuBeginMs: Long, token: Long) {
        isDispatchBegin = true
    }

    @CallSuper
    open fun dispatchEnd(
        s: String,
        beginNs: Long,
        cpuBeginMs: Long,
        endNs: Long,
        cpuEndMs: Long,
        token: Long,
        isVsyncFrame: Boolean
    ) {
        isDispatchBegin = false
    }

    open fun doFrame(
        focusActivity: String,
        startMs: Long,
        endMs: Long,
        isVsyncFrame: Boolean,
        intendedFrameTimeNs: Long,
        inputCostNs: Long,
        animationCostNs: Long,
        traversalCostNs: Long
    ) {
    }
}