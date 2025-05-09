package com.example.xposed_hooking_info

import android.app.Application
import com.example.xposed_hooking_info.hook.DeviceInfoHook
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.application.ModuleApplication

class XposedHookApplication : ModuleApplication() {

    override fun onCreate() {
        super.onCreate()

        // Initialize YukiHookAPI
        YukiHookAPI.configs {
            // Enable debug mode for development
            isDebug = true
            // Configure YukiHook as you need
            isAllowPrintingLogs = true
            isEnableDataChannel = true
        }

        // Initialize the module
        YukiHookAPI.encase {
            // Load our DeviceInfoHook hooker
            loadHooker(DeviceInfoHook())
        }
    }
}