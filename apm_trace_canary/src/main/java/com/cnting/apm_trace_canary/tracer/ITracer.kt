package com.cnting.apm_trace_canary.tracer

/**
 * Created by cnting on 2023/7/24
 *
 */
interface ITracer {
 fun onStartTrace()
 fun onCloseTrace()
}