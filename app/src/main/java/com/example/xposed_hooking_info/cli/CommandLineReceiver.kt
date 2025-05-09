package com.example.xposed_hooking_info.cli

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.xposed_hooking_info.data.DevicePreferencesRepository
import com.example.xposed_hooking_info.network.FakeDeviceApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Receiver for handling command line commands to change device info
 */
class CommandLineReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val resultCode = resultCode
        val action = intent.action

        if (action == ACTION_SPOOF_DEVICE) {
            // We'll run this in a coroutine since we need to make a network call
            CoroutineScope(Dispatchers.IO).launch {
                val repository = DevicePreferencesRepository(context)
                val apiService = FakeDeviceApiService()

                try {
                    // Fetch a new random device info from the API
                    val fakeDeviceInfo = apiService.getFakeDeviceInfo()

                    // Save the fetched device info
                    repository.saveDeviceInfo(fakeDeviceInfo)

                    // Send a success result if there's a result receiver
                    setResultCode(RESULT_SUCCESS)
                    setResultData("Successfully spoofed device information")
                } catch (e: Exception) {
                    // Send an error result if there's a result receiver
                    setResultCode(RESULT_ERROR)
                    setResultData("Error spoofing device information: ${e.message}")
                }
            }
        }
    }

    companion object {
        const val ACTION_SPOOF_DEVICE = "com.example.xposed_hooking_info.SPOOF_DEVICE"
        const val RESULT_SUCCESS = 0
        const val RESULT_ERROR = 1
    }
}