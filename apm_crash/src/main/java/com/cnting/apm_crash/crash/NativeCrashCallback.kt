package com.cnting.apm_crash.crash

import java.lang.Error

/**
 * Created by cnting on 2023/10/29
 *
 */
interface NativeCrashCallback {
 fun onCrash(threadName:String,error: Error)
}