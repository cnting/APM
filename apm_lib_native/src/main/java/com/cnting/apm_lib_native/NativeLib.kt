package com.cnting.apm_lib_native

class NativeLib {

    /**
     * A native method that is implemented by the 'apm_lib_native' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'apm_lib_native' library on application startup.
        init {
            System.loadLibrary("apm_lib_native")
        }
    }
}