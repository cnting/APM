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
    open fun dispatchBegin(beginNs: Long, cpuBeginNs: Long, token: Long) {
        isDispatchBegin = true
    }

    @CallSuper
    open fun dispatchEnd(
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
        startNs: Long,
        endNs: Long,
        isVsyncFrame: Boolean,
        intendedFrameTimeNs: Long,
        inputCostNs: Long,
        animationCostNs: Long,
        traversalCostNs: Long
    ) {
    }
}