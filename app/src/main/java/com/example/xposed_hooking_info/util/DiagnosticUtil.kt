package com.example.xposed_hooking_info.util

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import com.example.xposed_hooking_info.data.DevicePreferencesRepository
import com.example.xposed_hooking_info.model.DeviceInfo

/**
 * Utility class to check if the spoofing is working correctly
 * This is primarily for administrators to verify the module is active
 */
class DiagnosticUtil(private val context: Context) {

    private val repository = DevicePreferencesRepository(context)
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    /**
     * Checks if the device information is being spoofed correctly
     * @return A DiagnosticResult with status information
     */
    fun checkSpoofingStatus(): DiagnosticResult {
        val savedDeviceInfo = repository.getSavedDeviceInfoSync(context)
        val result = DiagnosticResult()

        // Only check fields that have been set in the saved device info
        if (savedDeviceInfo.brand.isNotEmpty()) {
            result.addCheck("Brand", savedDeviceInfo.brand, Build.BRAND)
        }

        if (savedDeviceInfo.model.isNotEmpty()) {
            result.addCheck("Model", savedDeviceInfo.model, Build.MODEL)
        }

        if (savedDeviceInfo.device.isNotEmpty()) {
            result.addCheck("Device", savedDeviceInfo.device, Build.DEVICE)
        }

        if (savedDeviceInfo.manufacturer.isNotEmpty()) {
            result.addCheck("Manufacturer", savedDeviceInfo.manufacturer, Build.MANUFACTURER)
        }

        if (savedDeviceInfo.product.isNotEmpty()) {
            result.addCheck("Product", savedDeviceInfo.product, Build.PRODUCT)
        }

        if (savedDeviceInfo.buildId.isNotEmpty()) {
            result.addCheck("Build ID", savedDeviceInfo.buildId, Build.ID)
        }

        if (savedDeviceInfo.fingerprint.isNotEmpty()) {
            result.addCheck("Fingerprint", savedDeviceInfo.fingerprint, Build.FINGERPRINT)
        }

        try {
            // These may require permissions or may not be available on all devices
            if (savedDeviceInfo.imei.isNotEmpty()) {
                val actualImei = try {
                    telephonyManager.imei ?: telephonyManager.deviceId ?: ""
                } catch (e: Exception) {
                    "Permission denied"
                }
                result.addCheck("IMEI", savedDeviceInfo.imei, actualImei)
            }

            if (savedDeviceInfo.hardwareSerial.isNotEmpty()) {
                result.addCheck("Serial", savedDeviceInfo.hardwareSerial, Build.getSerial())
            }
        } catch (e: Exception) {
            // Ignore permission-related exceptions
        }

        return result
    }

    /**
     * Class to represent diagnostic results
     */
    data class DiagnosticResult(
        val checks: MutableList<Check> = mutableListOf()
    ) {
        fun addCheck(name: String, expected: String, actual: String) {
            checks.add(Check(name, expected, actual))
        }

        val allPassed: Boolean
            get() = checks.all { it.passed }

        val summary: String
            get() {
                val passedCount = checks.count { it.passed }
                return if (checks.isEmpty()) {
                    "No spoofing values have been set up yet."
                } else {
                    "$passedCount/${checks.size} values spoofed correctly"
                }
            }

        data class Check(
            val name: String,
            val expected: String,
            val actual: String
        ) {
            val passed: Boolean
                get() = expected == actual
        }
    }
}