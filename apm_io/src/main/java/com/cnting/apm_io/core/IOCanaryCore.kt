package com.cnting.apm_io.core

/**
 * Created by cnting on 2023/11/9
 *
 */
class IOCanaryCore {

    fun start() {
        IOCanaryJniBridge.install()
    }

    fun stop() {
        IOCanaryJniBridge.uninstall()
    }
}