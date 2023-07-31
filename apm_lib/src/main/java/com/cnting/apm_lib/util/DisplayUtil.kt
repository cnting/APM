package com.cnting.apm_lib.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.cnting.apm_lib.analyze.AnalyzeActivity

/**
 * Created by cnting on 2023/7/31
 *
 */
object DisplayUtil {
    fun showAnalyzeActivityInLauncher(context: Context) {
        val componentName = ComponentName(context, AnalyzeActivity::class.java)
        val packageManager = context.packageManager
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }
}