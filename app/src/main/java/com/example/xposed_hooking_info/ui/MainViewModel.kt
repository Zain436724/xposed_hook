package com.example.xposed_hooking_info.ui

import android.app.Application
import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.xposed_hooking_info.data.DevicePreferencesRepository
import com.example.xposed_hooking_info.model.DeviceInfo
import com.example.xposed_hooking_info.network.FakeDeviceApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DevicePreferencesRepository(application)
    private val apiService = FakeDeviceApiService()

    private val _currentDeviceInfo = MutableStateFlow(DeviceInfo.getCurrentDeviceInfo(application))
    val currentDeviceInfo: StateFlow<DeviceInfo> = _currentDeviceInfo.asStateFlow()

    private val _savedDeviceInfo = MutableStateFlow(DeviceInfo())
    val savedDeviceInfo: StateFlow<DeviceInfo> = _savedDeviceInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Load saved device info if any
        viewModelScope.launch {
            repository.deviceInfo.collectLatest { info ->
                if (info != DeviceInfo()) {
                    _savedDeviceInfo.value = info
                }
            }
        }

        // Request READ_PHONE_STATE permission if needed
        // The actual permission request will be handled by the activity
    }

    fun refreshCurrentDeviceInfo() {
        _currentDeviceInfo.value = DeviceInfo.getCurrentDeviceInfo(getApplication())
    }

    fun fetchRandomDeviceInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val fakeDeviceInfo = apiService.getFakeDeviceInfo()
                _savedDeviceInfo.value = fakeDeviceInfo
                saveDeviceInfo(fakeDeviceInfo)
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching device info: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateDeviceInfoField(fieldName: String, value: String) {
        val currentInfo = _savedDeviceInfo.value
        val updatedInfo = when (fieldName) {
            "imei" -> currentInfo.copy(imei = value)
            "brand" -> currentInfo.copy(brand = value)
            "buildId" -> currentInfo.copy(buildId = value)
            "product" -> currentInfo.copy(product = value)
            "model" -> currentInfo.copy(model = value)
            "device" -> currentInfo.copy(device = value)
            "hardwareSerial" -> currentInfo.copy(hardwareSerial = value)
            "phoneNumber" -> currentInfo.copy(phoneNumber = value)
            "simImsi" -> currentInfo.copy(simImsi = value)
            "bootloader" -> currentInfo.copy(bootloader = value)
            "fingerprint" -> currentInfo.copy(fingerprint = value)
            "manufacturer" -> currentInfo.copy(manufacturer = value)
            "board" -> currentInfo.copy(board = value)
            "displayId" -> currentInfo.copy(displayId = value)
            else -> currentInfo
        }

        _savedDeviceInfo.value = updatedInfo
        viewModelScope.launch {
            saveDeviceInfo(updatedInfo)
        }
    }

    fun saveDeviceInfo(deviceInfo: DeviceInfo) {
        viewModelScope.launch {
            repository.saveDeviceInfo(deviceInfo)
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun hasPhoneStatePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }
}