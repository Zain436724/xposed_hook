package com.example.xposed_hooking_info.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat

/**
 * Helper class to handle API level differences
 * This ensures our app works across different Android versions
 */
class ApiCompatibilityHelper {

    companion object {
        /**
         * Gets the current device IMEI with proper handling of API level differences
         */
        fun getDeviceImei(context: Context): String {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return "Permission not granted"
            }

            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return try {
                when {
                    // Android 10+ requires READ_PRIVILEGED_PHONE_STATE for IMEI access
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        "Unavailable on Android 10+"
                    }
                    // Android 8+ supports getImei()
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        telephonyManager.imei ?: "Not available"
                    }
                    // Older versions use getDeviceId()
                    else -> {
                        @Suppress("DEPRECATION")
                        telephonyManager.deviceId ?: "Not available"
                    }
                }
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }

        /**
         * Gets the device serial number with proper handling of API level differences
         */
        fun getDeviceSerial(context: Context): String {
            return try {
                when {
                    // Android 10+ requires READ_PRIVILEGED_PHONE_STATE for serial access
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        "Unavailable on Android 10+"
                    }
                    // Android 8+ requires READ_PHONE_STATE for serial access
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                            "Permission not granted"
                        } else {
                            Build.getSerial()
                        }
                    }
                    // Older versions use SERIAL field
                    else -> {
                        @Suppress("DEPRECATION")
                        Build.SERIAL
                    }
                }
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }

        /**
         * Checks if this device needs runtime permissions handling
         * (Marshmallow and above)
         */
        fun needsRuntimePermissions(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        }
    }
}