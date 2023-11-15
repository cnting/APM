package com.cnting.apm_battery

import android.app.Application
import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.cnting.apm_battery.hooker.SystemServiceBinderHooker
import com.cnting.apm_lib.listener.PluginListener
import com.cnting.apm_lib.plugin.Plugin
import java.lang.reflect.Method

/**
 * Created by cnting on 2023/11/15
 *
 */
class BatteryPlugin : Plugin() {
    override fun init(application: Application, pluginListener: PluginListener) {
        super.init(application, pluginListener)
        val hooker = SystemServiceBinderHooker(
            Context.WIFI_SERVICE,
            "android.net.wifi.IWifiManager",
            object : SystemServiceBinderHooker.HookCallback {
                override fun onServiceMethodInvoke(method: Method?, args: Array<out Any>?) {
                    Log.d("===>", "调用:${method?.name}")
                }

                override fun onServiceMethodIntercept(
                    receiver: Any?,
                    method: Method?,
                    args: Array<out Any>?
                ): Any? {
                    return null
                }
            })
        hooker.hook()

        val wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.startScan()
    }
}