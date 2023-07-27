package com.cnting.apm_trace_canary.history

import com.cnting.apm_trace_canary.core.LooperDispatchListener

/**
 * Created by cnting on 2023/7/27
 *
 */
class LooperHistory : LooperDispatchListener() {


    override fun isValid(): Boolean {
        return true
    }

    override fun dispatchStart(s: String) {
    }

    override fun dispatchEnd(s: String) {
    }

}