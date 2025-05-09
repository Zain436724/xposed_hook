package com.example.xposed_hooking_info.model

import android.content.Context
import android.os.Build
import com.example.xposed_hooking_info.util.ApiCompatibilityHelper

data class DeviceInfo(
    val imei: String = "",
    val brand: String = "",
    val buildId: String = "",
    val product: String = "",
    val model: String = "",
    val device: String = "",
    val hardwareSerial: String = "",
    val phoneNumber: String = "",
    val simImsi: String = "",
    val bootloader: String = "",
    val fingerprint: String = "",

    // Additional properties that might be useful
    val manufacturer: String = "",
    val board: String = "",
    val displayId: String = "",
    val androidVersion: String = "",
    val sdkVersion: String = ""
) {
    companion object {
        // Get current real device info
        fun getCurrentDeviceInfo(): DeviceInfo {
            return DeviceInfo(
                brand = Build.BRAND,
                model = Build.MODEL,
                device = Build.DEVICE,
                product = Build.PRODUCT,
                manufacturer = Build.MANUFACTURER,
                buildId = Build.ID,
                fingerprint = Build.FINGERPRINT,
                bootloader = Build.BOOTLOADER,
                board = Build.BOARD,
                displayId = Build.DISPLAY,
                androidVersion = Build.VERSION.RELEASE,
                sdkVersion = Build.VERSION.SDK_INT.toString(),
                // These properties require permission and will be handled separately
                imei = "",
                hardwareSerial = "",
                phoneNumber = "",
                simImsi = ""
            )
        }

        // Get current real device info with context-dependent properties
        fun getCurrentDeviceInfo(context: Context): DeviceInfo {
            val baseInfo = getCurrentDeviceInfo()

            return baseInfo.copy(
                imei = ApiCompatibilityHelper.getDeviceImei(context),
                hardwareSerial = ApiCompatibilityHelper.getDeviceSerial(context)
                // Phone number and IMSI would require additional permissions and code
            )
        }
    }
}