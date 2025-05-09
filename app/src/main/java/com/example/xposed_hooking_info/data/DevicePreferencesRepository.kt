package com.example.xposed_hooking_info.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.xposed_hooking_info.model.DeviceInfo
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "device_prefs")

class DevicePreferencesRepository(private val context: Context) {

    private val gson = Gson()

    // Define preferences keys
    object PreferencesKeys {
        val DEVICE_INFO = stringPreferencesKey("device_info")
    }

    // Save device information to preferences
    suspend fun saveDeviceInfo(deviceInfo: DeviceInfo) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEVICE_INFO] = gson.toJson(deviceInfo)
        }
    }

    // Get the saved device information as a Flow
    val deviceInfo: Flow<DeviceInfo> = context.dataStore.data.map { preferences ->
        val deviceInfoJson = preferences[PreferencesKeys.DEVICE_INFO] ?: ""
        if (deviceInfoJson.isNotEmpty()) {
            try {
                gson.fromJson(deviceInfoJson, DeviceInfo::class.java)
            } catch (e: Exception) {
                DeviceInfo()
            }
        } else {
            DeviceInfo()
        }
    }

    // Get the saved device information synchronously (for Xposed module)
    fun getSavedDeviceInfoSync(context: Context): DeviceInfo {
        val sharedPreferences = context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE)
        val deviceInfoJson = sharedPreferences.getString(PreferencesKeys.DEVICE_INFO.name, "")
        return if (deviceInfoJson != null && deviceInfoJson.isNotEmpty()) {
            try {
                gson.fromJson(deviceInfoJson, DeviceInfo::class.java)
            } catch (e: Exception) {
                DeviceInfo()
            }
        } else {
            DeviceInfo()
        }
    }
}