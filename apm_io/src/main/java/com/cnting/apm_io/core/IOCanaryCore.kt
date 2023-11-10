package com.cnting.apm_io.core

import com.cnting.apm_io.detect.CloseGuardHooker

/**
 * Created by cnting on 2023/11/9
 *
 */
class IOCanaryCore {

    private var mCloseGuardHooker: CloseGuardHooker? = null

    fun start() {
        IOCanaryJniBridge.install()
        mCloseGuardHooker = CloseGuardHooker()
        mCloseGuardHooker?.hook()
    }

    fun stop() {
        IOCanaryJniBridge.uninstall()
    }
}